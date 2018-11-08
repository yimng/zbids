package com.caicui.commons.common.utils;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.*;

public class BeanCopyUtil {
    private static final String METHOD_TYPE_GET = "get";
    private static final String METHOD_TYPE_SET = "set";

    /**
     * Description: 松散拷贝
     *
     * @param srcObj
     * @param srcClazz
     * @param destClazz
     * @return
     * @throws Exception
     * @Version1.0 2014年5月29日 下午5:09:13 by 于科为 创建
     */
    public static <S, O> O looseCopy(S srcObj, Class<S> srcClazz, Class<O> destClazz) throws Exception {
        O destObj = (O) destClazz.newInstance();
        return looseCopy(srcObj, srcClazz, (O) destObj, destClazz);
    }

    /**
     * Description: 松散拷贝
     *
     * @param srcObj
     * @param srcClazz
     * @param destObj
     * @param destClazz
     * @return
     * @throws Exception
     * @Version1.0 2014年5月29日 下午5:09:34 by 于科为 创建
     */
    public static <S, O> O looseCopy(S srcObj, Class<S> srcClazz, O destObj, Class<O> destClazz) throws Exception {
        Map<String, String> map = getSrcPropertyStringValue(srcObj, srcClazz);
        List<Method> destDeclaredMethods = ReflectionUtil.getDeclaredMethods(destClazz);
        for (Method method : destDeclaredMethods) {
            String methodName = method.getName();
            if (methodName.startsWith(METHOD_TYPE_SET)) {
                String propName = exactPropertyName(methodName);
                if (map.containsKey(propName)) {
                    method.invoke(destObj, map.get(propName));
                }
            }
        }

        return destObj;
    }

    /**
     * Description: 获取类属性和属性的字符串值的Map
     *
     * @param object
     * @param clazz
     * @return
     * @throws Exception
     * @Version1.0 2014年5月29日 下午5:05:44 by 于科为 创建
     */
    public static Map<String, String> getSrcPropertyStringValue(Object object, Class<? extends Object> clazz) throws Exception {
        List<Method> declaredMethods = ReflectionUtil.getDeclaredMethods(clazz);
        Map<String, String> map = new HashMap<String, String>();
        for (Method method : declaredMethods) {
            String methodName = method.getName();
            if (methodName.startsWith(METHOD_TYPE_GET)) {
                String propName = exactPropertyName(methodName);
                Class<?> returnType = method.getReturnType();
                String strVal = "";
                if (returnType.equals(String.class)) {
                    String obj = (String) method.invoke(object);
                    if (obj != null) {
                        strVal = obj;
                    }
                } else if (returnType.isAssignableFrom(Date.class)) {
                    Date date = (Date) method.invoke(object);
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    if (date != null) {
                        strVal = dateFormat.format(date);
                    }
                } else {
                    Object obj = method.invoke(object);
                    if (obj != null) {
                        strVal = obj.toString();
                    }
                }
                if (StringUtils.isNotEmpty(strVal)) {
                    map.put(propName, strVal);
                }
            }
        }

        return map;
    }

    /**
     * 基础拷贝
     *
     * @param srcObj
     * @param srcClazz
     * @param destClazz
     * @param <S>
     * @param <O>
     * @return
     * @throws Exception
     */
    public static <S, O> O primaryCopy(S srcObj, Class<S> srcClazz, Class<O> destClazz) throws Exception {
        O destObj = (O) destClazz.newInstance();

        return primaryCopy(srcObj, srcClazz, (O) destObj, destClazz);
    }

    /**
     * 基础拷贝
     *
     * @param srcObj
     * @param srcClazz
     * @param destObj
     * @param destClazz
     * @param <S>
     * @param <O>
     * @return
     * @throws Exception
     */
    public static <S, O> O primaryCopy(S srcObj, Class<S> srcClazz, O destObj, Class<O> destClazz) throws Exception {
        Map<String, Method> srcGetMap = getAccessAbleMethodMap(srcClazz, METHOD_TYPE_GET);
        Map<String, Method> disSetMap = getAccessAbleMethodMap(destClazz, METHOD_TYPE_SET);
        Set<String> srcKeySet = srcGetMap.keySet();
        Set<String> disKeySet = disSetMap.keySet();
        Collection<String> accessAbleKeyCollection = CollectionUtils.intersection(srcKeySet, disKeySet);
        for (String key : accessAbleKeyCollection) {
            Method getMethod = srcGetMap.get(key);
            Method setMethod = disSetMap.get(key);
            if (methodArgsMatch(getMethod, setMethod)) {
                Object value = getMethod.invoke(srcObj);
                if (isWrapClass(getMethod.getReturnType()) || isPrimitive(getMethod.getReturnType()) || getMethod.getReturnType().equals(String.class)) {
                    setMethod.invoke(destObj, value);
                }
            }
        }

        return destObj;
    }

    /**
     * 判断是否基础类型
     *
     * @param clazz
     * @return
     */
    public static Boolean isPrimitive(Class clazz) {
        try {
            return clazz.isPrimitive();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 判断是否封装类型
     *
     * @param clazz
     * @return
     */
    public static Boolean isWrapClass(Class clazz) {
        try {
            return ((Class) clazz.getField("TYPE").get(null)).isPrimitive();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 严格拷贝<br>
     * 只有目标对象(dest)属性名称和类型和源对象(src)的名称和类型全一致 ，<br>
     * 且必须目标对象(dest)源对象有get/set方法，且方法是public 的情况才发生拷贝<br>
     *
     * @param srcObj
     * @param srcClazz
     * @param destClazz
     * @throws Exception
     */
    public static <S, O> O strictCopy(S srcObj, Class<S> srcClazz, Class<O> destClazz) throws Exception {
        O distObj = destClazz.newInstance();
        distObj = strictCopy(srcObj, srcClazz, distObj, destClazz);
        return distObj;
    }

    /**
     * 严格拷贝<br>
     * 只有目标对象(dist)属性名称和类型和源对象(src)的名称和类型全一致 ，<br>
     * 且必须目标对象(dist)源对象有get/set方法，且方法是public 的情况才发生拷贝<br>
     *
     * @param srcObj
     * @param srcClazz
     * @param destObj
     * @param destClazz
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public static <S, O> O strictCopy(S srcObj, Class<S> srcClazz, O destObj, Class<O> destClazz) throws Exception {
        Map<String, Method> srcGetMap = getAccessAbleMethodMap(srcClazz, METHOD_TYPE_GET);
        Map<String, Method> disSetMap = getAccessAbleMethodMap(destClazz, METHOD_TYPE_SET);
        Set<String> srcKeySet = srcGetMap.keySet();
        Set<String> disKeySet = disSetMap.keySet();
        Collection<String> accessAbleKeyCollection = CollectionUtils.intersection(srcKeySet, disKeySet);
        for (String key : accessAbleKeyCollection) {
            Method getMethod = srcGetMap.get(key);
            Method setMethod = disSetMap.get(key);
            if (methodArgsMatch(getMethod, setMethod)) {
                Object value = getMethod.invoke(srcObj);
                setMethod.invoke(destObj, value);
            }
        }

        return destObj;
    }

    /**
     * Description: 获取可访问的方法Map
     *
     * @param clazz
     * @param type
     * @return
     * @throws Exception
     * @Version1.0 2014年5月29日 下午5:11:00 by 于科为 创建
     */
    private static Map<String, Method> getAccessAbleMethodMap(Class<? extends Object> clazz, String type) throws Exception {
        Map<String, Field> suspectedAccessAbleFieldMap = getSuspectedAccessAbleFieldMap(clazz);
        List<Method> declaredMethods = ReflectionUtil.getDeclaredMethods(clazz);
        Map<String, Method> resultMap = new HashMap<String, Method>();
        if (CollectionUtils.isNotEmpty(suspectedAccessAbleFieldMap.keySet())) {
            for (String propName : suspectedAccessAbleFieldMap.keySet()) {
                Field field = suspectedAccessAbleFieldMap.get(propName);
                Class<?> filedType = field.getType();
                String getMethodName = METHOD_TYPE_GET + propName.substring(0, 1).toUpperCase() + propName.substring(1);
                String setMethodName = METHOD_TYPE_SET + propName.substring(0, 1).toUpperCase() + propName.substring(1);
                for (Method method : declaredMethods) {
                    String methodName = method.getName();
                    Class<?> returnType = method.getReturnType();
                    Class<?>[] parameterTypes = method.getParameterTypes();
                    int length = method.getParameterTypes().length;
                    if (StringUtils.equals(methodName, setMethodName) && StringUtils.equalsIgnoreCase(type, METHOD_TYPE_SET) && (filedType != null)
                            && (length == 1) && parameterTypes[0].equals(filedType)) {
                        resultMap.put(propName, method);
                    }
                    if (StringUtils.equals(methodName, getMethodName) && StringUtils.equalsIgnoreCase(type, METHOD_TYPE_GET) && (filedType != null)
                            && (length == 0) && returnType.equals(filedType)) {
                        resultMap.put(propName, method);
                    }
                }
            }
        }

        return resultMap;
    }

    /**
     * Description: 判断方法参数是否匹配
     *
     * @param getMethod
     * @param setMethod
     * @return
     * @Version1.0 2014年5月29日 下午5:03:44 by 于科为 创建
     */
    private static boolean methodArgsMatch(Method getMethod, Method setMethod) {
        Class<?> returnType = getMethod.getReturnType();
        Class<?>[] parameterTypes = setMethod.getParameterTypes();
        if (parameterTypes.length != 1) {
            return false;
        }
        Class<?> paramType = parameterTypes[0];
        if (!returnType.isAssignableFrom(paramType)) {
            return false;
        }
        return true;
    }

    private static Map<String, Field> getSuspectedAccessAbleFieldMap(Class<? extends Object> clazz) throws Exception {
        Collection<String> accessAbleNames = getSuspectedAccessAbleFieldNames(clazz);
        Map<String, Field> reslutMap = new HashMap<String, Field>();
        for (String name : accessAbleNames) {
            List<Field> declaredFields = ReflectionUtil.getDeclaredFields(clazz);
            for (Field declaredField : declaredFields) {
                if (declaredField.getName().equals(name)) {
                    reslutMap.put(name, declaredField);
                }
            }
        }
        return reslutMap;
    }

    @SuppressWarnings("unchecked")
    private static Collection<String> getSuspectedAccessAbleFieldNames(Class<? extends Object> clazz) {

        // Method[] declaredMethods = clazz.getDeclaredMethods();
        // Field[] declaredFields = clazz.getDeclaredFields();
        List<Method> declaredMethods = ReflectionUtil.getDeclaredMethods(clazz);
        List<Field> declaredFields = ReflectionUtil.getDeclaredFields(clazz);
        Set<String> fieldNameList = new HashSet<String>();
        Set<String> getterNameList = new HashSet<String>();
        Set<String> setterNameList = new HashSet<String>();
        for (Field field : declaredFields) {
            String name = field.getName();
            fieldNameList.add(name);
        }
        for (Method method : declaredMethods) {
            String methodName = method.getName();
            if (StringUtils.startsWith(methodName, METHOD_TYPE_SET)) {
                String propName = exactPropertyName(methodName);
                setterNameList.add(propName);
            }
            if (StringUtils.startsWith(methodName, METHOD_TYPE_GET)) {
                String propName = exactPropertyName(methodName);
                getterNameList.add(propName);
            }
        }
        Collection<String> accessAbleNames = null;
        if (CollectionUtils.isNotEmpty(fieldNameList) && CollectionUtils.isNotEmpty(setterNameList) && CollectionUtils.isNotEmpty(getterNameList)) {
            accessAbleNames = CollectionUtils.intersection(CollectionUtils.intersection(fieldNameList, setterNameList), getterNameList);
        }

        return accessAbleNames;
    }

    /**
     * Description: 抽取属性名称
     *
     * @param methodName
     * @return
     * @Version1.0 2014年5月29日 下午5:00:16 by 于科为
     */
    private static String exactPropertyName(String methodName) {
        return methodName.substring(3, 4).toLowerCase() + methodName.substring(4);
    }
}

