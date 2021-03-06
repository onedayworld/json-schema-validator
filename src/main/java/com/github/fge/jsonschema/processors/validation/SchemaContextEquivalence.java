/*
 * Copyright (c) 2014, Francis Galiegue (fgaliegue@gmail.com)
 *
 * This software is dual-licensed under:
 *
 * - the Lesser General Public License (LGPL) version 3.0 or, at your option, any
 *   later version;
 * - the Apache Software License (ASL) version 2.0.
 *
 * The text of both licenses is available under the src/resources/ directory of
 * this project (under the names LGPL-3.0.txt and ASL-2.0.txt respectively).
 *
 * Direct link to the sources:
 *
 * - LGPL 3.0: https://www.gnu.org/licenses/lgpl-3.0.txt
 * - ASL 2.0: http://www.apache.org/licenses/LICENSE-2.0.txt
 */

package com.github.fge.jsonschema.processors.validation;

import com.github.fge.jsonschema.core.tree.SchemaTree;
import com.github.fge.jsonschema.core.util.equivalence.SchemaTreeEquivalence;
import com.github.fge.jsonschema.processors.data.SchemaContext;
import com.google.common.base.Equivalence;

/**
 * Equivalence for schema contexts
 *
 * <p>This is used by {@link ValidationChain} and {@link ValidationProcessor} to
 * cache computation results. Two schema contexts are considered equivalent if:
 * </p>
 *
 * <ul>
 *     <li>schema trees are considered equivalent,</li>
 *     <li>and the type of the instance is the same.</li>
 * </ul>
 *
 * @see SchemaTreeEquivalence
 */
public final class SchemaContextEquivalence
    extends Equivalence<SchemaContext>
{
    private static final Equivalence<SchemaContext> INSTANCE
        = new SchemaContextEquivalence();

    private static final Equivalence<SchemaTree> TREE_EQUIVALENCE
        = SchemaTreeEquivalence.getInstance();

    public static Equivalence<SchemaContext> getInstance()
    {
        return INSTANCE;
    }

    @Override
    protected boolean doEquivalent(final SchemaContext a, final SchemaContext b)
    {
        return TREE_EQUIVALENCE.equivalent(a.getSchema(), b.getSchema())
            && a.getInstanceType() == b.getInstanceType();
    }

    @Override
    protected int doHash(final SchemaContext t)
    {
        return 31 * TREE_EQUIVALENCE.hash(t.getSchema())
            + t.getInstanceType().hashCode();
    }
}
