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
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

public class XMLConfigCreatorTest {
    private static final String API_MANAGER_CONF = "api-manager.xml";
    private Document apiManagerConfDoc;

    @BeforeMethod
    public void setUp() throws Exception {
        File apiManagerConfFile = new File(Thread.currentThread().getContextClassLoader().
                getResource(API_MANAGER_CONF).getFile());

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        apiManagerConfDoc = builder.parse(apiManagerConfFile.getAbsolutePath());
    }

    @Test
    public void testCreateConfigWithNameOnly() throws Exception {

        XMLConfigCreator configCreator = new XMLConfigCreator(apiManagerConfDoc);

        Node config = configCreator.createConfig("<loner/>");

        Assert.assertEquals("loner", config.getNodeName());
        Assert.assertEquals("", config.getTextContent());

        config = configCreator.createConfig("<loner></loner>");

        Assert.assertEquals("loner", config.getNodeName());
        Assert.assertEquals("", config.getTextContent());
    }

    @Test
    public void testCreateConfigWithAttributesOnly() throws Exception {
        XMLConfigCreator configCreator = new XMLConfigCreator(apiManagerConfDoc);

        Node config = configCreator.createConfig("<complicated mood=\"good\" view=\"neutral\" temperament=\"aggressive\"/>");

        Assert.assertEquals("complicated", config.getNodeName());
        Assert.assertEquals("", config.getTextContent());
        Assert.assertTrue(config.hasAttributes());


        NamedNodeMap attributes = config.getAttributes();

        Assert.assertEquals("good", attributes.getNamedItem("mood").getNodeValue());
        Assert.assertEquals("neutral", attributes.getNamedItem("view").getNodeValue());
        Assert.assertEquals("aggressive", attributes.getNamedItem("temperament").getNodeValue());
    }

    @Test
    public void testCreateConfigWithValue() throws Exception {
        XMLConfigCreator configCreator = new XMLConfigCreator(apiManagerConfDoc);

        Node config = configCreator.createConfig("<singular>uno</singular>");

        Assert.assertEquals("singular", config.getNodeName());
        Assert.assertEquals("uno", config.getTextContent());
    }

    @Test
    public void testCreateConfigWithValueAndAttributes() throws Exception {
        XMLConfigCreator configCreator = new XMLConfigCreator(apiManagerConfDoc);

        Node config = configCreator.createConfig("<packing height=\"100\" length=\"230\" width=\"40\">multiple</packing>");

        Assert.assertEquals("packing", config.getNodeName());
        Assert.assertEquals("multiple", config.getTextContent());
        Assert.assertTrue(config.hasAttributes());

        NamedNodeMap attributes = config.getAttributes();

        Assert.assertEquals("100", attributes.getNamedItem("height").getNodeValue());
        Assert.assertEquals("230", attributes.getNamedItem("length").getNodeValue());
        Assert.assertEquals("40", attributes.getNamedItem("width").getNodeValue());
    }

    @Test(dataProvider = "invalidXML")
    public void testCreateConfigWithInvalidXMLString(String xmlInput) {
        XMLConfigCreator configCreator = new XMLConfigCreator(apiManagerConfDoc);

        try {
            Node config = configCreator.createConfig(xmlInput);
            // Force failure if this is executed because that means  exception wasn't thrown
            Assert.assertTrue(false);
        }
        catch (ConfigException e) {
           Assert.assertEquals("XML parsing error when parsing xml string '"+ xmlInput +"'", e.getMessage());
        }
    }

    @DataProvider(name = "invalidXML")
    public Object[][] invalidXML() {
        Object[][] xmlInputs = new Object[5][1];

        xmlInputs[0][0] = "notags";
        xmlInputs[1][0] = "";
        xmlInputs[2][0] = "<noend";
        xmlInputs[3][0] = "nostart/>";
        xmlInputs[4][0] = "<bad>/>";

        return xmlInputs;
    }
}