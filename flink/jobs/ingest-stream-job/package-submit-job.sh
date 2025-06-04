mvn clean package
docker cp ./target/ingest-flink-job-1.0-SNAPSHOT.jar  flink-jobmanager:/opt/flink/
docker exec -it flink-jobmanager flink run /opt/flink/ingest-flink-job-1.0-SNAPSHOT.jar
