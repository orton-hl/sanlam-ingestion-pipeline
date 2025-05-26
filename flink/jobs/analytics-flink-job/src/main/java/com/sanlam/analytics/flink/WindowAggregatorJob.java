package com.sanlam.analytics.flink;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.connector.elasticsearch.sink.Elasticsearch7SinkBuilder;
import org.apache.flink.connector.elasticsearch.sink.ElasticsearchSink;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.http.HttpHost;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.Requests;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WindowAggregatorJob {
    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        String kafkaBootstrapServers = System.getenv("KAFKA_INGEST_BOOTSTRAP_SERVERS");
        String kafkaTopic = System.getenv("KAFKA_INGEST_TOPIC");
        String kafkaGroupId = UUID.randomUUID().toString();

        String elasticSearch = System.getenv("ELASTIC_SEARCH_URL");
        int elasticSearchPort = System.getenv("ELASTIC_SEARCH_PORT") == null ? 9200 : Integer.parseInt(System.getenv("ELASTIC_SEARCH_PORT"));
        String kibana = System.getenv("KIBANA");

        DataStream<String> ingestStream = env.fromSource(
                KafkaSource.<String>builder()
                        .setBootstrapServers(kafkaBootstrapServers)
                        .setTopics(kafkaTopic)
                        .setGroupId(kafkaGroupId)
                        .setStartingOffsets(OffsetsInitializer.earliest())
                        .setValueOnlyDeserializer(new SimpleStringSchema())
                        .build(),
                WatermarkStrategy.noWatermarks(),
                "Kafka Source");

        DataStream<Tuple2<String, Integer>> aggregatedStream = ingestStream
                .map(new MapFunction<String, Tuple2<String, Integer>>() {
                    private final ObjectMapper mapper = new ObjectMapper();

                    @Override
                    public Tuple2<String, Integer> map(String json) throws Exception {
                        JsonNode root = mapper.readTree(json);
                        String lang = root.path("content").path("data").get(0).path("lang").asText("unknown");
                        return Tuple2.of(lang, 1);
                    }
                })
                .keyBy(value -> value.f0)
                .window(TumblingProcessingTimeWindows.of(Time.seconds(5)))
                .reduce(new ReduceFunction<Tuple2<String, Integer>>() {
                    @Override
                    public Tuple2<String, Integer> reduce(Tuple2<String, Integer> t0, Tuple2<String, Integer> t1) {
                        return Tuple2.of(t0.f0, t0.f1 + t1.f1);
                    }
                });

        ingestStream.print();

        ElasticsearchSink<Tuple2<String, Integer>> sink = new Elasticsearch7SinkBuilder<Tuple2<String, Integer>>().setBulkFlushMaxActions(1)
                .setHosts(new HttpHost(elasticSearch, elasticSearchPort, "http"))
                .setEmitter((element, context, indexer) ->  indexer.add(createIndexRequest(element)))
                .build();

        aggregatedStream.sinkTo(sink);
        env.execute("Trending Posts Monitor Job");
    }

    private static IndexRequest createIndexRequest(Tuple2<String, Integer> element) {
        Map<String, Object> json = new HashMap<>();
        json.put("lang", element.f0);
        json.put("count", element.f1);
        json.put("@timestamp", new Date());

        return Requests.indexRequest()
                .index("language_counts")
                .id(element.f0)
                .source(json);
    }

}
