package com.kazuki43zoo.manager.response;

import com.kazuki43zoo.component.DownloadSupport;
import com.kazuki43zoo.domain.model.Api;
import com.kazuki43zoo.domain.model.ApiResponse;
import com.kazuki43zoo.domain.service.ApiResponseService;
import com.kazuki43zoo.domain.service.ApiService;
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

@RequestMapping("/manager/responses")
@Controller
@SessionAttributes(types = ApiResponseSearchForm.class)
class ApiResponseManagementController {

    @Autowired
    ApiResponseService apiResponseService;

    @Autowired
    ApiService apiService;

    @Autowired
    DownloadSupport downloadSupport;

    @ModelAttribute("apiResponseSearchForm")
    public ApiResponseSearchForm setUpSearchForm() {
        return new ApiResponseSearchForm();
    }

    @RequestMapping(method = RequestMethod.GET)
    public String list(@Validated ApiResponseSearchForm form, BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "response/list";
        }
        List<ApiResponse> apiResponses = apiResponseService.findAll(form.getPath(), form.getMethod(), form.getDescription());
        model.addAttribute("apiResponses", apiResponses);
        return "response/list";
    }

    @RequestMapping(path = "create", method = RequestMethod.GET)
    public String createForm(Model model) {
        model.addAttribute(new ApiResponseForm());
        return "response/form";
    }

    @RequestMapping(path = "create", method = RequestMethod.POST)
    public String create(@Validated ApiResponseForm form, BindingResult result, RedirectAttributes redirectAttributes) throws IOException {
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
            return "response/form";
        }
        redirectAttributes.addAttribute("id", apiResponse.getId());
        return "redirect:/manager/responses/{id}";
    }

    @RequestMapping(path = "{id}", method = RequestMethod.GET)
    public String editForm(@PathVariable int id, Model model) throws IOException {
        ApiResponse apiResponse = apiResponseService.findOne(id);
        if (apiResponse == null) {
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

    @RequestMapping(path = "{id}", method = RequestMethod.POST, params = "update")
    public String edit(@PathVariable int id, @Validated ApiResponseForm form, BindingResult result, Model model) throws IOException {
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
        apiResponseService.update(id, apiResponse, keepAttachmentFile);
        return "redirect:/manager/responses/{id}";
    }

    @RequestMapping(path = "{id}", method = RequestMethod.POST, params = "delete")
    public String delete(@PathVariable int id) {
        apiResponseService.delete(id);
        return "redirect:/manager/responses";
    }


    @RequestMapping(path = "{id}/file", method = RequestMethod.GET)
    public ResponseEntity<Resource> download(@PathVariable int id) throws UnsupportedEncodingException {
        return download(apiResponseService.findOne(id));
    }


    @RequestMapping(path = "{id}/histories", method = RequestMethod.GET)
    public String histories(@PathVariable int id, Model model) {
        ApiResponse apiResponse = apiResponseService.findOne(id);
        if (apiResponse == null) {
            return "redirect:/manager/responses";
        }
        List<ApiResponse> apiResponses = apiResponseService.findAllHistoryById(id);
        Api api = apiService.findOne(apiResponse.getPath(), apiResponse.getMethod());

        model.addAttribute(apiResponse);
        model.addAttribute("apiResponses", apiResponses);
        if (api != null) {
            model.addAttribute(api);
        }
        return "response/historyList";
    }

    @RequestMapping(path = "{id}/histories/{subId}", method = RequestMethod.GET)
    public String history(@PathVariable int id, @PathVariable int subId, Model model, RedirectAttributes redirectAttributes) throws IOException {
        ApiResponse apiResponse = apiResponseService.findHistory(id, subId);
        if (apiResponse == null) {
            redirectAttributes.addAttribute("id", id);
            return "redirect:/manager/responses/{id}";
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


    @RequestMapping(path = "{id}/histories/{subId}", method = RequestMethod.POST, params = "restore")
    public String restoreHistory(@PathVariable int id, @PathVariable int subId) throws IOException {
        apiResponseService.restoreHistory(id, subId);
        return "redirect:/manager/responses/{id}";
    }

    @RequestMapping(path = "{id}/histories/{subId}", method = RequestMethod.POST, params = "delete")
    public String deleteHistory(@PathVariable int id, @PathVariable int subId) throws IOException {
        apiResponseService.deleteHistory(id, subId);
        return "redirect:/manager/responses/{id}/histories";
    }


    @RequestMapping(path = "{id}/histories/{subId}/file", method = RequestMethod.GET)
    public ResponseEntity<Resource> download(@PathVariable int id, @PathVariable int subId) throws UnsupportedEncodingException {
        return download(apiResponseService.findHistory(id, subId));
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
