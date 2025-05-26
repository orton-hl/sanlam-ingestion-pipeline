import shortuuid
from datetime import datetime
from faker import Faker
import random
import uuid
import json
import time

fake = Faker()

def generate_trace_id():
    return shortuuid.uuid()

def generate_time_stamp():
    return datetime.utcnow().isoformat()


def generate_fake_tweet():
    tweet_id = str(uuid.uuid4().int)[:18]
    user_id = str(uuid.uuid4().int)[:18]
    media_key = str(uuid.uuid4())[:6]

    tweet = {
        "data": [
            {
                "id": tweet_id,
                "text": fake.sentence(nb_words=random.randint(6, 15)),
                "created_at": fake.date_time_this_year().isoformat(),
                "author_id": user_id,
                "conversation_id": tweet_id,
                "lang": random.choice(["en", "es", "fr", "de"]),
                "entities": {
                    "hashtags": [{"start": 0, "end": 10, "tag": fake.word()} for _ in range(random.randint(0, 2))],
                    "mentions": [{"start": 11, "end": 20, "username": fake.user_name()} for _ in range(random.randint(0, 2))],
                    "urls": [{
                        "start": 21,
                        "end": 45,
                        "url": fake.url(),
                        "expanded_url": fake.url(),
                        "display_url": fake.domain_name(),
                        "status": 200,
                        "title": fake.catch_phrase(),
                        "description": fake.text(max_nb_chars=50)
                    } for _ in range(random.randint(0, 2))]
                },
                "public_metrics": {
                    "retweet_count": random.randint(0, 1000),
                    "reply_count": random.randint(0, 500),
                    "like_count": random.randint(0, 2000),
                    "quote_count": random.randint(0, 300)
                },
                "attachments": {
                    "media_keys": [media_key]
                },
                "referenced_tweets": [
                    {
                        "type": random.choice(["retweeted", "quoted", "replied_to"]),
                        "id": str(uuid.uuid4().int)[:18]
                    }
                ],
                "edit_controls": {
                    "edits_remaining": random.randint(0, 5),
                    "is_edit_eligible": random.choice([True, False]),
                    "editable_until": fake.future_datetime().isoformat()
                }
            }
        ],
        "includes": {
            "users": [
                {
                    "id": user_id,
                    "name": fake.name(),
                    "username": fake.user_name(),
                    "created_at": fake.date_time_this_decade().isoformat(),
                    "description": fake.text(max_nb_chars=100),
                    "public_metrics": {
                        "followers_count": random.randint(0, 10000),
                        "following_count": random.randint(0, 5000),
                        "tweet_count": random.randint(100, 10000),
                        "listed_count": random.randint(0, 100)
                    }
                }
            ],
            "media": [
                {
                    "media_key": media_key,
                    "type": random.choice(["photo", "video"]),
                    "url": fake.image_url(),
                    "alt_text": fake.sentence(nb_words=6)
                }
            ]
        },
        "matching_rules": [
            {
                "id": str(uuid.uuid4()),
                "tag": fake.word()
            }
        ]
    }

    return tweet