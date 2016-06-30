-- DROP TABLE IF EXISTS mock_api_response;
-- DROP TABLE IF EXISTS mock_api_response_history;
-- DROP TABLE IF EXISTS mock_api;

CREATE TABLE IF NOT EXISTS mock_api_response (
  id IDENTITY
  ,path VARCHAR (256)
  ,method VARCHAR (16)
  ,status_code INTEGER
  ,header TEXT
  ,body BLOB
  ,attachment_file BLOB
  ,file_name VARCHAR (256)
  ,waiting_msec BIGINT
  ,description TEXT
  ,CONSTRAINT pk_mock_api_response PRIMARY KEY(id)
  ,CONSTRAINT uk1_mock_api_response UNIQUE KEY(path, method)
);

CREATE TABLE IF NOT EXISTS mock_api_response_history (
   id INTEGER
  ,sub_id INTEGER
  ,status_code INTEGER
  ,header TEXT
  ,body BLOB
  ,attachment_file BLOB
  ,file_name VARCHAR (256)
  ,waiting_msec BIGINT
  ,description TEXT
  ,created_at TiMESTAMP
  ,CONSTRAINT pk_mock_api_response_history PRIMARY KEY(id, sub_id)
);

CREATE TABLE IF NOT EXISTS mock_api (
  id IDENTITY
  ,path VARCHAR (256)
  ,method VARCHAR (16)
  ,content_type VARCHAR (128)
  ,description TEXT
  ,CONSTRAINT pk_mock_api PRIMARY KEY(id)
  ,CONSTRAINT uk1_mock_api UNIQUE KEY(path, method)
);

