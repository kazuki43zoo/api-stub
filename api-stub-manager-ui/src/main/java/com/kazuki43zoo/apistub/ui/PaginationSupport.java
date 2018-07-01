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
package com.kazuki43zoo.apistub.ui;

import com.kazuki43zoo.apistub.ui.component.pagination.Pagination;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import org.springframework.web.util.CookieGenerator;

import javax.servlet.http.HttpServletResponse;

@Component
public class PaginationSupport {

  public int decidePageSize(Pageable pageable, int paramPageSize, int cookiePageSize) {
    int pageSize = paramPageSize > 0 ? paramPageSize : cookiePageSize;
    return pageSize > 0 ? pageSize : pageable.getPageSize();
  }

  public void storePageSize(int pageSize, Model model, HttpServletResponse response, CookieGenerator generator) {
    generator.addCookie(response, String.valueOf(pageSize));
    model.addAttribute(Pagination.ATTR_NAME_SIZE_IN_PAGE, pageSize);
  }

  public Pageable decidePageable(Pageable pageable, int pageSize) {
    if (pageable.getPageSize() == pageSize) {
      return pageable;
    }
    return PageRequest.of(pageable.getPageNumber(), pageSize, pageable.getSort());
  }

}
