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
import org.testng.Assert;
import org.testng.annotations.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.File;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

public class XMLConfigOperatorTest {

    private static final String API_MANAGER_CONF = "api-manager.xml";
    private static final String COMMENTS_CONF = "comments.xml";
    private Document apiManagerConfDoc;
    private Document commentsConfDoc;

    private Node createConfig(Document doc, String name, String content) throws ParserConfigurationException {
        Element expectedElement = doc.createElement(name);
        expectedElement.setTextContent(content);
        return expectedElement;
    }

    private Node getNode(Document doc, String xpathString) throws XPathExpressionException {
        XPathFactory xPathfactory = XPathFactory.newInstance();
        XPath xpath = xPathfactory.newXPath();
        XPathExpression expr = xpath.compile(xpathString);

        NodeList nodeList = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
        return nodeList.item(0);

    }

    private TreeMap<Integer, Node> getChildren(Node parentNode) {
        NodeList children = parentNode.getChildNodes();

        TreeMap<Integer, Node> siblings = new TreeMap<>();

        for (int i = 0; i < children.getLength(); ++i) {
            siblings.put(i, children.item(i));
        }

        return siblings;
    }

    private int getPositionOfChild(Node parentNode, String contentMatch) {
        NodeList children = parentNode.getChildNodes();

        for (int i = 0; i < children.getLength(); ++i) {
            Node child =  children.item(i);

            if (child.getNodeType() == Node.COMMENT_NODE) {
                if (child.getTextContent().contains(contentMatch)) {
                    return i;
                }
            }
            else {
                if (child.getNodeName().contains(contentMatch)) {
                    return i;
                }
            }
        }

        return -1;
    }


    @BeforeMethod
    public void setUp() throws Exception {
        File apiManagerConfFile = new File(Thread.currentThread().getContextClassLoader().
                getResource(API_MANAGER_CONF).getFile());

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        apiManagerConfDoc = builder.parse(apiManagerConfFile.getAbsolutePath());

        File commentsConfFile = new File(Thread.currentThread().getContextClassLoader().
                getResource(COMMENTS_CONF).getFile());

        factory = DocumentBuilderFactory.newInstance();
        builder = factory.newDocumentBuilder();
        commentsConfDoc = builder.parse(commentsConfFile.getAbsolutePath());
    }

    @Test
    public void testIsConfigExists() throws Exception {
        XMLConfigOperator apiManagerConf = new XMLConfigOperator(apiManagerConfDoc);

        boolean isExists = apiManagerConf.isConfigExists("//DataSourceName");
        Assert.assertTrue(isExists);
    }

    @Test
    public void testSearchNonExistingElement() throws Exception {
        XMLConfigOperator apiManagerConf = new XMLConfigOperator(apiManagerConfDoc);

        boolean isExists = apiManagerConf.isConfigExists("blah");
        Assert.assertFalse(isExists);

        isExists = apiManagerConf.isConfigExists("//dataSourceName");
        Assert.assertFalse(isExists);
    }

    @Test
    public void testIsConfigExistsInvalidXPath() throws Exception {
        XMLConfigOperator apiManagerConf = new XMLConfigOperator(apiManagerConfDoc);

        ConfigException exception = null;
        try {
            apiManagerConf.isConfigExists("\\");
        }
        catch (ConfigException e) {
            exception = e;
        }

        Assert.assertNotNull(exception);
        Assert.assertEquals("XPath expression '\\' evaluation error", exception.getMessage());
    }

    @Test
    public void testIsConfigExistsAsComment() throws Exception {
        XMLConfigOperator apiManagerConf = new XMLConfigOperator(apiManagerConfDoc);

        boolean isExists = apiManagerConf.isConfigExists("//APIConsumerAuthentication/comment()[contains(., 'ClaimsRetrieverImplClass')]");
        Assert.assertTrue(isExists);

        isExists = apiManagerConf.isConfigExists("//APIConsumerAuthentication/comment()[contains(., 'ConsumerDialectURI>http://wso2.org/claims</ConsumerDialectURI')]");
        Assert.assertTrue(isExists);
    }


    @Test
    public void testGetConfig() throws Exception {
        XMLConfigOperator apiManagerConf = new XMLConfigOperator(apiManagerConfDoc);

        // Create expected element
        Node expectedConfig = createConfig(apiManagerConfDoc, "SecurityContextHeader", "X-JWT-Assertion");

        Node config = apiManagerConf.getConfig("//APIConsumerAuthentication/SecurityContextHeader");
        Assert.assertNotNull(config);
        Assert.assertEquals(config.getNodeName(), "SecurityContextHeader");


        Assert.assertEquals(config.getNodeName(), expectedConfig.getNodeName());
        Assert.assertEquals(config.getTextContent(), expectedConfig.getTextContent());

        config = apiManagerConf.getConfig("//SecurityContextHeader");
        Assert.assertNotNull(config);
        Assert.assertEquals(config.getNodeName(), "SecurityContextHeader");
        Assert.assertEquals(config.getNodeName(), expectedConfig.getNodeName());
        Assert.assertEquals(config.getTextContent(), expectedConfig.getTextContent());
    }

    @Test
    public void testGetConfigNonExistingElement() throws Exception {
        XMLConfigOperator apiManagerConf = new XMLConfigOperator(apiManagerConfDoc);

        Node config = apiManagerConf.getConfig("//SecurityContextHeaders");
        Assert.assertNull(config);
    }

    @Test
    public void testGetConfigInvalidXPath() throws Exception {
        XMLConfigOperator apiManagerConf = new XMLConfigOperator(apiManagerConfDoc);

        ConfigException exception = null;
        try {
            apiManagerConf.getConfig("\\");
        }
        catch (ConfigException e) {
            exception = e;
        }

        Assert.assertNotNull(exception);
        Assert.assertEquals("XPath expression '\\' evaluation error", exception.getMessage());
    }

    @Test
    public void testUpdateConfig() throws Exception {
        XMLConfigOperator apiManagerConf = new XMLConfigOperator(apiManagerConfDoc);

        // Create expected element
        Node expectedConfig = createConfig(apiManagerConfDoc, "Username", "admin");

        Assert.assertTrue(apiManagerConf.updateConfig("//AuthManager/Username", expectedConfig));

        Node config = apiManagerConf.getConfig("//AuthManager/Username");
        Assert.assertNotNull(config);
        Assert.assertEquals(config.getNodeName(), "Username");
        Assert.assertEquals(config.getNodeName(), expectedConfig.getNodeName());
        Assert.assertEquals(config.getTextContent(), expectedConfig.getTextContent());
    }

    @Test
    public void testUpdateNonExistingConfig() throws Exception {
        XMLConfigOperator apiManagerConf = new XMLConfigOperator(apiManagerConfDoc);

        // Create expected element
        Node expectedConfig = createConfig(apiManagerConfDoc, "Username", "blah");

        Assert.assertFalse(apiManagerConf.updateConfig("//AuthManager/UsernameS", expectedConfig));

        // Check if actual config has been unchanged
        Node config = apiManagerConf.getConfig("//AuthManager/Username");
        Assert.assertNotNull(config);
        Assert.assertEquals(config.getNodeName(), "Username");
        Assert.assertNotEquals(config.getTextContent(), expectedConfig.getTextContent());
    }

    @Test
    public void testUpdateConfigInvalidXPath() throws Exception {
        XMLConfigOperator apiManagerConf = new XMLConfigOperator(apiManagerConfDoc);

        ConfigException exception = null;
        try {
            apiManagerConf.updateConfig("\\", createConfig(apiManagerConfDoc, "Username", "admin"));
        }
        catch (ConfigException e) {
            exception = e;
        }

        Assert.assertNotNull(exception);
        Assert.assertEquals("XPath expression '\\' evaluation error", exception.getMessage());
    }

    @Test
    public void testAddConfigBefore() throws Exception {
        XMLConfigOperator apiManagerConf = new XMLConfigOperator(apiManagerConfDoc);

        // Validate order of child nodes
        Node parentNode = getNode(apiManagerConfDoc, "//APIConsumerAuthentication");

        int childPosition = getPositionOfChild(parentNode, "ConsumerDialectURI");
        TreeMap<Integer, Node> origSiblings = getChildren(parentNode);

        Node newConfig = createConfig(apiManagerConfDoc, "ClaimsRetrieverImplClass",
                "org.wso2.carbon.apimgt.impl.token.DefaultClaimsRetriever");

        boolean isAdded = apiManagerConf.addConfig("//APIConsumerAuthentication/comment()" +
                        "[contains(., 'ConsumerDialectURI')]", newConfig, XMLConfigOperator.Position.BEFORE);
        Assert.assertTrue(isAdded);

        Node config = apiManagerConf.getConfig("//APIConsumerAuthentication/ClaimsRetrieverImplClass");
        Assert.assertNotNull(config);

        // Validate order of child nodes
        TreeMap<Integer, Node> newSiblings = getChildren(parentNode);

        Assert.assertTrue(newSiblings.size() == origSiblings.size() + 1);

        final Iterator<Map.Entry<Integer, Node>> it = newSiblings.entrySet().iterator();

        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            int position = ((Integer)pair.getKey());

            if (position < childPosition) {
                Assert.assertEquals(origSiblings.get(position).getNodeType(),newSiblings.get(position).getNodeType());
                Assert.assertEquals(origSiblings.get(position).getNodeName(),newSiblings.get(position).getNodeName());
                Assert.assertEquals(origSiblings.get(position).getTextContent(),newSiblings.get(position).getTextContent());
            }
            else if (position == childPosition) {
                Assert.assertEquals(newSiblings.get(position).getNodeType(), Node.ELEMENT_NODE);
                Assert.assertEquals(newSiblings.get(position).getNodeName(), "ClaimsRetrieverImplClass");
                Assert.assertEquals(newSiblings.get(position).getTextContent(), "org.wso2.carbon.apimgt.impl.token.DefaultClaimsRetriever");
            }
            else {
                Assert.assertEquals(origSiblings.get(position - 1).getNodeType(),newSiblings.get(position).getNodeType());
                Assert.assertEquals(origSiblings.get(position - 1).getNodeName(),newSiblings.get(position).getNodeName());
                Assert.assertEquals(origSiblings.get(position - 1).getTextContent(),newSiblings.get(position).getTextContent());
            }
        }
    }


    @Test
    public void testAddConfigBeforeRootElement() throws Exception {
        XMLConfigOperator apiManagerConf = new XMLConfigOperator(apiManagerConfDoc);

        Assert.assertFalse(apiManagerConf.addConfig("/", createConfig(apiManagerConfDoc, "ClaimsRetrieverImplClass",
                "org.wso2.carbon.apimgt.impl.token.DefaultClaimsRetriever"), XMLConfigOperator.Position.BEFORE));

        // Validate if root element has changed
        Node root = apiManagerConf.getConfig("/");

        Assert.assertFalse(root.getNodeName().equals("ClaimsRetrieverImplClass"));
    }

    @Test
    public void testAddConfigAtRootElement() throws Exception {
        XMLConfigOperator apiManagerConf = new XMLConfigOperator(apiManagerConfDoc);

        Assert.assertFalse(apiManagerConf.addConfig("/", createConfig(apiManagerConfDoc, "ClaimsRetrieverImplClass",
                        "org.wso2.carbon.apimgt.impl.token.DefaultClaimsRetriever"), XMLConfigOperator.Position.AT));

        // Validate if root element has changed
        Node root = apiManagerConf.getConfig("/");

        Assert.assertFalse(root.getNodeName().equals("ClaimsRetrieverImplClass"));
    }

    @Test
    public void testAddConfigAfterRootElement() throws Exception {
        XMLConfigOperator apiManagerConf = new XMLConfigOperator(apiManagerConfDoc);

        Assert.assertFalse(apiManagerConf.addConfig("/", createConfig(apiManagerConfDoc, "ClaimsRetrieverImplClass",
                                                "org.wso2.carbon.apimgt.impl.token.DefaultClaimsRetriever"),
                XMLConfigOperator.Position.AFTER));

        // Validate if root element has changed
        Node root = apiManagerConf.getConfig("/");

        Assert.assertFalse(root.getNodeName().equals("ClaimsRetrieverImplClass"));
    }

    @Test
    public void testAddConfigBeforeRootElementWithStartingComments() throws Exception {
        XMLConfigOperator commentsConf = new XMLConfigOperator(commentsConfDoc);

        Assert.assertFalse(commentsConf.addConfig("/", createConfig(commentsConfDoc, "ClaimsRetrieverImplClass",
                "org.wso2.carbon.apimgt.impl.token.DefaultClaimsRetriever"), XMLConfigOperator.Position.BEFORE));

        // Validate if root element has changed
        Node root = commentsConf.getConfig("/");

        Assert.assertFalse(root.getNodeName().equals("ClaimsRetrieverImplClass"));
    }


    @Test
    public void testAddConfigAt() throws Exception {
        XMLConfigOperator apiManagerConf = new XMLConfigOperator(apiManagerConfDoc);

        // Validate order of child nodes
        Node parentNode = getNode(apiManagerConfDoc, "//APIConsumerAuthentication");
        TreeMap<Integer, Node> origSiblings = getChildren(parentNode);
        int lastNodePosition = origSiblings.size() - 1;

        Assert.assertTrue(apiManagerConf.addConfig("//APIConsumerAuthentication",
                createConfig(apiManagerConfDoc, "APIMClaimCacheExpiry", "600"), XMLConfigOperator.Position.AT));

        Node config = apiManagerConf.getConfig("//APIConsumerAuthentication/APIMClaimCacheExpiry");
        Assert.assertNotNull(config);


        // Validate order of child nodes
        TreeMap<Integer, Node> newSiblings = getChildren(parentNode);

        Assert.assertTrue(newSiblings.size() == origSiblings.size() + 1);

        final Iterator<Map.Entry<Integer, Node>> it = newSiblings.entrySet().iterator();

        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            int position = ((Integer)pair.getKey());

            if (position <= lastNodePosition) {
                Assert.assertEquals(origSiblings.get(position).getNodeType(),newSiblings.get(position).getNodeType());
                Assert.assertEquals(origSiblings.get(position).getNodeName(),newSiblings.get(position).getNodeName());
                Assert.assertEquals(origSiblings.get(position).getTextContent(),newSiblings.get(position).getTextContent());
            }
            else  {
                Assert.assertEquals(newSiblings.get(position).getNodeType(), Node.ELEMENT_NODE);
                Assert.assertEquals(newSiblings.get(position).getNodeName(), "APIMClaimCacheExpiry");
                Assert.assertEquals(newSiblings.get(position).getTextContent(), "600");
            }
        }
    }


    @Test
    public void testAddConfigAfter() throws Exception {
        XMLConfigOperator apiManagerConf = new XMLConfigOperator(apiManagerConfDoc);

        // Validate order of child nodes
        Node parentNode = getNode(apiManagerConfDoc, "//APIConsumerAuthentication");
        int childPosition = getPositionOfChild(parentNode, "ClaimsRetrieverImplClass");
        TreeMap<Integer, Node> origSiblings = getChildren(parentNode);

        Node newConfig = createConfig(apiManagerConfDoc, "ClaimsRetrieverImplClass",
                "org.wso2.carbon.apimgt.impl.token.DefaultClaimsRetriever");

        Assert.assertTrue(apiManagerConf.addConfig("//APIConsumerAuthentication/comment()" +
                        "[contains(., 'ClaimsRetrieverImplClass')]",
                newConfig, XMLConfigOperator.Position.AFTER));

        Node config = apiManagerConf.getConfig("//APIConsumerAuthentication/ClaimsRetrieverImplClass");
        Assert.assertNotNull(config);

        // Validate order of child nodes
        parentNode = getNode(apiManagerConfDoc, "//APIConsumerAuthentication");
        TreeMap<Integer, Node> newSiblings = getChildren(parentNode);

        Assert.assertTrue(newSiblings.size() == origSiblings.size() + 1);

        final Iterator<Map.Entry<Integer, Node>> it = newSiblings.entrySet().iterator();

        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            int position = ((Integer) pair.getKey());

            if (position <= childPosition) {
                Assert.assertEquals(origSiblings.get(position).getNodeType(),newSiblings.get(position).getNodeType());
                Assert.assertEquals(origSiblings.get(position).getNodeName(),newSiblings.get(position).getNodeName());
                Assert.assertEquals(origSiblings.get(position).getTextContent(),newSiblings.get(position).getTextContent());
            }
            else if (position == childPosition + 1) {
                Assert.assertEquals(newSiblings.get(position).getNodeType(), Node.ELEMENT_NODE);
                Assert.assertEquals(newSiblings.get(position).getNodeName(), "ClaimsRetrieverImplClass");
                Assert.assertEquals(newSiblings.get(position).getTextContent(), "org.wso2.carbon.apimgt.impl.token.DefaultClaimsRetriever");
            }
            else {
                Assert.assertEquals(origSiblings.get(position - 1).getNodeType(),newSiblings.get(position).getNodeType());
                Assert.assertEquals(origSiblings.get(position - 1).getNodeName(),newSiblings.get(position).getNodeName());
                Assert.assertEquals(origSiblings.get(position - 1).getTextContent(),newSiblings.get(position).getTextContent());
            }
        }
    }

    @Test
    public void testAddToNonExistingConfig() throws Exception {
        XMLConfigOperator apiManagerConf = new XMLConfigOperator(apiManagerConfDoc);

        Node newConfig = createConfig(apiManagerConfDoc, "Rubbish", "dump");
        Assert.assertFalse(apiManagerConf.addConfig("//AuthManager/UsernameS", newConfig,
                                                                    XMLConfigOperator.Position.AFTER));

        // Check if config got added with invalid xpath
        Node config = apiManagerConf.getConfig("//AuthManager/Rubbish");
        Assert.assertNull(config);
    }



    @Test
    public void testAddConfigInvalidXPath() throws Exception {
        XMLConfigOperator apiManagerConf = new XMLConfigOperator(apiManagerConfDoc);

        ConfigException exception = null;
        try {
            apiManagerConf.addConfig("\\", createConfig(apiManagerConfDoc, "Username", "admin"),
                    XMLConfigOperator.Position.AFTER);
        }
        catch (ConfigException e) {
            exception = e;
        }

        Assert.assertNotNull(exception);
        Assert.assertEquals("XPath expression '\\' evaluation error", exception.getMessage());
    }


    @Test
    public void testRemoveConfig() throws Exception {
        XMLConfigOperator apiManagerConf = new XMLConfigOperator(apiManagerConfDoc);

        // Validate order of child nodes
        Node parentNode = getNode(apiManagerConfDoc, "//AuthManager");
        int childPosition = getPositionOfChild(parentNode, "Username");
        TreeMap<Integer, Node> origSiblings = getChildren(parentNode);

        Assert.assertTrue(apiManagerConf.removeConfig("//AuthManager/Username"));

        Node config = apiManagerConf.getConfig("//AuthManager/Username");
        Assert.assertNull(config);

        // Validate order of child nodes
        TreeMap<Integer, Node> newSiblings = getChildren(parentNode);

        Assert.assertTrue(newSiblings.size() == origSiblings.size() - 1);

        final Iterator<Map.Entry<Integer, Node>> it = newSiblings.entrySet().iterator();

        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            int position = ((Integer) pair.getKey());

            if (position < childPosition) {
                Assert.assertEquals(origSiblings.get(position).getNodeType(),newSiblings.get(position).getNodeType());
                Assert.assertEquals(origSiblings.get(position).getNodeName(),newSiblings.get(position).getNodeName());
                Assert.assertEquals(origSiblings.get(position).getTextContent(),newSiblings.get(position).getTextContent());
            }
            else {
                Assert.assertEquals(origSiblings.get(position + 1).getNodeType(),newSiblings.get(position).getNodeType());
                Assert.assertEquals(origSiblings.get(position + 1).getNodeName(),newSiblings.get(position).getNodeName());
                Assert.assertEquals(origSiblings.get(position + 1).getTextContent(),newSiblings.get(position).getTextContent());
            }
        }
    }


    @Test
    public void testRemoveNonExistingConfig() throws Exception {
        XMLConfigOperator apiManagerConf = new XMLConfigOperator(apiManagerConfDoc);

        // Validate order of child nodes
        Node parentNode = getNode(apiManagerConfDoc, "//AuthManager");
        TreeMap<Integer, Node> origSiblings = getChildren(parentNode);

        Assert.assertFalse(apiManagerConf.removeConfig("//AuthManager/UsernameS"));

        // Validate order of child nodes
        TreeMap<Integer, Node> newSiblings = getChildren(parentNode);

        Assert.assertTrue(newSiblings.size() == origSiblings.size());
    }

    @Test
    public void testRemoveConfigInvalidXPath() throws Exception {
        XMLConfigOperator apiManagerConf = new XMLConfigOperator(apiManagerConfDoc);

        ConfigException exception = null;
        try {
            apiManagerConf.removeConfig("\\");
        }
        catch (ConfigException e) {
            exception = e;
        }

        Assert.assertNotNull(exception);
        Assert.assertEquals("XPath expression '\\' evaluation error", exception.getMessage());
    }

    @Test
    public void testRemoveRootElement() throws Exception {
        XMLConfigOperator apiManagerConf = new XMLConfigOperator(apiManagerConfDoc);

        Assert.assertFalse(apiManagerConf.removeConfig("/"));

        // Validate if root element has changed
        Node root = apiManagerConf.getConfig("/");

        Assert.assertTrue(root.getNodeName().equals("APIManager"));
    }
}