import json
from logging import _levelToName
from confluent_kafka import Producer

from app.constants import AGENT_META, ENV, KAFKA_LOGS_BOOTSTRAP_SERVERS, KAFKA_LOGS_TOPIC
from app.util import generate_time_stamp
# from util import generate_time_stamp, generate_trace_id
# from constants import KAFKA_LOGS_BOOTSTRAP_SERVERS, KAFKA_LOGS_TOPIC, ENV, AGENT_META, LOG_LEVEL
# from logging import ERROR, CRITICAL, FATAL, WARNING, WARN, INFO, DEBUG, _levelToName, _nameToLevel


conf = {
    'bootstrap.servers' : KAFKA_LOGS_BOOTSTRAP_SERVERS
}
producer = Producer(conf)

print('###############################')
print(conf)
print('###############################')

def log_delivery_report(err, msg):
    key = msg.key().decode('utf-8') if msg.key() else None
    # if err is not None:
    #     print(f"Delivery failed to LOG QUEUE: {err}")
    # else:
    #     print(f"Message {key} delivered to LOG QUEUE ")

def send_log_message(level: int, content: dict, key: str):

    value = json.dumps({
        "level": _levelToName.get(level),
        "content": content,
        "meta": AGENT_META,
        "date": generate_time_stamp()
    })

    producer.produce(KAFKA_LOGS_TOPIC, key=key, value=value, callback=log_delivery_report)
    producer.flush()
