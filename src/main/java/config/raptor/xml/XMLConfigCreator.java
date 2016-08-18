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

package config.raptor.xml;

import config.raptor.ConfigException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class XMLConfigCreator {
    private Document document;

    // No external construction allowed to prevent issues related to the wrong Document object being used
    XMLConfigCreator(Document document) {
        this.document = document;
    }

    public Node createConfig(String xmlString) throws ConfigException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        docFactory.setNamespaceAware(true);

        Node node;
        try {
            docFactory.setFeature(javax.xml.XMLConstants.FEATURE_SECURE_PROCESSING, true);
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(new InputSource(
                                        new ByteArrayInputStream(xmlString.getBytes(StandardCharsets.UTF_8))));

            node = this.document.importNode(doc.getDocumentElement(), true);
        } catch (SAXException e) {
            throw new ConfigException("XML parsing error when parsing xml string '" + xmlString + "'", e);
        } catch (ParserConfigurationException e) {
            throw new ConfigException("Parser configuration error", e);
        } catch (IOException e) {
            throw new ConfigException("Error reading xml string '" + xmlString + "'", e);
        }

        return node;
    }
}
