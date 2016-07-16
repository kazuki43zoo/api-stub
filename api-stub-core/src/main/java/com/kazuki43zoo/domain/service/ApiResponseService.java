package com.kazuki43zoo.domain.service;

import com.kazuki43zoo.domain.model.ApiResponse;
import com.kazuki43zoo.domain.repository.ApiResponseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
@Service
public class ApiResponseService {

    @Autowired
    ApiResponseRepository repository;

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

    public List<ApiResponse> findAll(String path, String method, String description) {
        return repository.findAll(path, method, description);
    }

    public List<ApiResponse> findAllHistoryById(int id) {
        return repository.findAllHistoryById(id);
    }

    public ApiResponse findHistory(int id, int subId) {
        return repository.findHistory(id, subId);
    }

    public void create(ApiResponse newMockResponse) {
        repository.create(newMockResponse);
        repository.createHistory(newMockResponse.getId());
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

    public void deleteHistory(int id, int subId) {
        repository.deleteHistory(id, subId);
    }

}
