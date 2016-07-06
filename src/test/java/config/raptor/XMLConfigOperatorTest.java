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

import org.testng.Assert;
import org.testng.annotations.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

public class XMLConfigOperatorTest {

    private static final String API_MANAGER_CONF = "api-manager.xml";
    private static final String COMMENTS_CONF = "comments.xml";
    private String apiManagerConfPath;
    private String commentsConfPath;

    private Config createConfig(String name, String content) throws ParserConfigurationException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document doc = docBuilder.newDocument();
        Element expectedElement = doc.createElement(name);
        expectedElement.setTextContent(content);
        return new XMLConfig(name, expectedElement);
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
        apiManagerConfPath = apiManagerConfFile.getAbsolutePath();

        File commentsConfFile = new File(Thread.currentThread().getContextClassLoader().
                getResource(COMMENTS_CONF).getFile());

        commentsConfPath = commentsConfFile.getAbsolutePath();
    }

    @Test
    public void testIsConfigExists() throws Exception {
        XMLConfigIO apiManagerConfIO = new XMLConfigIO(apiManagerConfPath);
        ConfigOperator apiManagerConf = apiManagerConfIO.getOperator();

        boolean isExists = apiManagerConf.isConfigExists("//DataSourceName");
        Assert.assertTrue(isExists);
    }

    @Test
    public void testSearchNonExistingElement() throws Exception {
        XMLConfigIO apiManagerConfIO = new XMLConfigIO(apiManagerConfPath);
        ConfigOperator apiManagerConf = apiManagerConfIO.getOperator();

        boolean isExists = apiManagerConf.isConfigExists("blah");
        Assert.assertFalse(isExists);

        isExists = apiManagerConf.isConfigExists("//dataSourceName");
        Assert.assertFalse(isExists);
    }

    @Test
    public void testIsConfigExistsInvalidXPath() throws Exception {
        XMLConfigIO apiManagerConfIO = new XMLConfigIO(apiManagerConfPath);
        ConfigOperator apiManagerConf = apiManagerConfIO.getOperator();

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
        XMLConfigIO apiManagerConfIO = new XMLConfigIO(apiManagerConfPath);
        ConfigOperator apiManagerConf = apiManagerConfIO.getOperator();

        boolean isExists = apiManagerConf.isConfigExists("//APIConsumerAuthentication/comment()[contains(., 'ClaimsRetrieverImplClass')]");
        Assert.assertTrue(isExists);

        isExists = apiManagerConf.isConfigExists("//APIConsumerAuthentication/comment()[contains(., 'ConsumerDialectURI>http://wso2.org/claims</ConsumerDialectURI')]");
        Assert.assertTrue(isExists);
    }


    @Test
    public void testGetConfig() throws Exception {
        XMLConfigIO apiManagerConfIO = new XMLConfigIO(apiManagerConfPath);
        ConfigOperator apiManagerConf = apiManagerConfIO.getOperator();

        // Create expected element
        Config expectedConfig = createConfig("SecurityContextHeader", "X-JWT-Assertion");

        Config config = apiManagerConf.getConfig("//APIConsumerAuthentication/SecurityContextHeader");
        Assert.assertNotNull(config);
        Assert.assertEquals(config.getName(), "SecurityContextHeader");
        Assert.assertTrue(config.equals(expectedConfig));

        config = apiManagerConf.getConfig("//SecurityContextHeader");
        Assert.assertNotNull(config);
        Assert.assertEquals(config.getName(), "SecurityContextHeader");
        Assert.assertTrue(config.equals(expectedConfig));
    }

    @Test
    public void testGetConfigNonExistingElement() throws Exception {
        XMLConfigIO apiManagerConfIO = new XMLConfigIO(apiManagerConfPath);
        ConfigOperator apiManagerConf = apiManagerConfIO.getOperator();

        Config config = apiManagerConf.getConfig("//SecurityContextHeaders");
        Assert.assertNull(config);
    }

    @Test
    public void testGetConfigInvalidXPath() throws Exception {
        XMLConfigIO apiManagerConfIO = new XMLConfigIO(apiManagerConfPath);
        ConfigOperator apiManagerConf = apiManagerConfIO.getOperator();

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
        XMLConfigIO apiManagerConfIO = new XMLConfigIO(apiManagerConfPath);
        ConfigOperator apiManagerConf = apiManagerConfIO.getOperator();

        XMLConfigCreator configCreator = new XMLConfigCreator(apiManagerConfIO.getXMLDocument(), "Username", "admin");

        Assert.assertTrue(apiManagerConf.updateConfig("//AuthManager/Username", configCreator.createConfig()));

        // Create expected element
        Config expectedConfig = createConfig("Username", "admin");

        Config config = apiManagerConf.getConfig("//AuthManager/Username");
        Assert.assertNotNull(config);
        Assert.assertEquals(config.getName(), "Username");
        Assert.assertTrue(config.equals(expectedConfig));
    }

    @Test
    public void testUpdateNonExistingConfig() throws Exception {
        XMLConfigIO apiManagerConfIO = new XMLConfigIO(apiManagerConfPath);
        ConfigOperator apiManagerConf = apiManagerConfIO.getOperator();

        XMLConfigCreator configCreator = new XMLConfigCreator(apiManagerConfIO.getXMLDocument(), "Username", "admin");

        Assert.assertFalse(apiManagerConf.updateConfig("//AuthManager/UsernameS", configCreator.createConfig()));

        // Create expected element
        Config expectedConfig = createConfig("Username", "admin");

        // Check if actual config has been unchanged
        Config config = apiManagerConf.getConfig("//AuthManager/Username");
        Assert.assertNotNull(config);
        Assert.assertEquals(config.getName(), "Username");
        Assert.assertFalse(config.equals(expectedConfig));
    }

    @Test
    public void testUpdateConfigInvalidXPath() throws Exception {
        XMLConfigIO apiManagerConfIO = new XMLConfigIO(apiManagerConfPath);
        ConfigOperator apiManagerConf = apiManagerConfIO.getOperator();

        XMLConfigCreator configCreator = new XMLConfigCreator(apiManagerConfIO.getXMLDocument(), "Username", "admin");

        ConfigException exception = null;
        try {
            apiManagerConf.updateConfig("\\", configCreator.createConfig());
        }
        catch (ConfigException e) {
            exception = e;
        }

        Assert.assertNotNull(exception);
        Assert.assertEquals("XPath expression '\\' evaluation error", exception.getMessage());
    }

    @Test
    public void testAddConfigBefore() throws Exception {
        XMLConfigIO apiManagerConfIO = new XMLConfigIO(apiManagerConfPath);
        ConfigOperator apiManagerConf = apiManagerConfIO.getOperator();

        XMLConfigCreator configCreator = new XMLConfigCreator(apiManagerConfIO.getXMLDocument(), "ClaimsRetrieverImplClass",
                                        "org.wso2.carbon.apimgt.impl.token.DefaultClaimsRetriever");

        // Validate order of child nodes
        Config parent = apiManagerConf.getConfig("//APIConsumerAuthentication");

        Node parentNode = ((XMLConfig) parent).getNode();
        int childPosition = getPositionOfChild(parentNode, "ConsumerDialectURI");
        TreeMap<Integer, Node> origSiblings = getChildren(parentNode);

        Assert.assertTrue(apiManagerConf.addConfig("//APIConsumerAuthentication/comment()[contains(., 'ConsumerDialectURI')]",
                configCreator.createConfig(), ConfigOperator.Position.BEFORE));

        Config config = apiManagerConf.getConfig("//APIConsumerAuthentication/ClaimsRetrieverImplClass");
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
        XMLConfigIO apiManagerConfIO = new XMLConfigIO(apiManagerConfPath);
        ConfigOperator apiManagerConf = apiManagerConfIO.getOperator();

        XMLConfigCreator configCreator = new XMLConfigCreator(apiManagerConfIO.getXMLDocument(),
                "ClaimsRetrieverImplClass", "org.wso2.carbon.apimgt.impl.token.DefaultClaimsRetriever");

        Assert.assertFalse(apiManagerConf.addConfig("/", configCreator.createConfig(),
                ConfigOperator.Position.BEFORE));

        // Validate if root element has changed
        Config root = apiManagerConf.getConfig("/");

        Assert.assertFalse(root.getName().equals("ClaimsRetrieverImplClass"));
    }

    @Test
    public void testAddConfigAtRootElement() throws Exception {
        XMLConfigIO apiManagerConfIO = new XMLConfigIO(apiManagerConfPath);
        ConfigOperator apiManagerConf = apiManagerConfIO.getOperator();

        XMLConfigCreator configCreator = new XMLConfigCreator(apiManagerConfIO.getXMLDocument(),
                "ClaimsRetrieverImplClass", "org.wso2.carbon.apimgt.impl.token.DefaultClaimsRetriever");

        Assert.assertFalse(apiManagerConf.addConfig("/", configCreator.createConfig(),
                ConfigOperator.Position.AT));

        // Validate if root element has changed
        Config root = apiManagerConf.getConfig("/");

        Assert.assertFalse(root.getName().equals("ClaimsRetrieverImplClass"));
    }

    @Test
    public void testAddConfigAfterRootElement() throws Exception {
        XMLConfigIO apiManagerConfIO = new XMLConfigIO(apiManagerConfPath);
        ConfigOperator apiManagerConf = apiManagerConfIO.getOperator();

        XMLConfigCreator configCreator = new XMLConfigCreator(apiManagerConfIO.getXMLDocument(),
                "ClaimsRetrieverImplClass", "org.wso2.carbon.apimgt.impl.token.DefaultClaimsRetriever");

        Assert.assertFalse(apiManagerConf.addConfig("/", configCreator.createConfig(),
                ConfigOperator.Position.AFTER));

        // Validate if root element has changed
        Config root = apiManagerConf.getConfig("/");

        Assert.assertFalse(root.getName().equals("ClaimsRetrieverImplClass"));
    }

    @Test
    public void testAddConfigBeforeRootElementWithStartingComments() throws Exception {
        XMLConfigIO commentsConfIO = new XMLConfigIO(commentsConfPath);
        ConfigOperator commentsConf = commentsConfIO.getOperator();

        XMLConfigCreator configCreator = new XMLConfigCreator(commentsConfIO.getXMLDocument(),
                "ClaimsRetrieverImplClass", "org.wso2.carbon.apimgt.impl.token.DefaultClaimsRetriever");

        Assert.assertFalse(commentsConf.addConfig("/", configCreator.createConfig(),
                ConfigOperator.Position.BEFORE));

        // Validate if root element has changed
        Config root = commentsConf.getConfig("/");

        Assert.assertFalse(root.getName().equals("ClaimsRetrieverImplClass"));
    }


    @Test
    public void testAddConfigAt() throws Exception {
        XMLConfigIO apiManagerConfIO = new XMLConfigIO(apiManagerConfPath);
        ConfigOperator apiManagerConf = apiManagerConfIO.getOperator();

        XMLConfigCreator configCreator = new XMLConfigCreator(apiManagerConfIO.getXMLDocument(), "APIMClaimCacheExpiry",
                "600");

        // Validate order of child nodes
        Config parent = apiManagerConf.getConfig("//APIConsumerAuthentication");

        Node parentNode = ((XMLConfig) parent).getNode();
        TreeMap<Integer, Node> origSiblings = getChildren(parentNode);
        int lastNodePosition = origSiblings.size() - 1;

        Assert.assertTrue(apiManagerConf.addConfig("//APIConsumerAuthentication", configCreator.createConfig(),
                ConfigOperator.Position.AT));

        Config config = apiManagerConf.getConfig("//APIConsumerAuthentication/APIMClaimCacheExpiry");
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
        XMLConfigIO apiManagerConfIO = new XMLConfigIO(apiManagerConfPath);
        ConfigOperator apiManagerConf = apiManagerConfIO.getOperator();

        XMLConfigCreator configCreator = new XMLConfigCreator(apiManagerConfIO.getXMLDocument(), "ClaimsRetrieverImplClass",
                "org.wso2.carbon.apimgt.impl.token.DefaultClaimsRetriever");

        // Validate order of child nodes
        Config parent = apiManagerConf.getConfig("//APIConsumerAuthentication");

        Node parentNode = ((XMLConfig) parent).getNode();
        int childPosition = getPositionOfChild(parentNode, "ClaimsRetrieverImplClass");
        TreeMap<Integer, Node> origSiblings = getChildren(parentNode);

        Assert.assertTrue(apiManagerConf.addConfig("//APIConsumerAuthentication/comment()[contains(., 'ClaimsRetrieverImplClass')]",
                configCreator.createConfig(), ConfigOperator.Position.AFTER));

        Config config = apiManagerConf.getConfig("//APIConsumerAuthentication/ClaimsRetrieverImplClass");
        Assert.assertNotNull(config);

        // Validate order of child nodes
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
        XMLConfigIO apiManagerConfIO = new XMLConfigIO(apiManagerConfPath);
        ConfigOperator apiManagerConf = apiManagerConfIO.getOperator();

        XMLConfigCreator configCreator = new XMLConfigCreator(apiManagerConfIO.getXMLDocument(), "Rubbish", "dump");

        Assert.assertFalse(apiManagerConf.addConfig("//AuthManager/UsernameS", configCreator.createConfig(),
                ConfigOperator.Position.AFTER));

        // Check if config got added with invalid xpath
        Config config = apiManagerConf.getConfig("//AuthManager/Rubbish");
        Assert.assertNull(config);
    }



    @Test
    public void testAddConfigInvalidXPath() throws Exception {
        XMLConfigIO apiManagerConfIO = new XMLConfigIO(apiManagerConfPath);
        ConfigOperator apiManagerConf = apiManagerConfIO.getOperator();

        XMLConfigCreator configCreator = new XMLConfigCreator(apiManagerConfIO.getXMLDocument(), "Username", "admin");

        ConfigException exception = null;
        try {
            apiManagerConf.addConfig("\\", configCreator.createConfig(), ConfigOperator.Position.AFTER);
        }
        catch (ConfigException e) {
            exception = e;
        }

        Assert.assertNotNull(exception);
        Assert.assertEquals("XPath expression '\\' evaluation error", exception.getMessage());
    }


    @Test
    public void testRemoveConfig() throws Exception {
        XMLConfigIO apiManagerConfIO = new XMLConfigIO(apiManagerConfPath);
        ConfigOperator apiManagerConf = apiManagerConfIO.getOperator();

        // Validate order of child nodes
        Config parent = apiManagerConf.getConfig("//AuthManager");

        Node parentNode = ((XMLConfig) parent).getNode();
        int childPosition = getPositionOfChild(parentNode, "Username");
        TreeMap<Integer, Node> origSiblings = getChildren(parentNode);

        Assert.assertTrue(apiManagerConf.removeConfig("//AuthManager/Username"));

        Config config = apiManagerConf.getConfig("//AuthManager/Username");
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
        XMLConfigIO apiManagerConfIO = new XMLConfigIO(apiManagerConfPath);
        ConfigOperator apiManagerConf = apiManagerConfIO.getOperator();

        // Validate order of child nodes
        Config parent = apiManagerConf.getConfig("//AuthManager");

        Node parentNode = ((XMLConfig) parent).getNode();
        TreeMap<Integer, Node> origSiblings = getChildren(parentNode);

        Assert.assertFalse(apiManagerConf.removeConfig("//AuthManager/UsernameS"));

        // Validate order of child nodes
        TreeMap<Integer, Node> newSiblings = getChildren(parentNode);

        Assert.assertTrue(newSiblings.size() == origSiblings.size());
    }

    @Test
    public void testRemoveConfigInvalidXPath() throws Exception {
        XMLConfigIO apiManagerConfIO = new XMLConfigIO(apiManagerConfPath);
        ConfigOperator apiManagerConf = apiManagerConfIO.getOperator();

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
        XMLConfigIO apiManagerConfIO = new XMLConfigIO(apiManagerConfPath);
        ConfigOperator apiManagerConf = apiManagerConfIO.getOperator();

        Assert.assertFalse(apiManagerConf.removeConfig("/"));

        // Validate if root element has changed
        Config root = apiManagerConf.getConfig("/");

        Assert.assertTrue(root.getName().equals("APIManager"));
    }
}