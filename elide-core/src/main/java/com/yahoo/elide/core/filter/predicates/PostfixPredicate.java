/*
 * Copyright 2019, Yahoo Inc.
 * Licensed under the Apache License, Version 2.0
 * See LICENSE file in project root for terms.
 */
package com.yahoo.elide.core.filter.predicates;

import com.yahoo.elide.core.Path;
import com.yahoo.elide.core.Path.PathElement;
import com.yahoo.elide.core.filter.Operator;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * POSTFIX Predicate class.
 */
public class PostfixPredicate extends FilterPredicate {

    public PostfixPredicate(Path path, List<Object> values) {
        super(path, Operator.POSTFIX, values);
    }

    @SafeVarargs
    public <T> PostfixPredicate(Path path, T... a) {
        this(path, Arrays.asList(a));
    }

    public PostfixPredicate(PathElement pathElement, List<Object> values) {
        this(new Path(Collections.singletonList(pathElement)), values);
    }

    @SafeVarargs
    public <T> PostfixPredicate(PathElement pathElement, T... a) {
        this(pathElement, Arrays.asList(a));
    }
}
