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
import org.raml.yagi.framework.nodes.StringNode;
import org.raml.yagi.framework.nodes.snakeyaml.SYArrayNode;
import org.raml.v2.internal.impl.commons.type.ResolvedType;
import org.raml.v2.internal.impl.commons.nodes.TypeDeclarationNode;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static org.raml.yagi.framework.util.NodeSelector.selectIntValue;
import static org.raml.yagi.framework.util.NodeSelector.selectStringValue;

public class StringResolvedType extends XmlFacetsCapableType
{
    private static final int DEFAULT_MIN_LENGTH = 0;
    private static final int DEFAULT_MAX_LENGTH = 2147483647;

    private Integer minLength;
    private Integer maxLength;
    private String pattern;
    private List<String> enums = new ArrayList<>();

    public StringResolvedType(TypeDeclarationNode from)
    {
        super(from);
    }

    public StringResolvedType(TypeDeclarationNode declarationNode, XmlFacets xmlFacets, Integer minLength, Integer maxLength, String pattern, List<String> enums)
    {
        super(declarationNode, xmlFacets);
        this.minLength = minLength;
        this.maxLength = maxLength;
        this.pattern = pattern;
        this.enums = enums;
    }

    protected StringResolvedType copy()
    {
        return new StringResolvedType(getTypeDeclarationNode(), getXmlFacets(), minLength, maxLength, pattern, enums);
    }


    @Override
    public ResolvedType overwriteFacets(TypeDeclarationNode from)
    {
        final StringResolvedType result = copy();
        result.setMinLength(selectIntValue("minLength", from));
        result.setMaxLength(selectIntValue("maxLength", from));
        result.setPattern(selectStringValue("pattern", from));
        result.setEnums(getEnumValues(from));
        return overwriteFacets(result, from);
    }

    @Nonnull
    private List<String> getEnumValues(Node typeNode)
    {

        Node values = typeNode.get("enum");
        List<String> enumValues = new ArrayList<>();
        if (values != null && values instanceof SYArrayNode)
        {
            for (Node node : values.getChildren())
            {
                enumValues.add(((StringNode) node).getValue());
            }
        }
        return enumValues;
    }

    @Override
    public ResolvedType mergeFacets(ResolvedType with)
    {
        final StringResolvedType result = copy();
        if (with instanceof StringResolvedType)
        {
            final StringResolvedType stringTypeDefinition = (StringResolvedType) with;
            result.setMaxLength(stringTypeDefinition.getMaxLength());
            result.setMinLength(stringTypeDefinition.getMinLength());
            result.setPattern(stringTypeDefinition.getPattern());
            result.setEnums(stringTypeDefinition.getEnums());
        }
        return mergeFacets(result, with);
    }

    @Override
    public ErrorNode validateFacets()
    {
        int minimumLength = minLength != null? minLength : DEFAULT_MIN_LENGTH;
        int maximumLength = maxLength != null? maxLength : DEFAULT_MAX_LENGTH;

        // Validating conflicts between the length facets
        if (maximumLength < minimumLength)
        {
            return ErrorNodeFactory.createInvalidFacet(
                    getTypeName(),
                    "maxLength must be greater or equal than minLength");
        }

        // For each enum in the list, it must be between the range defined by minLength and maxLength
        for (String thisEnum : enums)
        {
            if (thisEnum.length() < minimumLength || thisEnum.length() > maximumLength)
            {
                return ErrorNodeFactory.createInvalidFacet(
                        getTypeName(),
                        "enums must be between " + minimumLength + " and " + maximumLength + " characters");
            }
        }

        return null;
    }

    @Override
    public <T> T visit(TypeVisitor<T> visitor)
    {
        return visitor.visitString(this);
    }

    private void setPattern(String pattern)
    {
        if (pattern != null)
        {
            this.pattern = pattern;
        }
    }

    private void setMinLength(Integer minLength)
    {
        if (minLength != null)
        {
            this.minLength = minLength;
        }
    }

    private void setMaxLength(Integer maxLength)
    {
        if (maxLength != null)
        {
            this.maxLength = maxLength;
        }
    }

    public List<String> getEnums()
    {
        return enums;
    }

    public void setEnums(List<String> enums)
    {
        if (enums != null && !enums.isEmpty())
        {
            this.enums = enums;
        }
    }

    @Nullable
    public Integer getMinLength()
    {
        return minLength;
    }

    @Nullable
    public Integer getMaxLength()
    {
        return maxLength;
    }

    @Nullable
    public String getPattern()
    {
        return pattern;
    }
}
