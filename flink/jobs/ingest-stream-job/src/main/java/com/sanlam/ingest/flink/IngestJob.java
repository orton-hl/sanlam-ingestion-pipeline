package com.sanlam.ingest.flink;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sanlam.ingest.flink.dto.Post;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.jdbc.JdbcConnectionOptions;
import org.apache.flink.connector.jdbc.JdbcExecutionOptions;
import org.apache.flink.connector.jdbc.JdbcSink;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import java.util.UUID;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class IngestJob {
    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        String kafkaBootstrapServers = System.getenv("KAFKA_INGEST_BOOTSTRAP_SERVERS");
        String kafkaTopic = System.getenv("KAFKA_INGEST_TOPIC");
        String kafkaGroupId = UUID.randomUUID().toString();

        String jdbcUrl = System.getenv("POSTGRES_JDBC_URL");
        String jdbcUser = System.getenv("POSTGRES_USER");
        String jdbcPassword = System.getenv("POSTGRES_PASSWORD");

        KafkaSource<String> kafkaSource = KafkaSource.<String>builder()
                .setBootstrapServers(kafkaBootstrapServers)
                .setTopics(kafkaTopic)
                .setGroupId(kafkaGroupId)
                .setStartingOffsets(OffsetsInitializer.earliest())
                .setValueOnlyDeserializer(new SimpleStringSchema())
                .build();

        DataStream<String> kafkaStream = env.fromSource(
                kafkaSource,
                WatermarkStrategy.noWatermarks(),
                "Kafka Source"
        );

        ObjectMapper objectMapper = new ObjectMapper();

        DataStream<Post> parsedStream = kafkaStream.map(json -> {
            JsonNode root = objectMapper.readTree(json);
            String content = root.has("content") ? root.get("content").toString() : null;
            String agentMeta = root.has("agent_meta") ? root.get("agent_meta").toString() : null;
            String traceId = root.has("traceId") ? root.get("traceId").asText() : null;
            String date = root.has("date") ? root.get("date").asText() : null;

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS");
            LocalDateTime ldt = LocalDateTime.parse(date, formatter);

            return new Post(traceId, content, agentMeta, Timestamp.valueOf(ldt));
        });

        parsedStream.map(post -> {
            System.out.println("Inserting Post: " + post);
            return post;
        }).addSink(JdbcSink.sink(
                "INSERT INTO sanlam_raw_data_ingest.posts (traceId, content, agent_meta, date) VALUES (?, ?::jsonb, ?::jsonb, ?) " +
                        "ON CONFLICT (traceId) DO UPDATE SET content = EXCLUDED.content, agent_meta = EXCLUDED.agent_meta, date = EXCLUDED.date",
                (PreparedStatement ps, Post post) -> {
                    try {
                        ps.setString(1, post.getTraceId());
                        ps.setString(2, post.getContent());
                        ps.setString(3, post.getAgentMeta());
                        ps.setTimestamp(4, post.getDate());
                    } catch (SQLException e) {
                        System.err.println("Error binding Post to statement: " + post);
                        e.printStackTrace();
                        throw e;
                    }
                },
                JdbcExecutionOptions.builder()
                        .withBatchSize(10)
                        .withBatchIntervalMs(10)
                        .withMaxRetries(3)
                        .build(),
                new JdbcConnectionOptions.JdbcConnectionOptionsBuilder()
                        .withUrl(jdbcUrl)
                        .withDriverName("org.postgresql.Driver")
                        .withUsername(jdbcUser)
                        .withPassword(jdbcPassword)
                        .build()
        ));

        env.execute("Flink Kafka Consumer Job");
    }
}
