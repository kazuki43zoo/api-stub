package com.kazuki43zoo.service;

import com.kazuki43zoo.domain.MockApi;
import com.kazuki43zoo.domain.MockApiRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Service
public class MockApiService {

    @Autowired
    MockApiRepository repository;

    public MockApi findOneBy(String path, String method) {
        return repository.findOneByUk(path, method.toUpperCase());
    }

}
