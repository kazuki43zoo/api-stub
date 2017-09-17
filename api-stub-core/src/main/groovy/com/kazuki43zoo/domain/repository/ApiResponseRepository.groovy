/*
 *    Copyright 2016-2017 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.kazuki43zoo.domain.repository

import com.kazuki43zoo.domain.model.ApiResponse
import org.apache.ibatis.annotations.*
import org.apache.ibatis.jdbc.SQL
import org.apache.ibatis.session.RowBounds
import org.springframework.util.StringUtils

//language=SQL
@Mapper
interface ApiResponseRepository {

    @SelectProvider(type = SqlProvider.class, method = "findPage")
    List<ApiResponse> findPage(
            @Param("path") String path, @Param("method") String method, @Param("description") String description, RowBounds rowBounds)

    @SelectProvider(type = SqlProvider.class, method = "count")
    long count(@Param("path") String path, @Param("method") String method, @Param("description") String description)

    @Select('''
        SELECT
            h.id, h.sub_id, o.path, o.method, o.data_key, h.status_code, h.description, h.created_at
        FROM
            mock_api_response_history h
        INNER JOIN
            mock_api_response o ON o.id = h.id
        WHERE
            h.id = #{id}
        ORDER BY
            h.sub_id DESC
    ''')
    List<ApiResponse> findPageHistoryById(int id, RowBounds rowBounds)

    @Select('''
        SELECT
            COUNT(*)
        FROM
            mock_api_response_history h
        INNER JOIN
            mock_api_response o ON o.id = h.id
        WHERE
            h.id = #{id}
    ''')
    long countHistoryById(int id)

    @Select('''
        SELECT
            id, path, method, data_key, status_code, header, body, body_editor_mode
            , attachment_file, file_name, waiting_msec, description
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
            id
        FROM
            mock_api_response
        WHERE
            path = #{path}
        AND
            method = UPPER(#{method})
        AND
            data_key = IFNULL(#{dataKey},'')
    ''')
    Integer findIdByUk(
            @Param("path") String path, @Param("method") String method, @Param("dataKey") String dataKey)

    @Select('''
        SELECT
            id, path, method, data_key, status_code, header, body, body_editor_mode
            , attachment_file, file_name, waiting_msec, description
            , SELECT COUNT(sub_id) FROM mock_api_response_history WHERE id = #{id} AS historyNumber
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
            (
                path, method, data_key, status_code, header, body, body_editor_mode
                , attachment_file, file_name, waiting_msec, description
            )
        VALUES
            (
                #{path}, UPPER(#{method}), IFNULL(#{dataKey},''), #{statusCode}, #{header}, #{body}, #{bodyEditorMode}
                , #{attachmentFile}, #{fileName}, #{waitingMsec}, #{description}
            )
    ''')
    void create(ApiResponse mockResponse)

    @Insert('''
        INSERT INTO mock_api_response_history
            (
                id, sub_id, status_code, header, body, body_editor_mode
                , attachment_file, file_name, waiting_msec, description, created_at
            )
        SELECT
            id, (SELECT IFNULL(MAX(sub_id), 0) + 1 FROM mock_api_response_history WHERE id = #{id})
            , status_code, header, body, body_editor_mode, attachment_file, file_name, waiting_msec
            , description, CURRENT_TIMESTAMP
        FROM
            mock_api_response
        WHERE
            id = #{id}
    ''')
    void createHistory(int id)

    @Insert('''
        INSERT INTO api_proxy_response
            (
                path, method, data_key, status_code, header, body, attachment_file, file_name
            )
        VALUES
            (
                #{path}, UPPER(#{method}), IFNULL(#{dataKey},''), #{statusCode}, #{header}, #{body}, #{attachmentFile}, #{fileName}
            )
    ''')
    void createProxyResponse(ApiResponse mockResponse)

    @Update('''
        UPDATE mock_api_response
        SET
            data_key = IFNULL(#{dataKey},''), status_code = #{statusCode}, header = #{header}
            , body = #{body}, body_editor_mode = #{bodyEditorMode}, attachment_file = #{attachmentFile}
            , file_name = #{fileName}, waiting_msec = #{waitingMsec}, description = #{description}
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
        public String findPage(
                @Param("path") String path, @Param("method") String method, @Param("description") String description) {
            return new SQL() {
                {
                    SELECT("id", "path", "method", "data_key", "status_code", "description")
                    FROM("mock_api_response")
                    where(this, path, method, description)
                    ORDER_BY("path", "method", "data_key")
                }
            }.toString()
        }
        public String count(
                @Param("path") String path, @Param("method") String method, @Param("description") String description) {
            return new SQL() {
                {
                    SELECT("COUNT(*)")
                    FROM("mock_api_response")
                    where(this, path, method, description)
                }

            }.toString()
        }
        private static void where(SQL sql,  String path, String method, String description) {
            if (StringUtils.hasLength(path)) {
                sql.WHERE("path REGEXP #{path}")
            }
            if (StringUtils.hasLength(method)) {
                sql.WHERE("method = UPPER(#{method})")
            }
            if (StringUtils.hasLength(description)) {
                sql.WHERE("description REGEXP #{description}")
            }
        }
    }

}