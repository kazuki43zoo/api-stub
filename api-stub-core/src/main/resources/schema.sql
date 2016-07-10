-- DROP TABLE IF EXISTS mock_api_response;
-- DROP TABLE IF EXISTS mock_api_response_history;
-- DROP TABLE IF EXISTS mock_api;

CREATE TABLE IF NOT EXISTS mock_api_response (
  id IDENTITY
  ,path VARCHAR (256) NOT NULL
  ,method VARCHAR (16) NOT NULL
  ,data_key VARCHAR (2048) NOT NULL
  ,status_code INTEGER
  ,header TEXT
  ,body BLOB
  ,body_editor_mode VARCHAR (16)
  ,attachment_file BLOB
  ,file_name VARCHAR (256)
  ,waiting_msec BIGINT
  ,description TEXT
  ,CONSTRAINT pk_mock_api_response PRIMARY KEY(id)
  ,CONSTRAINT uk1_mock_api_response UNIQUE KEY(path, method, data_key)
);

CREATE TABLE IF NOT EXISTS mock_api_response_history (
   id INTEGER
  ,sub_id INTEGER
  ,status_code INTEGER
  ,header TEXT
  ,body BLOB
  ,body_editor_mode VARCHAR (16)
  ,attachment_file BLOB
  ,file_name VARCHAR (256)
  ,waiting_msec BIGINT
  ,description TEXT
  ,created_at TiMESTAMP NOT NULL
  ,CONSTRAINT pk_mock_api_response_history PRIMARY KEY(id, sub_id)
);

CREATE TABLE IF NOT EXISTS mock_api (
   id IDENTITY
  ,path VARCHAR (256) NOT NULL
  ,method VARCHAR (16) NOT NULL
  ,key_extractor VARCHAR(256)
  ,key_generating_strategy VARCHAR(16)
  ,expressions TEXT
  ,description TEXT
  ,CONSTRAINT pk_mock_api PRIMARY KEY(id)
  ,CONSTRAINT uk1_mock_api UNIQUE KEY(path, method)
);
