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
package com.kazuki43zoo.screen.api;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kazuki43zoo.api.key.KeyExtractor;
import com.kazuki43zoo.component.message.ErrorMessage;
import com.kazuki43zoo.component.message.InfoMessage;
import com.kazuki43zoo.component.message.MessageCode;
import com.kazuki43zoo.component.message.SuccessMessage;
import com.kazuki43zoo.component.pagination.Pagination;
import com.kazuki43zoo.component.download.DownloadSupport;
import com.kazuki43zoo.domain.model.Api;
import com.kazuki43zoo.domain.model.KeyGeneratingStrategy;
import com.kazuki43zoo.domain.service.ApiService;
import com.kazuki43zoo.screen.ImportSupport;
import com.kazuki43zoo.screen.PaginationSupport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.core.Conventions;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.CookieGenerator;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@RequestMapping("/manager/apis")
@Controller
@SessionAttributes(types = ApiSearchForm.class)
@RequiredArgsConstructor
public class ApiController {
    private static final String COOKIE_NAME_PAGE_SIZE = "api.pageSize";
    private static final CookieGenerator pageSizeCookieGenerator;
    static {
        pageSizeCookieGenerator = new CookieGenerator();
        pageSizeCookieGenerator.setCookieName(COOKIE_NAME_PAGE_SIZE);
    }
    private final ApiService service;
    private final List<KeyExtractor> keyExtractors;
    private final ImportSupport importSupport;
    private final PaginationSupport paginationSupport;
    private final DownloadSupport downloadSupport;
    private final ObjectMapper objectMapper;

    @ModelAttribute("apiSearchForm")
    public ApiSearchForm setUpSearchForm() {
        return new ApiSearchForm();
    }

    @ModelAttribute("keyExtractors")
    public List<String> keyExtractors() {
        return keyExtractors.stream()
                .map(Conventions::getVariableName)
                .collect(Collectors.toList());
    }

    @ModelAttribute("keyGeneratingStrategies")
    public List<String> keyGeneratingStrategies() {
        return Stream.of(KeyGeneratingStrategy.values())
                .map(KeyGeneratingStrategy::name)
                .collect(Collectors.toList());
    }

    @GetMapping
    public String list(@Validated ApiSearchForm form, BindingResult result,
                       Pageable pageable,
                       @RequestParam(name = Pagination.PARAM_NAME_SIZE_IN_PAGE, defaultValue = "0") int paramPageSize,
                       @CookieValue(name = COOKIE_NAME_PAGE_SIZE, defaultValue = "0") int cookiePageSize,
                       @RequestParam MultiValueMap<String, String> requestParams,
                       Model model, HttpServletResponse response) {
        int pageSize = paginationSupport.decidePageSize(pageable, paramPageSize, cookiePageSize);
        paginationSupport.storePageSize(pageSize, model, response, pageSizeCookieGenerator);

        if (result.hasErrors()) {
            return "api/list";
        }
        Page<Api> page = service.findAll(form.getPath(), form.getMethod(), form.getDescription(),
                paginationSupport.decidePageable(pageable, pageSize));
        if (page.getContent().isEmpty()) {
            model.addAttribute(
                    InfoMessage.builder().code(MessageCode.DATA_NOT_FOUND).build());
        }
        model.addAttribute(new Pagination(page, requestParams));
        return "api/list";
    }

    @PostMapping(params = "delete")
    public String delete(@RequestParam List<Integer> ids, RedirectAttributes redirectAttributes) {
        service.delete(ids);
        redirectAttributes.addFlashAttribute(
                SuccessMessage.builder().code(MessageCode.DATA_HAS_BEEN_DELETED).build());
        return "redirect:/manager/apis";
    }

    @GetMapping(path = "create")
    public String createForm(Model model) {
        model.addAttribute(new ApiForm());
        return "api/form";
    }


    @PostMapping(path = "create")
    public String create(@Validated ApiForm form, BindingResult result, Model model, RedirectAttributes redirectAttributes) throws JsonProcessingException {
        if (result.hasErrors()) {
            return "api/form";
        }
        Api api = new Api();
        BeanUtils.copyProperties(form, api);
        BeanUtils.copyProperties(form.getProxy(), api.getProxy());
        api.setExpressions(objectMapper.writeValueAsString(form.getExpressions()));
        try {
            service.create(api);
        } catch (DuplicateKeyException e) {
            if (log.isDebugEnabled()) {
                log.debug(e.getMessage(), e);
            }
            model.addAttribute(
                    ErrorMessage.builder().code(MessageCode.DATA_ALREADY_EXISTS).build());
            return "api/form";
        }
        redirectAttributes.addAttribute("id", api.getId());
        redirectAttributes.addFlashAttribute(
                SuccessMessage.builder().code(MessageCode.DATA_HAS_BEEN_CREATED).build());
        return "redirect:/manager/apis/{id}";
    }


    @GetMapping(path = "{id}")
    public String editForm(@PathVariable int id, Model model, RedirectAttributes redirectAttributes) throws IOException {
        Api api = service.findOne(id);
        if (api == null) {
            redirectAttributes.addFlashAttribute(
                    ErrorMessage.builder().code(MessageCode.DATA_NOT_FOUND).build());
            return "redirect:/manager/apis";
        }
        if (api.getKeyedResponseNumber() != 0) {
            model.addAttribute(InfoMessage.builder().code(MessageCode.KEYED_RESPONSE_EXISTS).build());
        }
        ApiForm form = new ApiForm();
        BeanUtils.copyProperties(api, form);
        BeanUtils.copyProperties(api.getProxy(), form.getProxy());
        form.setExpressions(Arrays.asList(objectMapper.readValue(api.getExpressions(), String[].class)));
        model.addAttribute(api);
        model.addAttribute(form);
        return "api/form";
    }


    @PostMapping(path = "{id}", params = "update")
    public String edit(@PathVariable int id, @Validated ApiForm form, BindingResult result, Model model, RedirectAttributes redirectAttributes) throws JsonProcessingException {
        if (result.hasErrors()) {
            model.addAttribute(service.findOne(id));
            return "api/form";
        }
        Api api = new Api();
        BeanUtils.copyProperties(form, api);
        BeanUtils.copyProperties(form.getProxy(), api.getProxy());
        api.setExpressions(objectMapper.writeValueAsString(form.getExpressions()));
        service.update(id, api);
        redirectAttributes.addFlashAttribute(
                SuccessMessage.builder().code(MessageCode.DATA_HAS_BEEN_UPDATED).build());
        return "redirect:/manager/apis/{id}";
    }

    @PostMapping(path = "{id}", params = "delete")
    public String delete(@PathVariable int id, RedirectAttributes redirectAttributes) {
        service.delete(id);
        redirectAttributes.addFlashAttribute(
                SuccessMessage.builder().code(MessageCode.DATA_HAS_BEEN_DELETED).build());
        return "redirect:/manager/apis";
    }

    @PostMapping(params = "export")
    public ResponseEntity<List<Api>> exportApis() throws UnsupportedEncodingException {
        List<Api> apis = service.findAllForExport();
        HttpHeaders headers = new HttpHeaders();
        downloadSupport.addContentDisposition(headers, "exportApis.json");
        return ResponseEntity
                .status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .headers(headers)
                .body(apis);
    }

    @PostMapping(params = "import")
    public String importApis(@RequestParam MultipartFile file, @RequestParam(defaultValue = "false") boolean override, RedirectAttributes redirectAttributes) throws IOException {
        if (!StringUtils.hasLength(file.getOriginalFilename())) {
            redirectAttributes.addFlashAttribute(
                    ErrorMessage.builder().code(MessageCode.IMPORT_FILE_NOT_SELECTED).build());
            return "redirect:/manager/apis";
        }
        if (file.getSize() == 0) {
            redirectAttributes.addFlashAttribute(
                    ErrorMessage.builder().code(MessageCode.IMPORT_FILE_EMPTY).build());
            return "redirect:/manager/apis";
        }
        List<Api> newApis;
        try {
            newApis = Arrays.asList(objectMapper.readValue(file.getInputStream(), Api[].class));
        } catch (JsonParseException | JsonMappingException e) {
            log.warn(e.getMessage(), e);
            redirectAttributes.addFlashAttribute(
                    ErrorMessage.builder().code(MessageCode.IMPORT_FILE_EMPTY).build());
            return "redirect:/manager/apis";
        }
        if (newApis.isEmpty()) {
            redirectAttributes.addFlashAttribute(
                    ErrorMessage.builder().code(MessageCode.IMPORT_FILE_EMPTY).build());
            return "redirect:/manager/apis";
        }

        List<Api> ignoredApis = new ArrayList<>();
        newApis.forEach(newApi -> {
            Integer id = service.findIdByUk(newApi.getPath(), newApi.getMethod());
            if (id == null) {
                service.create(newApi);
            } else if (override) {
                service.update(id, newApi);
            } else {
                ignoredApis.add(newApi);
            }
        });
        importSupport.storeProcessingResultMessages(redirectAttributes, newApis, ignoredApis);
        return "redirect:/manager/apis";
    }

}
