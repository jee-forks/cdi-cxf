package com.github.rmannibucau.cdi.cxf.impl;

public final class ClassLoaders {
    private ClassLoaders() {
        // no-op
    }

    public static ClassLoader current() {
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();
        if (loader != null) {
            return loader;
        }
        return ClassLoaders.class.getClassLoader();
    }
}
