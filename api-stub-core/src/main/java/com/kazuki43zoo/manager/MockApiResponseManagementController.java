package com.kazuki43zoo.manager;

import com.kazuki43zoo.component.DownloadSupport;
import com.kazuki43zoo.domain.MockApiResponse;
import com.kazuki43zoo.service.MockApiResponseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.List;

@RequestMapping("/manager/mocks")
@Controller
@SessionAttributes(types = MockApiResponseSearchForm.class)
public class MockApiResponseManagementController {

    @Autowired
    MockApiResponseService service;

    @Autowired
    DownloadSupport downloadSupport;

    @ModelAttribute("mockApiResponseSearchForm")
    public MockApiResponseSearchForm setUpSearchForm() {
        return new MockApiResponseSearchForm();
    }

    @RequestMapping(method = RequestMethod.GET)
    public String list(MockApiResponseSearchForm form, BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "mock/list";
        }
        List<MockApiResponse> mockResponses = service.findAll(form.getPath(), form.getDescription());
        model.addAttribute("mockResponses", mockResponses);
        return "mock/list";
    }

    @RequestMapping(path = "create", method = RequestMethod.GET)
    public String createForm(Model model) {
        model.addAttribute(new MockApiResponseForm());
        return "mock/form";
    }

    @RequestMapping(path = "create", method = RequestMethod.POST)
    public String create(@Validated MockApiResponseForm form, BindingResult result, RedirectAttributes redirectAttributes) throws IOException {
        if (result.hasErrors()) {
            return "mock/form";
        }
        MockApiResponse mockResponse = new MockApiResponse();
        mockResponse.setPath(form.getPath());
        mockResponse.setMethod(form.getMethod());
        mockResponse.setStatusCode(form.getStatusCode());
        mockResponse.setHeader(form.getHeader());
        if (form.getFile() != null && StringUtils.hasLength(form.getFile().getOriginalFilename())) {
            mockResponse.setAttachmentFile(form.getFile().getInputStream());
            mockResponse.setFileName(Paths.get(form.getFile().getOriginalFilename()).getFileName().toString());
        } else {
            mockResponse.setBody(new ByteArrayInputStream(form.getBody().getBytes(StandardCharsets.UTF_8)));
        }
        mockResponse.setWaitingMsec(form.getWaitingMsec());
        mockResponse.setDescription(form.getDescription());
        service.create(mockResponse);
        redirectAttributes.addAttribute("id", mockResponse.getId());
        return "redirect:/manager/mocks/{id}";
    }

    @RequestMapping(path = "{id}", method = RequestMethod.GET)
    public String editForm(@PathVariable int id, Model model) throws IOException {
        MockApiResponse mockResponse = service.findOne(id);
        if (mockResponse == null) {
            return "redirect:/manager/mocks";
        }
        MockApiResponseForm form = new MockApiResponseForm();
        form.setPath(mockResponse.getPath());
        form.setMethod(mockResponse.getMethod());
        form.setStatusCode(mockResponse.getStatusCode());
        form.setHeader(mockResponse.getHeader());
        if (mockResponse.getBody() != null && mockResponse.getFileName() == null) {
            form.setBody(StreamUtils.copyToString(mockResponse.getBody(), StandardCharsets.UTF_8));
        }
        form.setWaitingMsec(mockResponse.getWaitingMsec());
        form.setDescription(mockResponse.getDescription());
        model.addAttribute(mockResponse);
        model.addAttribute(form);
        return "mock/form";
    }

    @RequestMapping(path = "{id}", method = RequestMethod.POST, params = "update")
    public String edit(@PathVariable int id, @Validated MockApiResponseForm form, BindingResult result, Model model) throws IOException {
        if (result.hasErrors()) {
            model.addAttribute(service.findOne(id));
            return "mock/form";
        }
        MockApiResponse mockResponse = new MockApiResponse();
        mockResponse.setPath(form.getPath());
        mockResponse.setMethod(form.getMethod());
        mockResponse.setStatusCode(form.getStatusCode());
        mockResponse.setHeader(form.getHeader());
        boolean keepAttachmentFile = false;
        if (form.getFile() != null && StringUtils.hasLength(form.getFile().getOriginalFilename())) {
            mockResponse.setAttachmentFile(form.getFile().getInputStream());
            mockResponse.setFileName(Paths.get(form.getFile().getOriginalFilename()).getFileName().toString());
        } else if (StringUtils.hasLength(form.getBody())) {
            mockResponse.setBody(new ByteArrayInputStream(form.getBody().getBytes(StandardCharsets.UTF_8)));
        } else if (!form.isDeleteFile()) {
            keepAttachmentFile = true;
        }
        mockResponse.setWaitingMsec(form.getWaitingMsec());
        mockResponse.setDescription(form.getDescription());
        service.update(id, mockResponse, keepAttachmentFile);
        return "redirect:/manager/mocks/{id}";
    }

    @RequestMapping(path = "{id}", method = RequestMethod.POST, params = "delete")
    public String delete(@PathVariable int id) {
        service.delete(id);
        return "redirect:/manager/mocks";
    }


    @RequestMapping(path = "{id}/file", method = RequestMethod.GET)
    public ResponseEntity<Resource> download(@PathVariable int id) throws UnsupportedEncodingException {
        MockApiResponse mockResponse = service.findOne(id);
        HttpHeaders headers = new HttpHeaders();
        downloadSupport.addContentDisposition(headers, mockResponse.getFileName());
        return ResponseEntity
                .status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .headers(headers)
                .body(new InputStreamResource(mockResponse.getAttachmentFile()));
    }

}
