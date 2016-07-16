package com.kazuki43zoo.domain.repository

import com.kazuki43zoo.domain.model.Api
import org.apache.ibatis.annotations.*
import org.apache.ibatis.jdbc.SQL
import org.springframework.util.StringUtils

@Mapper
interface ApiRepository {

    @SelectProvider(type = SqlProvider.class, method = "findAll")
    List<Api> findAll(
            @Param("path") String path, @Param("method") String method, @Param("description") String description)

    @Select('''
        SELECT
            id, path, method, key_extractor, key_generating_strategy, expressions, description
        FROM
            mock_api
        WHERE
            id = #{id}
    ''')
    Api findOne(int id)

    @Select('''
        SELECT
            id, path, method, key_extractor, key_generating_strategy, expressions, description
        FROM
            mock_api
        WHERE
            path = #{path}
        AND
            method = UPPER(#{method})
    ''')
    Api findOneByUk(@Param("path") String path, @Param("method") String method)

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
    @Options(useGeneratedKeys = true)
    void create(Api api)

    @Update('''
        UPDATE mock_api
        SET
            description = #{description}, key_extractor = #{keyExtractor}
            , key_generating_strategy = #{keyGeneratingStrategy}, expressions = #{expressions}
        WHERE
            id = #{id}
    ''')
    void update(Api newApi)

    @Delete('''
        DELETE FROM
           mock_api
        WHERE
            id = #{id}
    ''')
    void delete(int id)

    static class SqlProvider {
        public String findAll(
                @Param("path") String path, @Param("method") String method, @Param("description") String description) {
            return new SQL() {
                {
                    SELECT("id").SELECT("path").SELECT("method").SELECT("description")
                    FROM("mock_api")
                    if (StringUtils.hasLength(path)) {
                        WHERE("path REGEXP #{path}")
                    }
                    if (StringUtils.hasLength(method)) {
                        WHERE("method = UPPER(#{method})")
                    }
                    if (StringUtils.hasLength(description)) {
                        WHERE("description REGEXP #{description}")
                    }
                    ORDER_BY("path").ORDER_BY("method")
                }
            }.toString()
        }
    }

}