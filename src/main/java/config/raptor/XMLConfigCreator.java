/*
 * Copyright 2016 Uvindra Dias Jayasinha
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package config.raptor;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.util.ArrayList;
import java.util.List;

public class XMLConfigCreator implements ConfigCreator {

    public static class AttributePair {
        public String key;
        public String value;
    }

    private String nodeName;
    private String nodeValue;
    private List<AttributePair> attributePairList = new ArrayList<>();
    private Document doc;

    XMLConfigCreator(Document doc, String nodeName) {
        this.doc = doc;
        this.nodeName = nodeName;
    }

    XMLConfigCreator(Document doc, String nodeName, String nodeValue) {
        this.doc = doc;
        this.nodeName = nodeName;
        this.nodeValue = nodeValue;
    }

    XMLConfigCreator(Document doc, String nodeName, String nodeValue, List<AttributePair> attributePairList) {
        this.doc = doc;
        this.nodeName = nodeName;
        this.nodeValue = nodeValue;
        this.attributePairList = attributePairList;
    }

    @Override
    public Config createConfig() throws ConfigException {
        Element element = doc.createElement(nodeName);

        if (!nodeValue.isEmpty()) {
            element.setTextContent(nodeValue);
        }

        for (AttributePair attributePair : attributePairList) {
            element.setAttribute(attributePair.key, attributePair.value);
        }

        return new XMLConfig(nodeName, element);
    }
}
