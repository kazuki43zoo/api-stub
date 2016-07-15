package com.kazuki43zoo.domain.repository

import com.kazuki43zoo.domain.model.ApiResponse
import org.apache.ibatis.annotations.*
import org.apache.ibatis.jdbc.SQL
import org.springframework.util.StringUtils

@Mapper
interface ApiResponseRepository {

    @SelectProvider(type = SqlProvider.class, method = "findAll")
    List<ApiResponse> findAll(
            @Param("path") String path, @Param("method") String method, @Param("description") String description)

    @Select('''
        SELECT
            h.id, h.sub_id, o.path, o.method, o.data_key, h.status_code, h.description, h.created_at
        FROM
            mock_api_response_history h JOIN mock_api_response o ON o.id = h.id
        WHERE
            h.id = #{id}
        ORDER BY
            h.sub_id DESC
    ''')
    List<ApiResponse> findAllHistoryById(int id)

    @Select('''
        SELECT
            id, path, method, data_key, status_code, header, body, body_editor_mode, attachment_file, file_name, waiting_msec, description
        FROM
            mock_api_response
        WHERE
            path = #{path}
        AND
            method = UPPER(#{method})
        AND
            data_key = IFNULL(#{dataKey},'')
    ''')
    ApiResponse findOneByUk(
            @Param("path") String path, @Param("method") String method, @Param("dataKey") String dataKey)

    @Select('''
        SELECT
            id, path, method, data_key, status_code, header, body, body_editor_mode, attachment_file, file_name, waiting_msec, description
        FROM
            mock_api_response
        WHERE
            id = #{id}
    ''')
    ApiResponse findOne(int id)

    @Select('''
        SELECT
            h.id, h.sub_id, o.path, o.method, o.data_key, h.status_code, h.header, h.body, h.body_editor_mode
            , h.attachment_file, h.file_name, h.waiting_msec, h.description, h.created_at
        FROM
            mock_api_response_history h
        INNER JOIN
            mock_api_response o ON o.id = h.id
        WHERE
            h.id = #{id} AND h.sub_id = #{subId}
    ''')
    ApiResponse findHistory(@Param("id") int id, @Param("subId") int subId)

    @Insert('''
        INSERT INTO mock_api_response
            (path, method, data_key, status_code, header, body, body_editor_mode, attachment_file, file_name, waiting_msec, description)
        VALUES
            (#{path}, UPPER(#{method}), IFNULL(#{dataKey},''), #{statusCode}, #{header}, #{body}, #{bodyEditorMode}, #{attachmentFile}, #{fileName}, #{waitingMsec}, #{description})
    ''')
    @Options(useGeneratedKeys = true)
    void create(ApiResponse mockResponse)

    @Insert('''
        INSERT INTO mock_api_response_history
            (id, sub_id, status_code, header, body, body_editor_mode, attachment_file, file_name, waiting_msec, description, created_at)
        SELECT
            id, (SELECT IFNULL(MAX(sub_id), 0) + 1 FROM mock_api_response_history WHERE id = #{id}), status_code, header
            , body, body_editor_mode, attachment_file, file_name, waiting_msec, description, CURRENT_TIMESTAMP
        FROM
            mock_api_response
        WHERE
            id = #{id}
    ''')
    void createHistory(int id)

    @Update('''
        UPDATE mock_api_response
        SET
            data_key = IFNULL(#{dataKey},''), status_code = #{statusCode}, header = #{header}, body = #{body}
            , body_editor_mode = #{bodyEditorMode}, attachment_file = #{attachmentFile}, file_name = #{fileName}
            , waiting_msec = #{waitingMsec}, description = #{description}
        WHERE
            id = #{id}
    ''')
    void update(ApiResponse mockResponse)

    @Delete('''
        DELETE FROM
            mock_api_response
        WHERE
            id = #{id}
    ''')
    void delete(int id);

    @Delete('''
        DELETE FROM
            mock_api_response_history
        WHERE
            id = #{id}
        AND
            sub_id = #{subId}
    ''')
    void deleteHistory(@Param("id") int id, @Param("subId") int subId)

    @Delete('''
        DELETE FROM
            mock_api_response_history
        WHERE
            id = #{id}
    ''')
    void deleteAllHistory(int id);

    static class SqlProvider {
        public String findAll(
                @Param("path") String path, @Param("method") String method, @Param("description") String description) {
            return new SQL() {
                {
                    SELECT("id").SELECT("path").SELECT("method").SELECT("data_key").SELECT("status_code").SELECT("description")
                    FROM("mock_api_response")
                    if (StringUtils.hasLength(path)) {
                        WHERE("path REGEXP #{path}")
                    }
                    if (StringUtils.hasLength(method)) {
                        WHERE("method = UPPER(#{method})")
                    }
                    if (StringUtils.hasLength(description)) {
                        WHERE("description REGEXP #{description}")
                    }
                    ORDER_BY("path").ORDER_BY("method").ORDER_BY("data_key")
                }
            }.toString()
        }
    }

}