import os
from dotenv import load_dotenv
from confluent_kafka import Producer
import socket

from app.util import generate_trace_id

load_dotenv()

def get_env_or_raise(var_name):
    value = os.getenv(var_name)
    if not value:
        raise EnvironmentError(f"Missing required environment variable: {var_name}")
    return value

ENV = get_env_or_raise("ENV")
AGENT_ID =  generate_trace_id()

DEBUG_MODE = get_env_or_raise("DEBUG").lower() == "true"
LOG_LEVEL = get_env_or_raise("LOG_LEVEL")

KAFKA_INGEST_BOOTSTRAP_SERVERS = get_env_or_raise("KAFKA_INGEST_BOOTSTRAP_SERVERS")
KAFKA_INGEST_TOPIC = get_env_or_raise("KAFKA_INGEST_TOPIC")

KAFKA_DLQ_BOOTSTRAP_SERVERS = get_env_or_raise("KAFKA_DLQ_BOOTSTRAP_SERVERS")
KAFKA_DLQ_TOPIC = get_env_or_raise("KAFKA_DLQ_TOPIC")

KAFKA_LOGS_BOOTSTRAP_SERVERS = get_env_or_raise("KAFKA_LOGS_BOOTSTRAP_SERVERS")
KAFKA_LOGS_TOPIC = get_env_or_raise("KAFKA_LOGS_TOPIC")

INGEST_CONFIG_SOURCE = get_env_or_raise("INGEST_CONFIG_SOURCE")
INGEST_CONFIG_VERSION = get_env_or_raise("INGEST_CONFIG_VERSION")

INGEST_CONFIG_X_BEARER_TOKEN = get_env_or_raise("INGEST_CONFIG_X_BEARER_TOKEN")

AGENT_META = {
    "host": socket.gethostname(),
    "fqdn":  socket.getfqdn(),
    "agent_id":  AGENT_ID,
    "env":  ENV,
    "id" : generate_trace_id()
}
