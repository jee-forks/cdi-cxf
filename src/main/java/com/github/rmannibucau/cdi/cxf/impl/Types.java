package com.github.rmannibucau.cdi.cxf.impl;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public final class Types {
    private Types() {
        // no-op
    }

    public static <T> Class<T> type(Type type) {
        if (type instanceof Class<?>) {
            return (Class<T>) type;
        } else if (type instanceof ParameterizedType && ((ParameterizedType) type).getRawType() instanceof Class<?>) {
            return (Class<T>) ((ParameterizedType) type).getRawType();
        }
        return null;
    }
}
