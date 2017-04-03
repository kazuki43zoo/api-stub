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

import com.kazuki43zoo.domain.model.Api
import org.apache.ibatis.annotations.*
import org.apache.ibatis.jdbc.SQL
import org.springframework.util.StringUtils

//language=SQL
@Mapper
interface ApiRepository {

    @SelectProvider(type = SqlProvider.class, method = "findAll")
    List<Api> findAll(
            @Param("path") String path, @Param("method") String method, @Param("description") String description)

    @Select('''
        SELECT
            a.id, a.path, a.method, a.key_extractor, a.key_generating_strategy, a.expressions, a.description
            , SELECT COUNT(r.id) FROM mock_api_response r
                  WHERE r.path = a.path AND r.method = a.method AND r.data_key NOT IN ('', 'default') AS keyed_response_number
            , p.enabled, p.url, p.capturing
        FROM
            mock_api a
        LEFT OUTER JOIN
            api_proxy p ON p.id = a.id
        WHERE
            a.id = #{id}
    ''')
    @Results([
            @Result(property = "proxy.enabled" ,column = "enabled")
            , @Result(property = "proxy.url" ,column = "url")
            , @Result(property = "proxy.capturing" ,column = "capturing")
    ])
    Api findOne(int id)

    @Select('''
        SELECT
            a.id, a.path, a.method, a.key_extractor, a.key_generating_strategy, a.expressions, a.description
            , p.enabled, p.url, p.capturing
        FROM
            mock_api a
        LEFT OUTER JOIN
            api_proxy p ON p.id = a.id
        WHERE
            a.path = #{path}
        AND
            a.method = UPPER(#{method})
    ''')
    @Results([
        @Result(property = "proxy.enabled" ,column = "enabled")
        , @Result(property = "proxy.url" ,column = "url")
        , @Result(property = "proxy.capturing" ,column = "capturing")
    ])
    Api findOneByUk(@Param("path") String path, @Param("method") String method)

    @Select('''
        SELECT
            id
        FROM
            mock_api
        WHERE
            path = #{path}
        AND
            method = UPPER(#{method})
    ''')
    Integer findIdByUk(@Param("path") String path, @Param("method") String method)

    @Insert('''
        INSERT INTO mock_api
            (
                path, method, key_extractor, key_generating_strategy, expressions, description
            )
        VALUES
            (
                #{path}, UPPER(#{method}), #{keyExtractor}, #{keyGeneratingStrategy}, #{expressions}, #{description}
            )
    ''')
    void create(Api api)

    @Insert('''
        INSERT INTO api_proxy
            (
                id, enabled, url, capturing
            )
        VALUES
            (
                #{id}, IFNULL(#{proxy.enabled},false), #{proxy.url}, IFNULL(#{proxy.capturing},false)
            )
    ''')
    void createProxy(Api api)

    @Update('''
        UPDATE mock_api
        SET
            description = #{description}, key_extractor = #{keyExtractor}
            , key_generating_strategy = #{keyGeneratingStrategy}, expressions = #{expressions}
        WHERE
            id = #{id}
    ''')
    void update(Api newApi)

    @Update('''
        UPDATE api_proxy
        SET
            enabled = IFNULL(#{proxy.enabled},false), url = #{proxy.url}, capturing = IFNULL(#{proxy.capturing},false)
        WHERE
            id = #{id}
    ''')
    boolean updateProxy(Api newApi)

    @Delete('''
        DELETE FROM
           mock_api
        WHERE
            id = #{id}
    ''')
    void delete(int id)

    @Delete('''
        DELETE FROM
           api_proxy
        WHERE
            id = #{id}
    ''')
    void deleteProxy(int id)

    static class SqlProvider {
        public String findAll(
                @Param("path") String path, @Param("method") String method, @Param("description") String description) {
            return new SQL() {
                {
                    SELECT("a.id").SELECT("a.path").SELECT("a.method").SELECT("a.description")
                            .SELECT("SELECT COUNT(r.id) FROM mock_api_response r WHERE r.path = a.path AND r.method = a.method AND r.data_key NOT IN ('', 'default') AS keyed_response_number")
                    FROM("mock_api a")
                    if (StringUtils.hasLength(path)) {
                        WHERE("a.path REGEXP #{path}")
                    }
                    if (StringUtils.hasLength(method)) {
                        WHERE("a.method = UPPER(#{method})")
                    }
                    if (StringUtils.hasLength(description)) {
                        WHERE("a.description REGEXP #{description}")
                    }
                    ORDER_BY("a.path").ORDER_BY("a.method")
                }
            }.toString()
        }
    }

}