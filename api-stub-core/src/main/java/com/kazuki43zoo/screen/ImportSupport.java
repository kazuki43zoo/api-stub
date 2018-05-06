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
package com.kazuki43zoo.screen;

import com.kazuki43zoo.component.message.ErrorMessage;
import com.kazuki43zoo.component.message.InfoMessage;
import com.kazuki43zoo.component.message.MessageCode;
import com.kazuki43zoo.component.message.SuccessMessage;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Component
public class ImportSupport {

    public void storeProcessingResultMessages(RedirectAttributes redirectAttributes, List<?> newList, List<?> ignoredList) {
        if (newList.size() == ignoredList.size()) {
            redirectAttributes.addFlashAttribute(ErrorMessage.builder().code(MessageCode.ALL_DATA_HAS_NOT_BEEN_IMPORTED).build());
        } else {
            redirectAttributes.addFlashAttribute(SuccessMessage.builder().code(MessageCode.DATA_HAS_BEEN_IMPORTED).build());
            if (!ignoredList.isEmpty()) {
                redirectAttributes.addFlashAttribute(InfoMessage.builder().code(MessageCode.PARTIALLY_DATA_HAS_NOT_BEEN_IMPORTED).build());
            }
        }
    }

}
