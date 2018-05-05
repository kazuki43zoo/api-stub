/*
 *    Copyright 2016-2018 the original author or authors.
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
import com.kazuki43zoo.domain.model.ApiResponse;
import com.kazuki43zoo.domain.repository.ApiResponseRepository;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.session.RowBounds;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Transactional
@Service
@RequiredArgsConstructor
public class ApiResponseService {

    private final ApiResponseRepository repository;
    private final ApiStubProperties properties;

    public ApiResponse findOne(String path, String method, String dataKey) {
        ApiResponse mockResponse = repository.findOneByUk(path, method, dataKey);
        if (mockResponse == null) {
            mockResponse = repository.findOneByUk(path, method, "default");
        }
        if (mockResponse == null) {
            mockResponse = new ApiResponse();
            mockResponse.setPath(path);
            mockResponse.setMethod(method);
        }
        return mockResponse;
    }

    public ApiResponse findOne(int id) {
        return repository.findOne(id);
    }

    public Integer findIdByUk(String path, String method, String dataKey) {
        return repository.findIdByUk(path, method, dataKey);
    }

    public Page<ApiResponse> findPage(String path, String method, String description, Pageable pageable) {
        String searchingPath = Optional.ofNullable(pageable)
            .map(e -> path.replaceAll("\\{.+\\}", ".+")).orElse(null);
        long count = repository.count(searchingPath, method, description);
        List<ApiResponse> content;
        if (count != 0) {
            content = repository.findPage(searchingPath, method, description, new RowBounds(Long.valueOf(pageable.getOffset()).intValue(), Long.valueOf(pageable.getPageSize()).intValue()));
        } else {
            content = Collections.emptyList();
        }
        return new PageImpl<>(content, pageable, count);
    }

    public Page<ApiResponse> findAllHistoryById(int id, Pageable pageable) {
        long count = repository.countHistoryById(id);
        List<ApiResponse> content;
        if (count != 0) {
            content = repository.findPageHistoryById(id, new RowBounds(Long.valueOf(pageable.getOffset()).intValue(), Long.valueOf(pageable.getPageSize()).intValue()));
        } else {
            content = Collections.emptyList();
        }
        return new PageImpl<>(content, pageable, count);
    }

    public ApiResponse findHistory(int id, int subId) {
        return repository.findHistory(id, subId);
    }

    public void create(ApiResponse newMockResponse) {
        newMockResponse.setPath(newMockResponse.getPath().replace(properties.getRootPath(), ""));
        repository.create(newMockResponse);
        repository.createHistory(newMockResponse.getId());
    }

    public void createProxyResponse(ApiResponse newMockResponse) {
        newMockResponse.setPath(newMockResponse.getPath().replace(properties.getRootPath(), ""));
        repository.createProxyResponse(newMockResponse);
    }

    public void update(int id, ApiResponse newMockResponse, boolean keepAttachmentFile, boolean saveHistory) {
        newMockResponse.setId(id);
        if (keepAttachmentFile) {
            ApiResponse mockResponse = findOne(id);
            newMockResponse.setAttachmentFile(mockResponse.getAttachmentFile());
            newMockResponse.setFileName(mockResponse.getFileName());
        }
        repository.update(newMockResponse);
        if (saveHistory) {
            repository.createHistory(id);
        }
    }

    public void restoreHistory(int id, int subId) {
        ApiResponse history = repository.findHistory(id, subId);
        ApiResponse target = repository.findOne(id);
        target.setStatusCode(history.getStatusCode());
        target.setHeader(history.getHeader());
        target.setBody(history.getBody());
        target.setAttachmentFile(history.getAttachmentFile());
        target.setFileName(history.getFileName());
        target.setDescription(history.getDescription());
        repository.update(target);
    }

    public void delete(int id) {
        repository.delete(id);
        repository.deleteAllHistory(id);
    }

    public void delete(List<Integer> ids) {
        ids.forEach(this::delete);
    }

    public void deleteHistory(int id, int subId) {
        repository.deleteHistory(id, subId);
    }

    public void deleteHistories(int id, List<Integer> subIds) {
        subIds.forEach(subId -> deleteHistory(id, subId));
    }

    public List<ApiResponse> findAllForExport(List<Integer> ids) {
        return ids.stream()
                .map(repository::findOne)
                .collect(Collectors.toList());
    }

}
