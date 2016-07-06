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

public class XMLUtil {

    static void updateNode(Node currentNode, final Node newNode) {
        currentNode.setTextContent(newNode.getTextContent());

        NamedNodeMap currentAttributes = currentNode.getAttributes();
        for (int i = 0; i < currentAttributes.getLength(); ++i) {
            ((Element)currentNode).removeAttributeNode((Attr) currentAttributes.item(i));
        }

        NamedNodeMap newAttributes = newNode.getAttributes();
        for (int i = 0; i < newAttributes.getLength(); ++i) {
            ((Element)currentNode).setAttributeNode((Attr) newAttributes.item(i));
        }
    }

    static boolean removeNode(Node node) {
        Node parent = node.getParentNode();

        if (parent != null) {
            parent.removeChild(node);

            return true;
        }

        return false;
    }
}
