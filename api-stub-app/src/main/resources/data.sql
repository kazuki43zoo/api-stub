TRUNCATE TABLE mock_api;
INSERT INTO mock_api (path, method, content_type, description) VALUES ('/v1/member', 'GET', 'application/json', 'テスト用');
INSERT INTO mock_api (path, method, content_type, description) VALUES ('/v1/member/file', 'GET', 'application/octet-stream', 'テスト用');
