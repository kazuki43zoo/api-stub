package com.kazuki43zoo.repository;

import com.kazuki43zoo.domain.MockApi;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface MockApiRepository {

    @Select("SELECT id, path, method, content_type, description FROM mock_api WHERE path = #{path} AND method = #{method}")
    MockApi findByUk(@Param("path") String path, @Param("method") String method);

}
