/*
 * Copyright 2021, Yahoo Inc.
 * Licensed under the Apache License, Version 2.0
 * See LICENSE file in project root for terms.
 */
package com.yahoo.elide.core.utils;


import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Scans a package for classes by looking at files in the classpath.
 */
public class DefaultClassScanner implements ClassScanner {

    private final Map<String, Set<Class<?>>> startupCache;

    /**
     * Primarily for tests so builds don't take forever. //TODO This is no longer needed with the lazy static cache
     */
    private static DefaultClassScanner _instance;

    /**
     * For use within a container where class scanning happens at compile time.
     * @param startupCache Maps annotations (in CACHE_ANNOTATIONS) to classes.
     */
    public DefaultClassScanner(Map<String, Set<Class<?>>> startupCache) {
        this.startupCache = startupCache;
    }

    /**
     * For use within a container where class scanning happens at boot time.
     */
    public DefaultClassScanner() {
        this.startupCache = ClassScannerCache.getInstance();
    }

    @Override
    public Set<Class<?>> getAnnotatedClasses(Package toScan, Class<? extends Annotation> annotation) {
        return getAnnotatedClasses(toScan.getName(), annotation);
    }

    @Override
    public Set<Class<?>> getAnnotatedClasses(String packageName, Class<? extends Annotation> annotation) {
        return startupCache.get(annotation.getCanonicalName()).stream()
                .filter(clazz ->
                        clazz.getPackage().getName().equals(packageName)
                                || clazz.getPackage().getName().startsWith(packageName + "."))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public Set<Class<?>> getAnnotatedClasses(List<Class<? extends Annotation>> annotations,
            FilterExpression filter) {
        Set<Class<?>> result = new LinkedHashSet<>();

        for (Class<? extends Annotation> annotation : annotations) {
            result.addAll(startupCache.get(annotation.getCanonicalName()).stream()
                    .filter(filter::include)
                    .collect(Collectors.toCollection(LinkedHashSet::new)));
        }

        return result;
    }

    @Override
    public Set<Class<?>> getAnnotatedClasses(List<Class<? extends Annotation>> annotations) {
        return getAnnotatedClasses(annotations, clazz -> true);
    }

    @Override
    public Set<Class<?>> getAnnotatedClasses(Class<? extends Annotation> ... annotations) {
        return getAnnotatedClasses(Arrays.asList(annotations));
    }

    @Override
    public Set<Class<?>> getAllClasses(String packageName) {
        try (ScanResult scanResult = new ClassGraph()
                .enableClassInfo().acceptPackages(packageName).scan()) {
            return scanResult.getAllClasses().stream()
                    .map((ClassInfo::loadClass))
                    .collect(Collectors.toCollection(LinkedHashSet::new));
        }
    }

    /**
     * Primarily for tests to only create a single instance of this to reduce build times.  Production code
     * will use DI to accomplish the same.
     * @return The single instance.
     */
    public static synchronized DefaultClassScanner getInstance() {
        if (_instance == null) {
            _instance = new DefaultClassScanner();
        }
        return _instance;
    }
}
