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
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;

public class XMLConfigFileTest {
    private static final String API_MANAGER_CONF = "api-manager.xml";
    private String apiManagerConfPath;

    @BeforeMethod
    public void setUp() throws Exception {
        File apiManagerConfFile = new File(Thread.currentThread().getContextClassLoader().
                                                                getResource(API_MANAGER_CONF).getFile());
        apiManagerConfPath = apiManagerConfFile.getAbsolutePath();
    }

    @Test
    public void testIsConfigExists() throws Exception {
        XMLConfigFile apiManagerConf = new XMLConfigFile(apiManagerConfPath);

        boolean isExists = apiManagerConf.isConfigExists("//DataSourceName");
        Assert.assertTrue(isExists);
    }

    @Test
    public void testSearchNonExistingElement() throws Exception {
        XMLConfigFile apiManagerConf = new XMLConfigFile(apiManagerConfPath);

        boolean isExists = apiManagerConf.isConfigExists("blah");
        Assert.assertFalse(isExists);

        isExists = apiManagerConf.isConfigExists("//dataSourceName");
        Assert.assertFalse(isExists);
    }

    @Test
    public void testIsConfigExistsAsComment() throws Exception {
        XMLConfigFile apiManagerConf = new XMLConfigFile(apiManagerConfPath);

        boolean isExists = apiManagerConf.isConfigExists("//APIConsumerAuthentication/comment()[contains(., 'ClaimsRetrieverImplClass')]");
        Assert.assertTrue(isExists);

        isExists = apiManagerConf.isConfigExists("//APIConsumerAuthentication/comment()[contains(., 'ConsumerDialectURI>http://wso2.org/claims</ConsumerDialectURI')]");
        Assert.assertTrue(isExists);
    }

}