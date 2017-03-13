/*
 * Copyright 2013 (c) MuleSoft, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package org.raml.v2.internal.impl.v10.nodes.factory;

import org.raml.v2.internal.impl.commons.nodes.OverlayableStringNode;
import org.raml.v2.internal.impl.v10.nodes.ArrayTypeExpressionNode;
import org.raml.yagi.framework.grammar.rule.DefaultValue;
import org.raml.yagi.framework.nodes.DefaultPosition;
import org.raml.yagi.framework.nodes.Node;
import org.raml.yagi.framework.nodes.SimpleTypeNode;

import javax.annotation.Nullable;
import java.util.List;

public class ArrayItemsDefaultValue implements DefaultValue
{
    @Nullable
    @Override
    public Node getDefaultValue(Node parent)
    {
        // We have to apply the default value if and only if the array is defined in the following form:
        // type: TheType[]

        Node type = parent.get("type");
        if (type instanceof ArrayTypeExpressionNode)
        {
            List<Node> children = type.getChildren();

            Node child = children.get(0);
            if (child instanceof SimpleTypeNode)
            {
                String of = (String) ((SimpleTypeNode) children.get(0)).getValue();

                final OverlayableStringNode items = new OverlayableStringNode(of);
                items.setStartPosition(DefaultPosition.emptyPosition());
                items.setEndPosition(DefaultPosition.emptyPosition());
                return items;
            }
        }

        return null;
    }
}
