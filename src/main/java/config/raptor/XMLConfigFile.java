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

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.File;
import java.io.IOException;


public class XMLConfigFile implements ConfigFile {

    private File file;
    private Document doc;

    public XMLConfigFile(String filePath) throws ParserConfigurationException, IOException, SAXException {
        file = new File(filePath);

        if (!file.isFile()) {
            throw new IllegalArgumentException("The file path " + filePath + " provided is not a valid file");
        }

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        docFactory.setNamespaceAware(true);

        docFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        docFactory.setIgnoringComments(false);
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        doc = docBuilder.parse(file);
        doc.getDocumentElement().normalize();
    }

    @Override
    public boolean isConfigExists(String pathString) throws ConfigException{
        try {
            NodeList nodeList = getXMLElements(pathString);

            if (nodeList.getLength() != 0) {
                return true;
            }
            else {
                return false;
            }

        } catch (XPathExpressionException e) {
            throw new ConfigException("XPath expression '" + pathString + "' evaluation error : " + e.getMessage(), e);
        }
    }

    @Override
    public Config getConfig(String pathString) throws ConfigException {
        Config config = null;

        try {
            NodeList nodeList = getXMLElements(pathString);

            if (nodeList.getLength() > 0) {
                Node node = nodeList.item(0);

                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    config = new XMLConfig(element.getTagName(), element);
                }
            }
        } catch (XPathExpressionException e) {
            throw new ConfigException("XPath expression '" + pathString + "' evaluation error : " + e.getMessage(), e);
        }

        return config;
    }

    @Override
    public void updateConfig(String pathString, Config config) {

    }

    @Override
    public void addConfig(String pathString, Config config) {

    }

    @Override
    public void removeConfig(String pathString) {

    }


    private NodeList getXMLElements(String searchString) throws XPathExpressionException {
        XPathFactory xPathfactory = XPathFactory.newInstance();
        XPath xpath = xPathfactory.newXPath();

        XPathExpression expr = xpath.compile(searchString);
        //return expr.evaluate(doc);

        return (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
    }

}
