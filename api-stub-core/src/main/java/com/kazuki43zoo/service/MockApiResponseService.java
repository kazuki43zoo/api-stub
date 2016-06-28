package com.kazuki43zoo.service;

import com.kazuki43zoo.domain.MockApiResponse;
import com.kazuki43zoo.domain.MockApiResponseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
@Service
public class MockApiResponseService {

    @Autowired
    MockApiResponseRepository repository;

    public MockApiResponse findOne(String path, String method) {
        MockApiResponse mockResponse = repository.findOneByUk(path, method.toLowerCase());
        if (mockResponse == null) {
            mockResponse = new MockApiResponse();
            mockResponse.setPath(path);
            mockResponse.setMethod(method);
        }
        return mockResponse;
    }

    public MockApiResponse findOne(int id) {
        return repository.findOne(id);
    }

    public List<MockApiResponse> findAll(String path, String description) {
        return repository.findAll(path, description);
    }

    public void create(MockApiResponse newMockResponse) {
        newMockResponse.setMethod(newMockResponse.getMethod().toUpperCase());
        repository.create(newMockResponse);
        repository.createHistory(newMockResponse.getId());
    }

    public void update(int id, MockApiResponse newMockResponse, boolean keepAttachmentFile) {
        newMockResponse.setId(id);
        newMockResponse.setMethod(newMockResponse.getMethod().toUpperCase());
        if (keepAttachmentFile) {
            MockApiResponse mockResponse = findOne(id);
            newMockResponse.setAttachmentFile(mockResponse.getAttachmentFile());
            newMockResponse.setFileName(mockResponse.getFileName());
        }
        repository.update(newMockResponse);
        repository.createHistory(id);
    }

    public void delete(int id) {
        repository.delete(id);
    }

}
