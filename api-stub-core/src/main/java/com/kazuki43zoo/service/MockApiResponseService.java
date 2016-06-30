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

    public MockApiResponse find(String path, String method) {
        MockApiResponse mockResponse = repository.findOneByUk(path, method.toUpperCase());
        if (mockResponse == null) {
            mockResponse = new MockApiResponse();
            mockResponse.setPath(path);
            mockResponse.setMethod(method);
        }
        return mockResponse;
    }

    public MockApiResponse find(int id) {
        return repository.find(id);
    }

    public List<MockApiResponse> findAll(String path, String description) {
        return repository.findAll(path, description);
    }

    public List<MockApiResponse> findAllHistoryById(int id) {
        return repository.findAllHistoryById(id);
    }

    public MockApiResponse findHistory(int id, int subId) {
        return repository.findHistory(id, subId);
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
            MockApiResponse mockResponse = find(id);
            newMockResponse.setAttachmentFile(mockResponse.getAttachmentFile());
            newMockResponse.setFileName(mockResponse.getFileName());
        }
        repository.update(newMockResponse);
        repository.createHistory(id);
    }


    public void restoreHistory(int id, int subId) {
        MockApiResponse history = repository.findHistory(id, subId);
        MockApiResponse target = repository.find(id);
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
