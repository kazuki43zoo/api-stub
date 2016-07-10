package com.kazuki43zoo.api.key;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Component
@Order(2)
public class XPathKeyExtractor implements KeyExtractor {

    @Override
    public List<String> extract(HttpServletRequest request, String requestBody, String... expressions)
            throws ParserConfigurationException, IOException, SAXException {
        Document document = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder().parse(new ByteArrayInputStream(requestBody.getBytes(StandardCharsets.UTF_8)));
        XPath xpath = XPathFactory.newInstance().newXPath();
        List<String> values = new ArrayList<>();
        for (String expression : expressions) {
            try {
                XPathExpression xPathExpression = xpath.compile(expression);
                String id = (String) xPathExpression.evaluate(document, XPathConstants.STRING);
                values.add(id);
            } catch (Exception e) {
                // skip
            }
        }
        return values;
    }

}
