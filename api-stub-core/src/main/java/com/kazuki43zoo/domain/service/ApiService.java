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
package com.kazuki43zoo.domain.service;

import com.kazuki43zoo.config.ApiStubProperties;
import com.kazuki43zoo.domain.model.Api;
import com.kazuki43zoo.domain.model.ApiResponse;
import com.kazuki43zoo.domain.repository.ApiRepository;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.session.RowBounds;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Transactional
@Service
@RequiredArgsConstructor
public class ApiService {
    private static final Pageable pageableForExport = new PageRequest(0, Integer.MAX_VALUE);
    private final ApiRepository repository;
    private final ApiStubProperties properties;

    public Api findOne(String path, String method) {
        return repository.findOneByUk(path, method);
    }

    public Integer findIdByUk(String path, String method) {
        return repository.findIdByUk(path, method);
    }

    public Api findOne(int id) {
        return repository.findOne(id);
    }

    public Page<Api> findAll(String path, String method, String description, Pageable pageable) {
        long count = repository.count(path, method, description);
        List<Api> content;
        if (count != 0) {
            content = repository.findPage(path, method, description, new RowBounds(pageable.getOffset(), pageable.getPageSize()));
        } else {
            content = Collections.emptyList();
        }
        return new PageImpl<>(content, pageable, count);
    }

    public void create(Api newApi) {
        newApi.setPath(newApi.getPath().replace(properties.getRootPath(), ""));
        repository.create(newApi);
        repository.createProxy(newApi);
    }

    public void update(int id, Api newApi) {
        newApi.setId(id);
        repository.update(newApi);
        if (!repository.updateProxy(newApi)) {
            repository.createProxy(newApi);
        }
    }

    public void delete(List<Integer> ids) {
        ids.forEach(this::delete);
    }

    public void delete(int id) {
        repository.delete(id);
        repository.deleteProxy(id);
    }

    public List<Api> findAllForExport() {
        return findAll(null, null, null, pageableForExport).getContent().stream()
                .map(api -> repository.findOne(api.getId()))
                .collect(Collectors.toList());
    }

}
