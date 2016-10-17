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
package org.raml.yagi.framework.grammar.rule;

import org.raml.yagi.framework.nodes.FloatingNode;
import org.raml.yagi.framework.nodes.IntegerNode;
import org.raml.yagi.framework.nodes.Node;
import org.raml.yagi.framework.nodes.SimpleTypeNode;
import org.raml.yagi.framework.suggester.ParsingContext;
import org.raml.yagi.framework.suggester.Suggestion;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static java.math.BigDecimal.ZERO;

public class DivisorValueRule extends Rule
{

    private Number divisorValue;

    public DivisorValueRule(Number divisorValue)
    {
        this.divisorValue = divisorValue;
    }

    @Nonnull
    @Override
    public List<Suggestion> getSuggestions(Node node, ParsingContext context)
    {
        return Collections.emptyList();
    }

    @Override
    public boolean matches(@Nonnull Node node)
    {
        if (node instanceof IntegerNode)
        {
            if (divisorValue.longValue() == 0 && Long.valueOf(0).equals(((IntegerNode) node).getValue()))
            {
                return true;
            }
            return divisorValue.longValue() != 0 && ((IntegerNode) node).getValue() % divisorValue.longValue() == 0L;
        }
        else if (node instanceof FloatingNode)
        {
            BigDecimal value = new BigDecimal(divisorValue.toString());

            if (value.compareTo(ZERO) == 0 && ((FloatingNode) node).getValue().compareTo(ZERO) == 0)
            {
                return true;
            }
            return !(value.compareTo(ZERO) == 0) && (((FloatingNode) node).getValue().remainder(value).compareTo(ZERO) == 0);
        }

        return false;
    }

    @Override
    public Node apply(@Nonnull Node node)
    {
        if (matches(node))
        {
            return createNodeUsingFactory(node, ((SimpleTypeNode) node).getValue());
        }
        else
        {
            if ((node instanceof IntegerNode && divisorValue.longValue() == 0) ||
                node instanceof FloatingNode && divisorValue.floatValue() == 0f)
            {
                return ErrorNodeFactory.createInvalidDivisorValue();
            }
            return ErrorNodeFactory.createInvalidMultipleOfValue(divisorValue);
        }
    }

    @Override
    public String getDescription()
    {
        return "Multiple of value";
    }
}
