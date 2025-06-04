
create schema sanlam_raw_data_ingest;



CREATE TABLE sanlam_raw_data_ingest.posts (
	traceId TEXT PRIMARY KEY,
	content JSONB,
	agent_meta JSONB,
	date TIMESTAMP
);


CREATE TABLE sanlam_raw_data_ingest.sanctioned_individuals (
    id SERIAL PRIMARY KEY,
    individual_id TEXT,
    reference_number TEXT,
    full_name TEXT,
    listed_on TEXT,
    comments TEXT,
    title TEXT,
    designation TEXT,
    individual_date_of_birth TEXT,
    individual_place_of_birth TEXT,
    individual_alias TEXT,
    nationality TEXT,
    individual_document TEXT,
    individual_address TEXT,
    application_status TEXT
);


SELECT * FROM information_schema.tables 
WHERE table_schema = 'sanlam_raw_data_ingest';
