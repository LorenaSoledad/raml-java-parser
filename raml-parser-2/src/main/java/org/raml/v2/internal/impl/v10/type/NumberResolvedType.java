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
package org.raml.v2.internal.impl.v10.type;

import org.raml.yagi.framework.grammar.rule.ErrorNodeFactory;
import org.raml.yagi.framework.nodes.ErrorNode;
import org.raml.yagi.framework.nodes.Node;
import org.raml.yagi.framework.nodes.SimpleTypeNode;
import org.raml.yagi.framework.nodes.snakeyaml.SYArrayNode;
import org.raml.v2.internal.impl.commons.nodes.TypeDeclarationNode;
import org.raml.v2.internal.impl.commons.type.ResolvedType;
import org.raml.yagi.framework.util.NodeSelector;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static java.math.BigDecimal.ROUND_CEILING;
import static java.math.BigDecimal.ROUND_FLOOR;
import static java.math.BigDecimal.ROUND_DOWN;
import static java.math.BigDecimal.ROUND_UNNECESSARY;
import static java.math.BigDecimal.ZERO;
import static java.math.BigDecimal.ONE;

public class NumberResolvedType extends XmlFacetsCapableType
{
    private Number minimum;
    private Number maximum;
    private Number multiple;
    private String format;
    private List<Number> enums = new ArrayList<>();

    public NumberResolvedType(TypeDeclarationNode from)
    {
        super(from);
    }

    public NumberResolvedType(TypeDeclarationNode declarationNode, XmlFacets xmlFacets, Number minimum, Number maximum, Number multiple, String format)
    {
        super(declarationNode, xmlFacets);
        this.minimum = minimum;
        this.maximum = maximum;
        this.multiple = multiple;
        this.format = format;
    }

    public NumberResolvedType copy()
    {
        return new NumberResolvedType(getTypeDeclarationNode(), getXmlFacets().copy(), minimum, maximum, multiple, format);
    }

    @Override
    public ResolvedType overwriteFacets(TypeDeclarationNode from)
    {
        final NumberResolvedType result = copy();
        result.setMinimum(NodeSelector.selectNumberValue("minimum", from));
        result.setMaximum(NodeSelector.selectNumberValue("maximum", from));
        result.setMultiple(NodeSelector.selectNumberValue("multipleOf", from));
        result.setFormat(NodeSelector.selectStringValue("format", from));
        result.setEnums(getEnumValues(from));
        return overwriteFacets(result, from);
    }

    @Nonnull
    private List<Number> getEnumValues(Node typeNode)
    {

        Node values = typeNode.get("enum");
        List<Number> enumValues = new ArrayList<>();
        if (values != null && values instanceof SYArrayNode)
        {
            for (Node node : values.getChildren())
            {
                enumValues.add((Number) ((SimpleTypeNode) node).getValue());
            }
        }
        return enumValues;
    }

    @Override
    public ResolvedType mergeFacets(ResolvedType with)
    {
        final NumberResolvedType result = copy();
        if (with instanceof NumberResolvedType)
        {
            NumberResolvedType numberTypeDefinition = (NumberResolvedType) with;
            result.setMinimum(numberTypeDefinition.getMinimum());
            result.setMaximum(numberTypeDefinition.getMaximum());
            result.setMultiple(numberTypeDefinition.getMultiple());
            result.setFormat(numberTypeDefinition.getFormat());
            result.setEnums(numberTypeDefinition.getEnums());
        }
        return mergeFacets(result, with);
    }

    @Override
    public ErrorNode validateFacets()
    {
        BigDecimal min = minimum != null? new BigDecimal(minimum.toString()) : new BigDecimal(Double.MIN_VALUE);
        BigDecimal max = maximum != null? new BigDecimal(maximum.toString()) : new BigDecimal(Double.MAX_VALUE);
        BigDecimal mult = multiple != null? new BigDecimal(multiple.toString()) : null;

        // Checking conflicts between the minimum and maximum facets if both are set
        if (max.compareTo(min) < 0 )
        {
            return ErrorNodeFactory.createInvalidFacet(
                    getTypeName(),
                    "maximum must be greater or equal than minimum");
        }


        // It must be at least one multiple of the number between the valid range
        if (mult != null && !hasValidMultiplesInRange(min, max, mult))
        {
            return ErrorNodeFactory.createInvalidFacet(
                    getTypeName(),
                    "It must be at least one multiple of " + mult + " in the given range");
        }


        // For each value in the list, it must be between minimum and maximum
        for (Number thisEnum : enums)
        {
            BigDecimal value = new BigDecimal(thisEnum.toString());

            if (value.compareTo(min) < 0 || value.compareTo(max) > 0)
            {
                return ErrorNodeFactory.createInvalidFacet(
                        getTypeName(),
                        "enums values must be between " + minimum + " and " + maximum);
            }

            if(mult != null && value.remainder(mult).compareTo(BigDecimal.ZERO) != 0)
            {
                return ErrorNodeFactory.createInvalidFacet(
                        getTypeName(),
                        "enums values must be all values multiple of " + mult);
            }
        }

        return null;
    }

    private boolean hasValidMultiplesInRange(BigDecimal min, BigDecimal max, BigDecimal mult)
    {
        // Zero is multiple of every number
        if (mult.compareTo(BigDecimal.ZERO) == 0)
        {
            return true;
        }

        BigDecimal divideMax = max.divide(mult, 0, ROUND_DOWN);
        BigDecimal divideMin = min.divide(mult, 0, ROUND_CEILING);
        BigDecimal subtract = divideMax.subtract(divideMin);
        BigDecimal plusOne = subtract.add(ONE);
        BigDecimal max0 = plusOne.max(ZERO);
        BigDecimal numberOfMultiplesInRange = max0.setScale(0, ROUND_DOWN);

        return numberOfMultiplesInRange.compareTo(ZERO) > 0;
    }

    @Override
    public <T> T visit(TypeVisitor<T> visitor)
    {
        return visitor.visitNumber(this);
    }

    public Number getMinimum()
    {
        return minimum;
    }


    public List<Number> getEnums()
    {
        return enums;
    }

    public void setEnums(List<Number> enums)
    {
        if (enums != null && !enums.isEmpty())
        {
            this.enums = enums;
        }
    }

    private void setMinimum(Number minimum)
    {
        if (minimum != null)
        {
            this.minimum = minimum;
        }
    }

    public Number getMaximum()
    {
        return maximum;
    }

    private void setMaximum(Number maximum)
    {
        if (maximum != null)
        {
            this.maximum = maximum;
        }
    }

    public Number getMultiple()
    {
        return multiple;
    }

    private void setMultiple(Number multiple)
    {
        if (multiple != null)
        {
            this.multiple = multiple;
        }
    }

    public String getFormat()
    {
        return format;
    }

    private void setFormat(String format)
    {
        if (format != null)
        {
            this.format = format;
        }
    }
}
