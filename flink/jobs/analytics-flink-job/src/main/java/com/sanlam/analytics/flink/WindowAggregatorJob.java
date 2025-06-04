package com.sanlam.analytics.flink;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.FlatMapFunction;
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
import org.apache.flink.util.Collector;
import org.apache.http.HttpHost;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.Requests;

import java.util.*;

public class WindowAggregatorJob {
    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        String kafkaBootstrapServers = System.getenv("KAFKA_INGEST_BOOTSTRAP_SERVERS");
        String kafkaTopic = System.getenv("KAFKA_INGEST_TOPIC");
        String kafkaGroupId = UUID.randomUUID().toString();

        String elasticSearchHost = System.getenv("ELASTIC_SEARCH_HOST");
        int elasticSearchPort = System.getenv("ELASTIC_SEARCH_PORT") == null ? 9200 : Integer.parseInt(System.getenv("ELASTIC_SEARCH_PORT"));
        String kibana = System.getenv("KIBANA");

        DataStream<String> ingestStream = env.fromSource(KafkaSource.<String>builder().setBootstrapServers(kafkaBootstrapServers).setTopics(kafkaTopic).setGroupId(kafkaGroupId).setStartingOffsets(OffsetsInitializer.earliest()).setValueOnlyDeserializer(new SimpleStringSchema()).build(), WatermarkStrategy.noWatermarks(), "Kafka Source");


        DataStream<Tuple2<String, Integer>> aggregatedStream = ingestStream.flatMap(new FlatMapFunction<String, Tuple2<String, Integer>>() {
            final ObjectMapper mapper = new ObjectMapper();

            @Override
            public void flatMap(String json, Collector<Tuple2<String, Integer>> out) throws Exception {
                JsonNode root = mapper.readTree(json);
                String text = root.path("content").path("data").get(0).path("text").asText("unknown");

                for (String word : text.split("\\W+")) {
                    if (!word.isEmpty()) {
                        out.collect(Tuple2.of(word.toLowerCase(), 1));
                    }
                }
            }
        })
                .filter(new FilterFunction<Tuple2<String, Integer>>() {
                    @Override
                    public boolean filter(Tuple2<String, Integer> entry) throws Exception {
                        return entry.f0.length() > 5;
                    }
                })

        .keyBy(value -> value.f0)
                .window(TumblingProcessingTimeWindows.of(Time.seconds(2)))
                .reduce(new ReduceFunction<Tuple2<String, Integer>>() {
                    @Override
                    public Tuple2<String, Integer> reduce(Tuple2<String, Integer> t0, Tuple2<String, Integer> t1) {
                        return Tuple2.of(t0.f0, t0.f1 + t1.f1);
                    }
                });

                ;
        ingestStream.print();

        ElasticsearchSink<Tuple2<String, Integer>> sink = new Elasticsearch7SinkBuilder<Tuple2<String, Integer>>().setBulkFlushMaxActions(1).setHosts(new HttpHost(elasticSearchHost, elasticSearchPort, "http")).setEmitter((element, context, indexer) -> indexer.add(createIndexRequest(element))).build();

        aggregatedStream.sinkTo(sink);
        env.execute("Trending Posts Monitor Job");
    }

    private static IndexRequest createIndexRequest(Tuple2<String, Integer> element) {
        Map<String, Object> json = new HashMap<>();
        json.put("word", element.f0);
        json.put("count", element.f1);
        json.put("@timestamp", new Date());

        return Requests.indexRequest().index("word_count").id(element.f0).source(json);
    }

}
