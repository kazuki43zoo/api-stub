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
package com.kazuki43zoo.screen.response;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kazuki43zoo.component.message.ErrorMessage;
import com.kazuki43zoo.component.message.InfoMessage;
import com.kazuki43zoo.component.message.MessageCode;
import com.kazuki43zoo.component.message.SuccessMessage;
import com.kazuki43zoo.component.web.DownloadSupport;
import com.kazuki43zoo.domain.model.Api;
import com.kazuki43zoo.domain.model.ApiResponse;
import com.kazuki43zoo.domain.service.ApiResponseService;
import com.kazuki43zoo.domain.service.ApiService;
import com.kazuki43zoo.screen.ImportHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.CookieGenerator;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@RequestMapping("/manager/responses")
@Controller
@SessionAttributes(types = ApiResponseSearchForm.class)
@RequiredArgsConstructor
class ApiResponseController {
    private static final String COOKIE_NAME_PAGE_SIZE = "apiResponse.pageSize";
    private static final Pageable pageableForExistingCheck = new PageRequest(0, 1);
    private static final CookieGenerator pageSizeCookieGenerator;
    static {
        pageSizeCookieGenerator = new CookieGenerator();
        pageSizeCookieGenerator.setCookieName(COOKIE_NAME_PAGE_SIZE);
    }
    private final ApiResponseService apiResponseService;
    private final ApiService apiService;
    private final ImportHelper importHelper;
    private final DownloadSupport downloadSupport;
    private final ObjectMapper objectMapper;


    @ModelAttribute("apiResponseSearchForm")
    public ApiResponseSearchForm setUpSearchForm() {
        return new ApiResponseSearchForm();
    }

    @GetMapping
    public String list(@Validated ApiResponseSearchForm form, BindingResult result, Pageable pageable,
                       @RequestParam(name = "size", defaultValue = "0") int paramPageSize,
                       @CookieValue(name = COOKIE_NAME_PAGE_SIZE, defaultValue = "0") int cookiePageSize,
                       Model model, HttpServletResponse response) {
        int pageSize = paramPageSize > 0 ? paramPageSize : cookiePageSize;
        pageSize = pageSize > 0 ? pageSize : pageable.getPageSize();
        pageSizeCookieGenerator.addCookie(response, String.valueOf(pageSize));
        model.addAttribute("pageSize", pageSize);

        if (result.hasErrors()) {
            return "response/list";
        }
        Page<ApiResponse> page = apiResponseService.findPage(form.getPath(), form.getMethod(), form.getDescription(),
                new PageRequest(pageable.getPageNumber(), pageSize, pageable.getSort()));
        if (page.getContent().isEmpty()) {
            model.addAttribute(
                    InfoMessage.builder().code(MessageCode.DATA_NOT_FOUND).build());
        }
        model.addAttribute("page", page);
        return "response/list";
    }

    @PostMapping(params = "delete")
    public String delete(@RequestParam List<Integer> ids, RedirectAttributes redirectAttributes) {
        apiResponseService.delete(ids);
        redirectAttributes.addFlashAttribute(
                SuccessMessage.builder().code(MessageCode.DATA_HAS_BEEN_DELETED).build());
        return "redirect:/manager/responses";
    }

    @GetMapping(path = "create")
    public String createForm(Model model) {
        model.addAttribute(new ApiResponseForm());
        return "response/form";
    }

    @PostMapping(path = "create")
    public String create(@Validated ApiResponseForm form, BindingResult result, Model model, RedirectAttributes redirectAttributes) throws IOException {
        if (result.hasErrors()) {
            return "response/form";
        }

        ApiResponse apiResponse = new ApiResponse();
        BeanUtils.copyProperties(form, apiResponse, "body");
        if (form.getFile() != null && StringUtils.hasLength(form.getFile().getOriginalFilename())) {
            apiResponse.setAttachmentFile(form.getFile().getInputStream());
            apiResponse.setFileName(Paths.get(form.getFile().getOriginalFilename()).getFileName().toString());
        } else {
            apiResponse.setBody(new ByteArrayInputStream(form.getBody().getBytes(StandardCharsets.UTF_8)));
        }
        try {
            apiResponseService.create(apiResponse);
        } catch (DuplicateKeyException e) {
            if (log.isDebugEnabled()) {
                log.debug(e.getMessage(), e);
            }
            model.addAttribute(
                    ErrorMessage.builder().code(MessageCode.DATA_ALREADY_EXISTS).build());
            return "response/form";
        }
        redirectAttributes.addAttribute("id", apiResponse.getId());
        redirectAttributes.addFlashAttribute(
                SuccessMessage.builder().code(MessageCode.DATA_HAS_BEEN_CREATED).build());
        return "redirect:/manager/responses/{id}";
    }

    @GetMapping(path = "{id}")
    public String editForm(@PathVariable int id, Model model, RedirectAttributes redirectAttributes) throws IOException {
        ApiResponse apiResponse = apiResponseService.findOne(id);
        if (apiResponse == null) {
            redirectAttributes.addFlashAttribute(
                    ErrorMessage.builder().code(MessageCode.DATA_NOT_FOUND).build());
            return "redirect:/manager/responses";
        }
        ApiResponseForm form = new ApiResponseForm();
        BeanUtils.copyProperties(apiResponse, form, "body");
        if (apiResponse.getBody() != null && apiResponse.getFileName() == null) {
            form.setBody(StreamUtils.copyToString(apiResponse.getBody(), StandardCharsets.UTF_8));
        }
        Api api = apiService.findOne(apiResponse.getPath(), apiResponse.getMethod());

        model.addAttribute(apiResponse);
        model.addAttribute(form);
        if (api != null) {
            model.addAttribute(api);
        }
        return "response/form";
    }

    @PostMapping(path = "{id}", params = "update")
    public String edit(@PathVariable int id, @Validated ApiResponseForm form, BindingResult result, Model model, RedirectAttributes redirectAttributes) throws IOException {
        if (result.hasErrors()) {
            model.addAttribute(apiResponseService.findOne(id));
            return "response/form";
        }
        ApiResponse apiResponse = new ApiResponse();
        BeanUtils.copyProperties(form, apiResponse, "body");
        boolean keepAttachmentFile = false;
        if (form.getFile() != null && StringUtils.hasLength(form.getFile().getOriginalFilename())) {
            apiResponse.setAttachmentFile(form.getFile().getInputStream());
            apiResponse.setFileName(Paths.get(form.getFile().getOriginalFilename()).getFileName().toString());
        } else if (StringUtils.hasLength(form.getBody())) {
            apiResponse.setBody(new ByteArrayInputStream(form.getBody().getBytes(StandardCharsets.UTF_8)));
        } else if (!form.isDeleteFile()) {
            keepAttachmentFile = true;
        }
        apiResponseService.update(id, apiResponse, keepAttachmentFile, form.isSaveHistory());
        redirectAttributes.addFlashAttribute(
                SuccessMessage.builder().code(MessageCode.DATA_HAS_BEEN_UPDATED).build());
        return "redirect:/manager/responses/{id}";
    }

    @PostMapping(path = "{id}", params = "delete")
    public String delete(@PathVariable int id, RedirectAttributes redirectAttributes) {
        apiResponseService.delete(id);
        redirectAttributes.addFlashAttribute(
                SuccessMessage.builder().code(MessageCode.DATA_HAS_BEEN_DELETED).build());
        return "redirect:/manager/responses";
    }


    @GetMapping(path = "{id}/file")
    public ResponseEntity<Resource> download(@PathVariable int id) throws UnsupportedEncodingException {
        return download(apiResponseService.findOne(id));
    }


    @GetMapping(path = "{id}/histories")
    public String histories(@PathVariable int id, Pageable pageable,
                            @CookieValue(name = COOKIE_NAME_PAGE_SIZE, defaultValue = "0") int cookiePageSize,
                            Model model, RedirectAttributes redirectAttributes) {
        ApiResponse apiResponse = apiResponseService.findOne(id);
        if (apiResponse == null) {
            redirectAttributes.addFlashAttribute(
                    ErrorMessage.builder().code(MessageCode.DATA_NOT_FOUND).build());
            return "redirect:/manager/responses";
        }
        Page<ApiResponse> page = apiResponseService.findAllHistoryById(id,
                new PageRequest(pageable.getPageNumber(), cookiePageSize > 0 ? cookiePageSize : pageable.getPageSize(), pageable.getSort()));
        if (page.getContent().isEmpty()) {
            redirectAttributes.addFlashAttribute(
                    ErrorMessage.builder().code(MessageCode.DATA_NOT_FOUND).build());
            return "redirect:/manager/responses/{id}";
        }
        Api api = apiService.findOne(apiResponse.getPath(), apiResponse.getMethod());

        model.addAttribute(apiResponse);
        model.addAttribute("page", page);
        if (api != null) {
            model.addAttribute(api);
        }
        return "response/historyList";
    }

    @PostMapping(path = "{id}/histories", params = "delete")
    public String deleteHistories(@PathVariable int id, @RequestParam List<Integer> subIds, RedirectAttributes redirectAttributes) {
        apiResponseService.deleteHistories(id, subIds);
        redirectAttributes.addFlashAttribute(
                SuccessMessage.builder().code(MessageCode.DATA_HAS_BEEN_DELETED).build());
        if (apiResponseService.findAllHistoryById(id, pageableForExistingCheck).getContent().isEmpty()) {
            return "redirect:/manager/responses/{id}";
        } else {
            return "redirect:/manager/responses/{id}/histories";
        }
    }

    @GetMapping(path = "{id}/histories/{subId}")
    public String history(@PathVariable int id, @PathVariable int subId, Model model, RedirectAttributes redirectAttributes) throws IOException {
        ApiResponse apiResponse = apiResponseService.findHistory(id, subId);
        if (apiResponse == null) {
            redirectAttributes.addFlashAttribute(
                    ErrorMessage.builder().code(MessageCode.DATA_NOT_FOUND).build());
            return "redirect:/manager/responses/{id}/histories";
        }
        ApiResponseForm form = new ApiResponseForm();
        BeanUtils.copyProperties(apiResponse, form, "body");
        if (apiResponse.getBody() != null && apiResponse.getFileName() == null) {
            form.setBody(StreamUtils.copyToString(apiResponse.getBody(), StandardCharsets.UTF_8));
        }
        Api api = apiService.findOne(apiResponse.getPath(), apiResponse.getMethod());
        model.addAttribute(apiResponse);
        model.addAttribute(form);
        if (api != null) {
            model.addAttribute(api);
        }
        return "response/history";
    }


    @PostMapping(path = "{id}/histories/{subId}", params = "restore")
    public String restoreHistory(@PathVariable int id, @PathVariable int subId, RedirectAttributes redirectAttributes) {
        apiResponseService.restoreHistory(id, subId);
        redirectAttributes.addFlashAttribute(
                SuccessMessage.builder().code(MessageCode.DATA_HAS_BEEN_RESTORED).build());
        return "redirect:/manager/responses/{id}";
    }

    @PostMapping(path = "{id}/histories/{subId}", params = "delete")
    public String deleteHistory(@PathVariable int id, @PathVariable int subId, RedirectAttributes redirectAttributes) {
        apiResponseService.deleteHistory(id, subId);
        redirectAttributes.addFlashAttribute(
                SuccessMessage.builder().code(MessageCode.DATA_HAS_BEEN_DELETED).build());
        if (apiResponseService.findAllHistoryById(id, pageableForExistingCheck).getContent().isEmpty()) {
            return "redirect:/manager/responses/{id}";
        } else {
            return "redirect:/manager/responses/{id}/histories";
        }
    }

    @GetMapping(path = "{id}/histories/{subId}/file")
    public ResponseEntity<Resource> download(@PathVariable int id, @PathVariable int subId) throws UnsupportedEncodingException {
        return download(apiResponseService.findHistory(id, subId));
    }

    @PostMapping(params = "export")
    public ResponseEntity<List<ApiResponse>> exportApiResponses(@RequestParam List<Integer> ids) throws UnsupportedEncodingException {
        List<ApiResponse> apiResponses = apiResponseService.findAllForExport(ids);
        HttpHeaders headers = new HttpHeaders();
        downloadSupport.addContentDisposition(headers, "exportApiResponses.json");
        return ResponseEntity
                .status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .headers(headers)
                .body(apiResponses);
    }

    @PostMapping(params = "import")
    public String importApiResponses(@RequestParam MultipartFile file, @RequestParam(defaultValue = "false") boolean override, RedirectAttributes redirectAttributes) throws IOException {
        if (!StringUtils.hasLength(file.getOriginalFilename())) {
            redirectAttributes.addFlashAttribute(
                    ErrorMessage.builder().code(MessageCode.IMPORT_FILE_NOT_SELECTED).build());
            return "redirect:/manager/responses";
        }
        if (file.getSize() == 0) {
            redirectAttributes.addFlashAttribute(
                    ErrorMessage.builder().code(MessageCode.IMPORT_FILE_EMPTY).build());
            return "redirect:/manager/responses";
        }
        List<ApiResponse> newApiResponses;
        try {
            newApiResponses = Arrays.asList(objectMapper.readValue(file.getInputStream(), ApiResponse[].class));
        } catch (JsonParseException | JsonMappingException e) {
            log.warn(e.getMessage(), e);
            redirectAttributes.addFlashAttribute(
                    ErrorMessage.builder().code(MessageCode.IMPORT_FILE_EMPTY).build());
            return "redirect:/manager/responses";
        }
        if (newApiResponses.isEmpty()) {
            redirectAttributes.addFlashAttribute(
                    ErrorMessage.builder().code(MessageCode.IMPORT_FILE_EMPTY).build());
            return "redirect:/manager/responses";
        }

        List<ApiResponse> ignoredApiResponses = new ArrayList<>();
        newApiResponses.forEach(newApiResponse -> {
            Integer id = apiResponseService.findIdByUk(newApiResponse.getPath(), newApiResponse.getMethod(), newApiResponse.getDataKey());
            if (id == null) {
                apiResponseService.create(newApiResponse);
            } else if (override) {
                apiResponseService.update(id, newApiResponse, false, false);
            } else {
                ignoredApiResponses.add(newApiResponse);
            }
        });
        importHelper.storeProcessingResultMessages(redirectAttributes, newApiResponses, ignoredApiResponses);
        return "redirect:/manager/responses";
    }


    private ResponseEntity<Resource> download(ApiResponse apiResponse) throws UnsupportedEncodingException {
        HttpHeaders headers = new HttpHeaders();
        downloadSupport.addContentDisposition(headers, apiResponse.getFileName());
        return ResponseEntity
                .status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .headers(headers)
                .body(new InputStreamResource(apiResponse.getAttachmentFile()));
    }

}
