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
package com.kazuki43zoo.component.pagination;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Page;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

@Getter
public class Pagination {

    public static final String ATTR_NAME_SIZE_IN_PAGE = "pageSize";
    public static final String PARAM_NAME_SIZE_IN_PAGE = "size";
    private static final String PARAM_NAME_PAGE_POSITION = "page";

    private final Page<?> page;
    private final String query;

    public Pagination(Page<?> page, MultiValueMap<String, String> params) {
        this.page = page;
        String query = null;
        if (params != null) {
            params.remove(PARAM_NAME_PAGE_POSITION);
            query = UriComponentsBuilder.fromPath("").queryParams(params).build().encode().getQuery();
        }
        this.query = (query != null) ? ("?" + query) : "";
    }

    public Range getRange(int maxSize) {
        int begin = Math.max(0, page.getNumber() - maxSize / 2);
        int end = begin + (maxSize - 1);
        if (end > page.getTotalPages() - 1) {
            end = page.getTotalPages() - 1;
            begin = Math.max(0, end - (maxSize - 1));
        }
        return new Range(begin, end);
    }

    @RequiredArgsConstructor
    @Getter
    @Setter
    public static class Range {
        private final int begin;
        private final int end;
    }

}
