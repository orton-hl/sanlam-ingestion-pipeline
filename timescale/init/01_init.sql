
create schema sanlam_raw_data_ingest;



CREATE TABLE sanlam_raw_data_ingest.posts (
	traceId TEXT PRIMARY KEY,
	content JSONB,
	agent_meta JSONB,
	date TIMESTAMP
);


CREATE TABLE sanlam_raw_data_ingest.sanctioned_individuals (
    individual_id SERIAL PRIMARY KEY,
    reference_number VARCHAR(100),
    full_name VARCHAR(255),
    listed_on DATE,
    comments TEXT,
    title VARCHAR(100),
    designation VARCHAR(100),
    individual_date_of_birth DATE,
    individual_place_of_birth VARCHAR(255),
    individual_alias VARCHAR(255),
    nationality VARCHAR(100),
    individual_document TEXT,
    individual_address TEXT,
    application_status VARCHAR(50)
);


SELECT * FROM information_schema.tables 
WHERE table_schema = 'sanlam_raw_data_ingest';
