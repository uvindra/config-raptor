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

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class XMLConfig implements Config {

    private String name;
    private Node value;

    XMLConfig(String name, Node value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public String getValue() {
        return value.getTextContent();
    }

    @Override
    public String getName() {
        return name;
    }




    @Override public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof XMLConfig))
            return false;

        XMLConfig config = (XMLConfig) o;

        if (!name.equals(config.name))
            return false;

        if (!value.getTextContent().equals(config.value.getTextContent()))
            return false;

        final NamedNodeMap thisAttributes = value.getAttributes();
        final NamedNodeMap otherAttributes= config.value.getAttributes();

        if (thisAttributes.getLength() != otherAttributes.getLength())
            return false;

        for (int i = 0; i < thisAttributes.getLength(); ++i) {
            if (!thisAttributes.item(i).getNodeName().equals(otherAttributes.item(i).getNodeName()))
                 return false;

            if (!thisAttributes.item(i).getNodeValue().equals(otherAttributes.item(i).getNodeValue()))
                return false;
        }

        return true;
    }

    @Override public int hashCode() {
        int result = 17;
        result = 31 * result + name.hashCode();
        result = 31 * result + value.hashCode();
        return result;
    }

    Node getNode() { return this.value; }
}
