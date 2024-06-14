package com.hibob.anyim.common.utils;

import org.springframework.util.ReflectionUtils;

import java.util.HashMap;
import java.util.Map;

public final class BeanUtil {

    private BeanUtil() {
    }

    public static <T> T copyProperties(Object source, Class<T> targetClass) {
        if (source == null) {
            return null;
        }

        try {
            T target = targetClass.newInstance();
            org.springframework.beans.BeanUtils.copyProperties(source, target);
            return target;
        } catch (Exception e) {
            ReflectionUtils.handleReflectionException(e);
            return null;
        }
    }

    public static Map<String, Object> objectToMap(Object obj) throws IllegalAccessException {
        Map<String, Object> map = new HashMap<>();
        Class<?> clazz = obj.getClass();
        for (java.lang.reflect.Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            map.put(field.getName(), field.get(obj));
        }
        return map;
    }

}
