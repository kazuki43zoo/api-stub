/**
 *    Copyright 2016 the original author or authors.
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

import com.kazuki43zoo.domain.model.Api;
import com.kazuki43zoo.domain.repository.ApiRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
@Service
public class ApiService {

    @Autowired
    ApiRepository repository;

    public Api findOne(String path, String method) {
        return repository.findOneByUk(path, method);
    }

    public Api findOne(int id) {
        return repository.findOne(id);
    }

    public List<Api> findAll(String path, String method, String description) {
        return repository.findAll(path, method, description);
    }

    public void create(Api newApi) {
        repository.create(newApi);
    }

    public void update(int id, Api newApi) {
        newApi.setId(id);
        repository.update(newApi);
    }

    public void delete(List<Integer> ids) {
        ids.forEach(this::delete);
    }

    public void delete(int id) {
        repository.delete(id);
    }

}
