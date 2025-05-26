from logging import ERROR, CRITICAL, FATAL, ERROR, WARNING, WARN, INFO, DEBUG, NOTSET, _levelToName
from app import constants 
import os
from tweepy import StreamingClient, StreamRule
from fastapi import FastAPI
from pydantic import BaseModel
from typing import Optional
import tweepy
import random
from app import ingest_producer
from app.util import generate_fake_tweet, generate_trace_id
import time
from faker import Faker
fake = Faker()

app = FastAPI()

class TwitterStream(StreamingClient):
    def on_tweet(self, tweet):
        print(f"Sending tweet to Kafka: {tweet}...")
    
    def on_errors(self, errors):
        print(f"Error: {errors}")

class AgentActionRequest(BaseModel):
    message_count: int = 5
    duration: float = 5

class RunXStream(BaseModel):
    run: bool = True

def run_test(count: int):
      for i in range(count):
        content = generate_fake_tweet()
        
        traceId = generate_trace_id()
        ingest_producer.ingest_message( content, traceId)


def register_agent(count: int):
    traceId = generate_trace_id()
    ingest_producer.ingest_message( constants.AGENT_META, traceId)

def stream_x():
    try:
        stream = TwitterStream( constants.INGEST_CONFIG_X_BEARER_TOKEN)
        stream.filter()
    except Exception as e:
        print(f"Failed to connect or stream from Twitter API: {e}")
        print("Use API TO MOCK")

def run_test_for_duration(duration_minutes: float):
    end_time = time.time() + (duration_minutes * 60)
    while time.time() < end_time:
        content = generate_fake_tweet()
        traceId = generate_trace_id()
        ingest_producer.ingest_message(content, traceId)
        time.sleep(random.uniform(0.5, 2))  

@app.post("/test_control/mock")
def test_run_mock(request: AgentActionRequest):
    run_test(request.message_count)
    return {"status": "success"}

@app.post("/test_control/mock/duration")
def test_run_mock(request: AgentActionRequest):
    run_test_for_duration(request.duration)
    return {"status": "success"}


@app.post("/test_control/run_x_stream")
def test_run_x_stream(request: RunXStream):
    stream_x()
    return {"status": "running x stream"}


if __name__ == "__main__":
    stream_x()