package com.caicui.commons.common.utils;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.commons.lang.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

//~--- JDK imports ------------------------------------------------------------

/**
 * 工具类 - 反射
 */
public class ReflectionUtil {

    /**
     * 调用Getter方法
     *
     * @param object       对象
     * @param propertyName 属性名称
     */
    public static Object invokeGetterMethod(Object object, String propertyName) {
        String getterMethodName = "get" + StringUtils.capitalize(propertyName);
        try {
            Method getterMethod = object.getClass().getMethod(getterMethodName);

            return getterMethod.invoke(object);
        } catch (Exception e) {
            e.printStackTrace();

            return null;
        }
    }

    /**
     * 调用Setter方法
     *
     * @param object        对象
     * @param propertyName  属性名称
     * @param propertyValue 属性值
     */
    public static void invokeSetterMethod(Object object, String propertyName, Object propertyValue) {
        Class<?> setterMethodClass = propertyValue.getClass();
        invokeSetterMethod(object, propertyName, propertyValue, setterMethodClass);
    }

    /**
     * 调用Setter方法
     *
     * @param object            对象
     * @param propertyName      属性名称
     * @param propertyValue     属性值
     * @param setterMethodClass 参数类型
     */
    public static void invokeSetterMethod(Object object, String propertyName, Object propertyValue, Class<?> setterMethodClass) {
        String setterMethodName = "set" + StringUtils.capitalize(propertyName);
        try {
            Method setterMethod = object.getClass().getMethod(setterMethodName, setterMethodClass);
            setterMethod.invoke(object, propertyValue);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取对象属性值,无视private/protected/getter
     *
     * @param object    对象
     * @param fieldName 属性名称
     */
    public static Object getFieldValue(Object object, String fieldName) {
        Field field = getAccessibleField(object, fieldName);
        if (field == null) {
            throw new IllegalArgumentException("Could not find field " + fieldName);
        }
        Object result = null;
        try {
            result = field.get(object);
        } catch (IllegalAccessException e) {
        }

        return result;
    }

    /**
     * 设置对象属性值,无视private/protected/setter
     *
     * @param object    对象
     * @param fieldName 属性名称
     */
    public static void setFieldValue(Object object, String fieldName, Object value) {
        Field field = getAccessibleField(object, fieldName);
        if (field == null) {
            throw new IllegalArgumentException("Could not find field " + fieldName);
        }
        try {
            field.set(object, value);
        } catch (IllegalAccessException e) {
        }
    }

    private static Field getAccessibleField(final Object object, final String fieldName) {
        for (Class<?> superClass = object.getClass(); superClass != Object.class; superClass = superClass.getSuperclass()) {
            try {
                Field field = superClass.getDeclaredField(fieldName);
                field.setAccessible(true);

                return field;
            } catch (NoSuchFieldException e) {
            }
        }
        return null;
    }


    public static List<Field> getFields(Class clazz) {
        Set<Field> fieldSet = new HashSet<Field>();
        if (!clazz.getSuperclass().equals(Object.class)) {
            Field[] fields = clazz.getFields();
            final List<Field> fieldArrayList = getFields(clazz.getSuperclass());
            fieldSet.addAll(fieldArrayList);
            fieldSet.addAll(filterField(fields));
        }
        final List<Field> fields = new ArrayList<Field>();
        fields.addAll(fieldSet);

        return fields;
    }

    public static List<Field> getDeclaredFields(Class clazz) {
        Set<Field> fieldSet = new HashSet<Field>();
        if (!clazz.getSuperclass().equals(Object.class)) {
            Field[] fields = clazz.getDeclaredFields();
            final List<Field> fieldArrayList = getDeclaredFields(clazz.getSuperclass());
            fieldSet.addAll(fieldArrayList);
            fieldSet.addAll(filterField(fields));
        }
        final List<Field> fields = new ArrayList<Field>();
        fields.addAll(fieldSet);
        return fields;
    }

    public static List<Method> getMethods(Class clazz) {
        Set<Method> methodHashSet = new HashSet<Method>();
        if (!clazz.getSuperclass().equals(Object.class)) {
            Method[] methods = clazz.getMethods();
            List<Method> methodList = getMethods(clazz.getSuperclass());
            methodHashSet.addAll(filterMethods(methods));
            methodHashSet.addAll(methodList);
        }

        return toList(methodHashSet);
    }

    public static List<Method> getDeclaredMethods(Class clazz) {
        Set<Method> methodHashSet = new HashSet<Method>();
        Method[] methods = clazz.getDeclaredMethods();
        if (!clazz.getSuperclass().equals(Object.class)) {
            List<Method> fieldArrayList = getDeclaredMethods(clazz.getSuperclass());
            methodHashSet.addAll(fieldArrayList);
        }
        methodHashSet.addAll(filterMethods(methods));
        return toList(methodHashSet);
    }

    /**
     * 将集合转换成列表
     *
     * @param set
     * @param <T>
     * @return
     */
    public static <T> List<T> toList(Set<T> set) {
        List<T> fields = new ArrayList<T>();
        fields.addAll(set);

        return fields;
    }

    /**
     * 过滤方法
     *
     * @param methods
     */
    private static Set<Method> filterMethods(Method[] methods) {
        Set<Method> set = new HashSet<Method>();
        for (Method method : methods) {
            final boolean isStatic = Modifier.isStatic(method.getModifiers());
            if (!isStatic) {
                set.add(method);
            }
        }

        return set;
    }

    /**
     * 过滤字段
     *
     * @param fields
     */
    private static Set<Field> filterField(Field[] fields) {
        Set<Field> set = new HashSet<Field>();
        for (Field field : fields) {
            final boolean isStatic = Modifier.isStatic(field.getModifiers());
            if (!isStatic) {
                set.add(field);
            }
        }

        return set;
    }
}



