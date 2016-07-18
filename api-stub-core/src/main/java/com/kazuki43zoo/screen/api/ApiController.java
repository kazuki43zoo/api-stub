package com.kazuki43zoo.screen.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kazuki43zoo.api.key.KeyExtractor;
import com.kazuki43zoo.component.message.ErrorMessage;
import com.kazuki43zoo.component.message.InfoMessage;
import com.kazuki43zoo.component.message.MessageCode;
import com.kazuki43zoo.component.message.SuccessMessage;
import com.kazuki43zoo.domain.model.Api;
import com.kazuki43zoo.domain.model.KeyGeneratingStrategy;
import com.kazuki43zoo.domain.service.ApiService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.Conventions;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequestMapping("/manager/apis")
@Controller
@SessionAttributes(types = ApiSearchForm.class)
public class ApiController {

    @Autowired
    ApiService service;

    @Autowired
    ApplicationContext applicationContext;

    @Autowired(required = false)
    List<KeyExtractor> keyExtractors;

    @Autowired
    ObjectMapper jsonObjectMapper;

    @ModelAttribute("apiSearchForm")
    public ApiSearchForm setUpSearchForm() {
        return new ApiSearchForm();
    }

    @ModelAttribute("keyExtractors")
    public List<String> keyExtractors() {
        return keyExtractors.stream().map(Conventions::getVariableName).collect(Collectors.toList());
    }

    @ModelAttribute("keyGeneratingStrategies")
    public List<String> keyGeneratingStrategies() {
        return Stream.of(KeyGeneratingStrategy.values())
                .map(KeyGeneratingStrategy::name)
                .collect(Collectors.toList());
    }

    @RequestMapping(method = RequestMethod.GET)
    public String list(@Validated ApiSearchForm form, BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "api/list";
        }
        List<Api> apis = service.findAll(form.getPath(), form.getMethod(), form.getDescription());
        if (apis.isEmpty()) {
            model.addAttribute(ErrorMessage.builder().code(MessageCode.DATA_NOT_FOUND).build());
        }
        model.addAttribute(apis);
        return "api/list";
    }

    @RequestMapping(method = RequestMethod.POST, params = "delete")
    public String delete(@RequestParam List<Integer> ids, RedirectAttributes redirectAttributes) {
        service.delete(ids);
        redirectAttributes.addFlashAttribute(SuccessMessage.builder().code(MessageCode.DATA_HAS_BEEN_DELETED).build());
        return "redirect:/manager/apis";
    }

    @RequestMapping(path = "create", method = RequestMethod.GET)
    public String createForm(Model model) {
        model.addAttribute(new ApiForm());
        return "api/form";
    }


    @RequestMapping(path = "create", method = RequestMethod.POST)
    public String create(@Validated ApiForm form, BindingResult result, Model model, RedirectAttributes redirectAttributes) throws JsonProcessingException {
        if (result.hasErrors()) {
            return "api/form";
        }
        Api api = new Api();
        BeanUtils.copyProperties(form, api);
        api.setExpressions(jsonObjectMapper.writeValueAsString(form.getExpressions()));
        try {
            service.create(api);
        } catch (DuplicateKeyException e) {
            model.addAttribute(ErrorMessage.builder().code(MessageCode.DATA_ALREADY_EXISTS).build());
            return "api/form";
        }
        redirectAttributes.addAttribute("id", api.getId());
        redirectAttributes.addFlashAttribute(SuccessMessage.builder().code(MessageCode.DATA_HAS_BEEN_CREATED).build());
        return "redirect:/manager/apis/{id}";
    }


    @RequestMapping(path = "{id}", method = RequestMethod.GET)
    public String editForm(@PathVariable int id, Model model, RedirectAttributes redirectAttributes) throws IOException {
        Api api = service.findOne(id);
        if (api == null) {
            redirectAttributes.addFlashAttribute(ErrorMessage.builder().code(MessageCode.DATA_NOT_FOUND).build());
            return "redirect:/manager/apis";
        }
        if (api.getKeyedResponseNumber() != 0) {
            model.addAttribute(InfoMessage.builder().code(MessageCode.KEYED_RESPONSE_EXISTS).build());
        }
        ApiForm form = new ApiForm();
        BeanUtils.copyProperties(api, form);
        form.setExpressions(Arrays.asList(jsonObjectMapper.readValue(api.getExpressions(), String[].class)));
        model.addAttribute(api);
        model.addAttribute(form);
        return "api/form";
    }


    @RequestMapping(path = "{id}", method = RequestMethod.POST, params = "update")
    public String edit(@PathVariable int id, @Validated ApiForm form, BindingResult result, Model model, RedirectAttributes redirectAttributes) throws JsonProcessingException {
        if (result.hasErrors()) {
            model.addAttribute(service.findOne(id));
            return "api/form";
        }
        Api api = new Api();
        BeanUtils.copyProperties(form, api);
        api.setExpressions(jsonObjectMapper.writeValueAsString(form.getExpressions()));
        service.update(id, api);
        redirectAttributes.addFlashAttribute(SuccessMessage.builder().code(MessageCode.DATA_HAS_BEEN_UPDATED).build());
        return "redirect:/manager/apis/{id}";
    }

    @RequestMapping(path = "{id}", method = RequestMethod.POST, params = "delete")
    public String delete(@PathVariable int id, RedirectAttributes redirectAttributes) {
        service.delete(id);
        redirectAttributes.addFlashAttribute(SuccessMessage.builder().code(MessageCode.DATA_HAS_BEEN_DELETED).build());
        return "redirect:/manager/apis";
    }

}
