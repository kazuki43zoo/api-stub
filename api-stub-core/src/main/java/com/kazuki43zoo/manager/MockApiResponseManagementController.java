package com.kazuki43zoo.manager;

import com.kazuki43zoo.component.DownloadSupport;
import com.kazuki43zoo.domain.model.MockApiResponse;
import com.kazuki43zoo.domain.service.MockApiResponseService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.dao.DuplicateKeyException;
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
        List<MockApiResponse> mockApiResponses = service.findAll(form.getPath(), form.getDescription());
        model.addAttribute("mockApiResponses", mockApiResponses);
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

        MockApiResponse mockApiResponse = new MockApiResponse();
        BeanUtils.copyProperties(form, mockApiResponse, "body");
        if (form.getFile() != null && StringUtils.hasLength(form.getFile().getOriginalFilename())) {
            mockApiResponse.setAttachmentFile(form.getFile().getInputStream());
            mockApiResponse.setFileName(Paths.get(form.getFile().getOriginalFilename()).getFileName().toString());
        } else {
            mockApiResponse.setBody(new ByteArrayInputStream(form.getBody().getBytes(StandardCharsets.UTF_8)));
        }
        try {
            service.create(mockApiResponse);
        } catch (DuplicateKeyException e) {
            return "mock/form";
        }
        redirectAttributes.addAttribute("id", mockApiResponse.getId());
        return "redirect:/manager/mocks/{id}";
    }

    @RequestMapping(path = "{id}", method = RequestMethod.GET)
    public String editForm(@PathVariable int id, Model model) throws IOException {
        MockApiResponse mockApiResponse = service.find(id);
        if (mockApiResponse == null) {
            return "redirect:/manager/mocks";
        }
        MockApiResponseForm form = new MockApiResponseForm();
        BeanUtils.copyProperties(mockApiResponse, form, "body");
        if (mockApiResponse.getBody() != null && mockApiResponse.getFileName() == null) {
            form.setBody(StreamUtils.copyToString(mockApiResponse.getBody(), StandardCharsets.UTF_8));
        }
        model.addAttribute(mockApiResponse);
        model.addAttribute(form);
        return "mock/form";
    }

    @RequestMapping(path = "{id}", method = RequestMethod.POST, params = "update")
    public String edit(@PathVariable int id, @Validated MockApiResponseForm form, BindingResult result, Model model) throws IOException {
        if (result.hasErrors()) {
            model.addAttribute(service.find(id));
            return "mock/form";
        }
        MockApiResponse mockResponse = new MockApiResponse();
        BeanUtils.copyProperties(form, mockResponse, "body");
        boolean keepAttachmentFile = false;
        if (form.getFile() != null && StringUtils.hasLength(form.getFile().getOriginalFilename())) {
            mockResponse.setAttachmentFile(form.getFile().getInputStream());
            mockResponse.setFileName(Paths.get(form.getFile().getOriginalFilename()).getFileName().toString());
        } else if (StringUtils.hasLength(form.getBody())) {
            mockResponse.setBody(new ByteArrayInputStream(form.getBody().getBytes(StandardCharsets.UTF_8)));
        } else if (!form.isDeleteFile()) {
            keepAttachmentFile = true;
        }
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
        return download(service.find(id));
    }


    @RequestMapping(path = "{id}/histories", method = RequestMethod.GET)
    public String histories(@PathVariable int id, Model model) {
        MockApiResponse mockApiResponse = service.find(id);
        if (mockApiResponse == null) {
            return "redirect:/manager/mocks";
        }
        List<MockApiResponse> mockApiResponses = service.findAllHistoryById(id);
        model.addAttribute(mockApiResponse);
        model.addAttribute("mockApiResponses", mockApiResponses);
        return "mock/historyList";
    }

    @RequestMapping(path = "{id}/histories/{subId}", method = RequestMethod.GET)
    public String history(@PathVariable int id, @PathVariable int subId, Model model, RedirectAttributes redirectAttributes) throws IOException {
        MockApiResponse mockApiResponse = service.findHistory(id, subId);
        if (mockApiResponse == null) {
            redirectAttributes.addAttribute("id", id);
            return "redirect:/manager/mocks/{id}";
        }
        MockApiResponseForm form = new MockApiResponseForm();
        BeanUtils.copyProperties(mockApiResponse, form, "body");
        if (mockApiResponse.getBody() != null && mockApiResponse.getFileName() == null) {
            form.setBody(StreamUtils.copyToString(mockApiResponse.getBody(), StandardCharsets.UTF_8));
        }
        model.addAttribute(mockApiResponse);
        model.addAttribute(form);
        return "mock/history";
    }


    @RequestMapping(path = "{id}/histories/{subId}", method = RequestMethod.POST, params = "restore")
    public String restoreHistory(@PathVariable int id, @PathVariable int subId) throws IOException {
        service.restoreHistory(id, subId);
        return "redirect:/manager/mocks/{id}";
    }

    @RequestMapping(path = "{id}/histories/{subId}", method = RequestMethod.POST, params = "delete")
    public String deleteHistory(@PathVariable int id, @PathVariable int subId) throws IOException {
        service.deleteHistory(id, subId);
        return "redirect:/manager/mocks/{id}/histories";
    }


    @RequestMapping(path = "{id}/histories/{subId}/file", method = RequestMethod.GET)
    public ResponseEntity<Resource> download(@PathVariable int id, @PathVariable int subId) throws UnsupportedEncodingException {
        return download(service.findHistory(id, subId));
    }

    private ResponseEntity<Resource> download(MockApiResponse mockApiResponse) throws UnsupportedEncodingException {
        HttpHeaders headers = new HttpHeaders();
        downloadSupport.addContentDisposition(headers, mockApiResponse.getFileName());
        return ResponseEntity
                .status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .headers(headers)
                .body(new InputStreamResource(mockApiResponse.getAttachmentFile()));
    }

}
