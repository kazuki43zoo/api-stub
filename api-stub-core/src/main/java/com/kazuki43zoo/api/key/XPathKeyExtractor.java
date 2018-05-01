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
package com.kazuki43zoo.api.key;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@Order(2)
public class XPathKeyExtractor implements KeyExtractor {

    @Override
    public List<Object> extract(HttpServletRequest request, byte[] requestBody, String... expressions) {
        if (requestBody == null || requestBody.length == 0) {
            return Collections.emptyList();
        }

        Document document;
        try {
            document = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder().parse(new ByteArrayInputStream(requestBody));
        } catch (SAXException | IOException | ParserConfigurationException e) {
            throw new IllegalStateException(e);
        }
        XPath xpath = XPathFactory.newInstance().newXPath();

        return Stream.of(expressions).map(expression -> {
            try {
                XPathExpression xPathExpression = xpath.compile(expression);
                return (String) xPathExpression.evaluate(document, XPathConstants.STRING);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }).filter(StringUtils::hasLength).collect(Collectors.toList());
    }

}
