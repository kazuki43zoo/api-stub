package com.kazuki43zoo.domain;

import org.apache.ibatis.annotations.*;
import org.apache.ibatis.jdbc.SQL;
import org.springframework.util.StringUtils;

import java.util.List;

@Mapper
public interface MockApiResponseRepository {

    @SelectProvider(type = SqlProvider.class, method = "findAll")
    List<MockApiResponse> findAll(@Param("path") String path, @Param("description") String description);

    @Select("SELECT h.id, h.sub_id, o.path, o.method, h.status_code, h.description, h.created_at " +
            "FROM mock_api_response_history h JOIN mock_api_response o ON o.id = h.id " +
            "WHERE h.id = #{id} " +
            "ORDER BY h.sub_id DESC")
    List<MockApiResponse> findAllHistoryById(int id);

    @Select("SELECT id, path, method, status_code, header, body, attachment_file, file_name, waiting_msec, description " +
            "FROM mock_api_response " +
            "WHERE path = #{path} AND method = #{method}")
    MockApiResponse findOneByUk(@Param("path") String path, @Param("method") String method);

    @Select("SELECT id, path, method, status_code, header, body, attachment_file, file_name, waiting_msec, description " +
            "FROM mock_api_response " +
            "WHERE id = #{id}")
    MockApiResponse find(int id);

    @Select("SELECT h.id, h.sub_id, o.path, o.method, h.status_code, h.header, h.body, h.attachment_file, h.file_name, h.waiting_msec, h.description, h.created_at " +
            "FROM mock_api_response_history h JOIN mock_api_response o ON o.id = h.id " +
            "WHERE h.id = #{id} AND h.sub_id = #{subId}")
    MockApiResponse findHistory(@Param("id") int id, @Param("subId") int subId);

    @Insert("INSERT INTO mock_api_response (path, method, status_code, header, body, attachment_file, file_name, waiting_msec, description) " +
            "VALUES(#{path}, #{method}, #{statusCode}, #{header}, #{body}, #{attachmentFile}, #{fileName}, #{waitingMsec}, #{description})")
    @Options(useGeneratedKeys = true)
    void create(MockApiResponse mockResponse);

    @Insert("INSERT INTO mock_api_response_history (id, sub_id, status_code, header, body, attachment_file, file_name, waiting_msec, description, created_at) " +
            "SELECT id, (SELECT IFNULL(MAX(sub_id), 0) + 1 FROM mock_api_response_history WHERE id = #{id}), status_code, header, body, attachment_file, file_name, waiting_msec, description, CURRENT_TIMESTAMP FROM mock_api_response WHERE id = #{id}")
    void createHistory(int id);

    @Update("UPDATE mock_api_response " +
            "SET path = #{path}, method = #{method}, status_code = #{statusCode}, header = #{header}, body = #{body}, attachment_file = #{attachmentFile}, file_name = #{fileName}, waiting_msec = #{waitingMsec}, description = #{description} " +
            "WHERE id = #{id}")
    void update(MockApiResponse mockResponse);

    @Delete("DELETE FROM mock_api_response " +
            "WHERE id = #{id}")
    void delete(int id);

    class SqlProvider {
        public String findAll(@Param("path") String path, @Param("description") String description) {
            return new SQL() {{
                SELECT("id").SELECT("path").SELECT("method").SELECT("status_code").SELECT("description");
                FROM("mock_api_response");
                if (StringUtils.hasLength(path)) {
                    WHERE("path REGEXP #{path}");
                }
                if (StringUtils.hasLength(description)) {
                    WHERE("description REGEXP #{description}");
                }
                ORDER_BY("path").ORDER_BY("method");
            }}.toString();

        }
    }

}
