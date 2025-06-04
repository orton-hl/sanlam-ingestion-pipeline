import json
from logging import ERROR, INFO
from app import constants, ingest_logs_producer
from confluent_kafka import Producer
from app.util import generate_time_stamp


conf = {
    'bootstrap.servers' : constants.KAFKA_DLQ_BOOTSTRAP_SERVERS
}
producer = Producer(conf)

print('###############################')
print(conf)
print('###############################')

def dlq_delivery_report(err, msg):
    if err is not None:
        print(f"Delivery to DLQ failed: {err}")
        ingest_logs_producer.send_log_message(ERROR, {
            "details" : "FAILED TO ADDED MESSAGE TO DLQ",
            "error" : err
        }, key)
    else:
        key = msg.key().decode('utf-8') if msg.key() else None
        print(f"Message delivered to DLQ {key} [{msg.partition()}]")
        ingest_logs_producer.send_log_message(INFO, {
            "details" : "MESSAGE ADDED TO DLQ"
        }, key)

def send_to_dlq(content: dict, key: str):
    payload = json.dumps({
        "content": content,
        "meta": constants.AGENT_META,
        "date": generate_time_stamp()
    })

    producer.produce(constants.KAFKA_DLQ_TOPIC, key=key, value=payload, callback=(dlq_delivery_report))
    producer.flush()
4