package com.sanlam.ingest.flink;

import com.sanlam.ingest.flink.dto.SanctionedIndividual;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.connector.file.src.FileSource;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.dataformat.csv.CsvMapper;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.dataformat.csv.CsvParser;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.dataformat.csv.CsvSchema;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.core.fs.Path;
import org.apache.flink.connector.jdbc.JdbcConnectionOptions;
import org.apache.flink.connector.jdbc.JdbcExecutionOptions;
import org.apache.flink.connector.jdbc.JdbcSink;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import java.io.File;
import org.apache.flink.formats.csv.CsvReaderFormat;


public class IngestBatchJob {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        String jdbcUrl = System.getenv("POSTGRES_JDBC_URL");
        String jdbcUser = System.getenv("POSTGRES_USER");
        String jdbcPassword = System.getenv("POSTGRES_PASSWORD");
        String batchFilePath = System.getenv("BATCH_FILE_PATH");

        CsvMapper csvMapper = new CsvMapper();
        csvMapper.enable(CsvParser.Feature.TRIM_SPACES);
        csvMapper.enable(CsvParser.Feature.IGNORE_TRAILING_UNMAPPABLE);

        CsvSchema csvSchema = CsvSchema.builder()
                .setColumnSeparator('|')
                .setQuoteChar('"')
                .setEscapeChar('\\')
                .addColumn("individualID")
                .addColumn("referenceNumber")
                .addColumn("fullName")
                .addColumn("listedOn")
                .addColumn("comments")
                .addColumn("title")
                .addColumn("designation")
                .addColumn("individualDateOfBirth")
                .addColumn("individualPlaceOfBirth")
                .addColumn("individualAlias")
                .addColumn("nationality")
                .addColumn("individualDocument")
                .addColumn("individualAddress")
                .addColumn("applicationStatus")
                .setUseHeader(true)
                .build();

        CsvReaderFormat<SanctionedIndividual> csvFormat = CsvReaderFormat.forSchema(
                csvSchema,
                TypeInformation.of(SanctionedIndividual.class)
        );

        FileSource<SanctionedIndividual> source = FileSource
                .forRecordStreamFormat(
                        csvFormat,
                        Path.fromLocalFile(new File(batchFilePath))
                )
                .build();

        DataStream<SanctionedIndividual> lines = env.fromSource(
                source,
                WatermarkStrategy.noWatermarks(),
                "csv-file-source"
        );


        String insertStatement = "INSERT INTO sanlam_raw_data_ingest.sanctioned_individuals\n" +
                "(id, individual_id, reference_number, full_name, listed_on, \"comments\", title, designation, individual_date_of_birth, individual_place_of_birth, individual_alias, nationality, individual_document, individual_address, application_status)\n" +
                "VALUES(nextval('sanlam_raw_data_ingest.sanctioned_individuals_id_seq'::regclass), ?,  ?,  ?,  ?,  ?,  ?,  ?,  ?,  ?,  ?,  ?,  ?,  ?,  ?);";

        lines.print();

        lines.addSink(JdbcSink.sink(
                insertStatement,
                (PreparedStatement ps, SanctionedIndividual sanctionedIndividual) -> {
                    try {
                        ps.setString(1, sanctionedIndividual.getIndividualID());
                        ps.setString(2, sanctionedIndividual.getReferenceNumber());
                        ps.setString(3, sanctionedIndividual.getFullName());
                        ps.setString(4, sanctionedIndividual.getListedOn());
                        ps.setString(5, sanctionedIndividual.getComments());
                        ps.setString(6, sanctionedIndividual.getTitle());
                        ps.setString(7, sanctionedIndividual.getDesignation());
                        ps.setString(8, sanctionedIndividual.getIndividualDateOfBirth());
                        ps.setString(9, sanctionedIndividual.getIndividualPlaceOfBirth());
                        ps.setString(10, sanctionedIndividual.getIndividualAlias());
                        ps.setString(11, sanctionedIndividual.getNationality());
                        ps.setString(12, sanctionedIndividual.getIndividualDocument());
                        ps.setString(13, sanctionedIndividual.getIndividualAddress());
                        ps.setString(14, sanctionedIndividual.getApplicationStatus());
                    } catch (SQLException e) {
                        System.err.println("Error binding SanctionedIndividual to statement: " + sanctionedIndividual);
                        e.printStackTrace();
                        throw e;
                    }
                },
                JdbcExecutionOptions.builder()
                        .withBatchSize(2)
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

        env.execute("CSV to Tuple Processing");
    }
}
