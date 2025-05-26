from logging import ERROR, DEBUG
import os
from confluent_kafka import Producer
from app import constants, ingest_dlq_producer, ingest_logs_producer
from app.util import generate_time_stamp
import json
from confluent_kafka import Producer

conf = { 'bootstrap.servers' : constants.KAFKA_INGEST_BOOTSTRAP_SERVERS }
producer = Producer(conf)

def handle_response(err, msg):
    key = msg.key().decode('utf-8') if msg.key() else None
    content = msg.value().decode('utf-8') if msg.value() else None

    if err is not None:
        ingest_logs_producer.send_log_message(ERROR, {
            "details" : "MESSAGE DELIVERY FAILED",
            "error" :err
        }, key)
        ingest_dlq_producer.send_to_dlq(content, key)
    else: 
        key = msg.key().decode('utf-8') if msg.key() else None
        ingest_logs_producer.send_log_message(DEBUG, {
            "details" : "message delivered",
            "key" : key,
        }, key)

        print(f"Message delivered to ingest queue {key} [{msg.partition()}]")


def ingest_message(content: dict, traceId: str):
    payload = json.dumps({
        "content": content,
        "agent_meta": constants.AGENT_META,
        "traceId": traceId,
        "date": generate_time_stamp()
    })

    producer.produce(constants.KAFKA_INGEST_TOPIC, key=traceId, value=payload, callback=(handle_response))
    producer.flush()
