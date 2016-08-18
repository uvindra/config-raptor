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
import org.json.JSONObject;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class XMLConfigOperator {

    public enum Position {
        BEFORE,
        AT,
        AFTER
    }
    private Document doc;

    // No external construction allowed to prevent issues related to the wrong Document object being used
    XMLConfigOperator(Document doc) {
        this.doc = doc;
    }

    public boolean isConfigExists(String pathString) throws ConfigException {
        try {
            NodeList nodeList = getXMLElements(pathString);

            if (nodeList.getLength() != 0) {
                return true;
            }
            else {
                return false;
            }

        } catch (XPathExpressionException e) {
            throw new ConfigException("XPath expression '" + pathString + "' evaluation error", e);
        }
    }

    public Node getConfig(String pathString) throws ConfigException {
        Node config = null;

        try {
            NodeList nodeList = getXMLElements(pathString);

            if (nodeList.getLength() > 0) {
                config = nodeList.item(0);

                if (config.getParentNode() == null) {
                    config = config.getFirstChild();
                }
            }
        } catch (XPathExpressionException e) {
            throw new ConfigException("XPath expression '" + pathString + "' evaluation error", e);
        }

        return config;
    }

    public boolean updateConfig(String pathString, Node newNode) throws ConfigException {
        try {
            NodeList nodeList = getXMLElements(pathString);

            if (nodeList.getLength() > 0) {
                Node node = nodeList.item(0);

                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    XMLUtil.updateNode(node, newNode);

                    return true;
                }
            }
        } catch (XPathExpressionException e) {
            throw new ConfigException("XPath expression '" + pathString + "' evaluation error", e);
        }

        return false;
    }

    public boolean addConfig(String pathString, Node config, Position position) throws ConfigException {
        try {
            NodeList nodeList = getXMLElements(pathString);

            if (nodeList.getLength() > 0) {
                Node currentNode = nodeList.item(0);

                if (position == Position.BEFORE) {
                    return addConfigBefore(currentNode, config);
                }
                else if (position == Position.AT) {
                    return addConfigAt(currentNode, config);
                }
                else { // Position.AFTER
                    return addConfigAfter(currentNode, config);
                }
            }
        } catch (XPathExpressionException e) {
            throw new ConfigException("XPath expression '" + pathString + "' evaluation error", e);
        }

        return false;
    }

    public boolean removeConfig(String pathString) throws ConfigException {
        try {
            NodeList nodeList = getXMLElements(pathString);

            if (nodeList.getLength() > 0) {
                Node currentNode = nodeList.item(0);
                return XMLUtil.removeNode(currentNode);
            }
        } catch (XPathExpressionException e) {
            throw new ConfigException("XPath expression '" + pathString + "' evaluation error", e);
        }

        return false;
    }


    private NodeList getXMLElements(String searchString) throws XPathExpressionException {
        XPathFactory xPathfactory = XPathFactory.newInstance();
        XPath xpath = xPathfactory.newXPath();
        XPathExpression expr = xpath.compile(searchString);

        return (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
    }


    private boolean addConfigBefore(Node currentNode, Node newNode)
    {
        Node parentNode = currentNode.getParentNode();

        if (parentNode != null) {
            NodeList childNodes = parentNode.getChildNodes();
            List<Node> childList = new ArrayList<>(childNodes.getLength() + 1);

            for (int i = 0; i < childNodes.getLength(); ++i) {
                Node child = childNodes.item(i);

                if (child.equals(currentNode)) {
                    childList.add(newNode);
                }

                childList.add(child);
            }

            while (parentNode.hasChildNodes()) {
                parentNode.removeChild(parentNode.getFirstChild());
            }

            for (Node node : childList) {
                parentNode.appendChild(node);
            }

            return true;
        }

        return false;
    }


    private boolean addConfigAt(Node currentNode, Node newNode)
    {
        if (currentNode.getParentNode() != null) { // if not the root node
            currentNode.appendChild(newNode);

            return true;
        }

        return false;
    }


    private boolean addConfigAfter(Node currentNode, Node newNode)
    {
        Node parentNode = currentNode.getParentNode();

        if (parentNode != null) {
            NodeList childNodes = parentNode.getChildNodes();
            List<Node> childList = new ArrayList<>(childNodes.getLength() + 1);

            for (int i = 0; i < childNodes.getLength(); ++i) {
                Node child = childNodes.item(i);

                childList.add(child);

                if (child.equals(currentNode)) {
                    childList.add(newNode);
                }
            }

            while (parentNode.hasChildNodes()) {
                parentNode.removeChild(parentNode.getFirstChild());
            }

            for (Node node : childList) {
                parentNode.appendChild(node);
            }

            return true;
        }

        return false;
    }
}
