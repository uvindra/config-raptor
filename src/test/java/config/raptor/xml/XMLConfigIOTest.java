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

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

public class XMLConfigIOTest {
    private static final String API_MANAGER_CONF = "api-manager.xml";
    private static final String UPDATED_API_MANAGER_CONF = "updated-api-manager";
    private String apiManagerConfPath;
    private File updatedApiManagerConfFile;

    @BeforeMethod
    public void setUp() throws Exception {
        File apiManagerConfFile = new File(Thread.currentThread().getContextClassLoader().
                getResource(API_MANAGER_CONF).getFile());
        apiManagerConfPath = apiManagerConfFile.getAbsolutePath();

        updatedApiManagerConfFile = new File(System.getProperty("user.dir") + File.separator + UPDATED_API_MANAGER_CONF);
        updatedApiManagerConfFile.createNewFile();

        FileChannel source = null;
        FileChannel destination = null;

        try {
            source = new FileInputStream(apiManagerConfFile).getChannel();
            destination = new FileOutputStream(updatedApiManagerConfFile).getChannel();
            destination.transferFrom(source, 0, source.size());
        }
        finally {
            if(source != null) {
                source.close();
            }
            if(destination != null) {
                destination.close();
            }
        }
    }

    @AfterMethod
    public void tearDown() {
        updatedApiManagerConfFile.delete();
    }

    @Test
    public void testGetOperator() throws Exception {
        XMLConfigIO configIO = new XMLConfigIO(apiManagerConfPath);

        Assert.assertNotNull(configIO.getConfigOperator());
    }
/*
    @Test
    public void testSave() throws Exception {
        XMLConfigIO configIO = new XMLConfigIO(updatedApiManagerConfFile.getAbsolutePath());

        // Create new element
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document doc = docBuilder.newDocument();
        Element newElement = doc.createElement("Password");
        newElement.setTextContent("xsaxlj");
        final Config config = new XMLConfig("Password", newElement);

        ConfigOperator configOperator = configIO.getConfigOperator();
        Assert.assertTrue(configOperator.updateConfig("//APIGateway/Environments/Environment/Password", config));

        configIO.save();

        XMLConfigIO updatedConfigIO = new XMLConfigIO(updatedApiManagerConfFile.getAbsolutePath());
        configOperator = updatedConfigIO.getConfigOperator();
        Config updatedConfig = configOperator.getConfig("//APIGateway/Environments/Environment/Password");

        Assert.assertTrue(updatedConfig.equals(config));
    }*/
}
