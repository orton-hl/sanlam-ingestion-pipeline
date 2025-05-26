package com.sanlam.ingest.flink;


import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.tuple.Tuple14;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.connector.file.src.FileSource;
import org.apache.flink.connector.file.src.reader.SimpleStreamFormat;
import org.apache.flink.core.fs.Path;
import org.apache.flink.formats.csv.CsvReaderFormat;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class IngestBatchJob {

    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        String jdbcUrl = System.getenv("POSTGRES_JDBC_URL");
        String jdbcUser = System.getenv("POSTGRES_USER");
        String jdbcPassword = System.getenv("POSTGRES_PASSWORD");
        String batchFilePath = System.getenv("BATCH_FILE_PATH");


// Read CSV file as text
        DataStream<String> text = env.readTextFile(batchFilePath);

// Parse each line into a tuple with all fields
        DataStream<Tuple14<String, String, String, String, String, String, String, String,
                String, String, String, String, String, String>> tupleData = text
                .filter(line -> !line.startsWith("IndividualID")) // Skip header if present
                .map(new MapFunction<String, Tuple14<String, String, String, String, String,
                                        String, String, String, String, String,
                                        String, String, String, String>>() {
                    @Override
                    public Tuple14<String, String, String, String, String, String, String,
                            String, String, String, String, String, String, String> map(String value) {
                        String[] fields = value.split(",");
                        return new Tuple14<>(
                                fields[0].trim(),  // IndividualID
                                fields[1].trim(),  // ReferenceNumber
                                fields[2].trim(),  // FullName
                                fields[3].trim(),  // ListedOn
                                fields[4].trim(),  // Comments
                                fields[5].trim(),  // Title
                                fields[6].trim(),  // Designation
                                fields[7].trim(),  // IndividualDateOfBirth
                                fields[8].trim(),  // IndividualPlaceOfBirth
                                fields[9].trim(),  // IndividualAlias
                                fields[10].trim(), // Nationality
                                fields[11].trim(), // IndividualDocument
                                fields[12].trim(), // IndividualAddress
                                fields[13].trim()  // ApplicationStatus
                        );
                    }
                });

        tupleData.print();
        env.execute("CSV to Tuple Processing");

    }
}
