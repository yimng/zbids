package com.caicui.commons.common.utils;


import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;


public class IdUtils {

    /**
     * 判断是否包含
     *
     * @param ids
     * @param id
     * @return
     */
    public static boolean contain(String ids, String id) {
        final Set<String> split = split(ids);

        return split.contains(id);
    }

    /**
     * 判断集合main是否包含sub
     *
     * @param main
     * @param sub
     * @return
     */
    public static boolean contains(Collection<String> main, Collection<String> sub) {
        final boolean isSubCollection = CollectionUtils.isSubCollection(sub, main);

        return isSubCollection;
    }

    /**
     * 判断集合main是否包含sub
     *
     * @param main
     * @param sub
     * @return
     */
    public static boolean contains(String main, String sub) {
        final Set<String> mainSet = split(main);
        final Set<String> subSet = split(sub);

        return contains(mainSet, subSet);
    }

    /**
     * 求数量
     *
     * @param ids
     * @return
     */
    public static Integer count(String ids) {
        return split(ids).size();
    }

    /**
     * IN查询连接字符串
     *
     * @param idList
     * @return
     */
    public static String inQueryJoin(Collection<String> idList) {
        if (CollectionUtils.isNotEmpty(idList)) {
            List<String> inQueryList = new ArrayList<>();
            for (String id : idList) {
                inQueryList.add("'" + id + "'");
            }
            return StringUtils.join(inQueryList.toArray(), ",");
        }
        return null;
    }

    /**
     * IN查询连接字符串
     *
     * @param ids
     * @return
     */
    public static String inQueryJoin(String[] ids) {
        if (ids != null) {
            final List<String> idList = Arrays.asList(ids);
            return inQueryJoin(idList);
        }
        return null;
    }

    /**
     * 是否全部为空
     *
     * @param ids
     * @return
     */
    public static boolean isEmpty(String... ids) {
        for (String id : ids) {
            final boolean empty = StringUtils.isEmpty(StringUtils.trimToNull(id));

            if (!empty) {
                return false;
            }
        }

        return true;
    }

    /**
     * 是否全部不为空
     *
     * @param ids
     * @return
     */
    public static boolean isNotEmpty(String... ids) {
        for (String id : ids) {
            final boolean notEmpty = StringUtils.isNotEmpty(StringUtils.trimToNull(id));

            if (!notEmpty) {
                return false;
            }
        }

        return true;
    }

    /**
     * 连接字符串
     *
     * @param ids
     * @return
     */
    public static String join(String[] ids) {
        return StringUtils.join(ids, ",");
    }


    /**
     * 连接字符串
     *
     * @param idList
     * @return
     */
    public static String join(Collection<String> idList) {
        return StringUtils.join(idList.toArray(), ",");
    }

    /**
     * 拼接字符串类型的ID
     *
     * @param ids
     * @return
     */
    public static String merge(String ids) {
        Set<String> resultSet = split(ids);

        return StringUtils.trimToNull(StringUtils.join(resultSet.toArray(), ","));
    }

    /**
     * 在map的value部分合并字符串
     *
     * @param map
     * @param key
     * @param newValue
     * @return
     */
    public static Map<String, String> mergeValues(Map<String, String> map, String key, String newValue) {
        StringBuilder valueBuilder = new StringBuilder();
        if ((map != null) && !map.isEmpty() && map.containsKey(key)) {
            String value = map.get(key);
            if (StringUtils.isNotEmpty(StringUtils.trimToNull(value))) {
                valueBuilder.append(value).append(",");
            }
        }

        if (StringUtils.isNotEmpty(newValue)) {
            valueBuilder.append(newValue);
        }

        map.put(key, StringUtils.trimToNull(valueBuilder.toString()));
        return map;
    }

    /**
     * 分割字符串
     *
     * @param ids
     * @return
     */
    public static Set<String> split(String ids) {
        Set<String> resultSet = new LinkedHashSet<String>();
        if (StringUtils.isNotEmpty(ids)) {
            final String[] splits = StringUtils.split(ids, ",");
            for (String split : splits) {
                if (StringUtils.isNotEmpty(StringUtils.trimToNull(split))) {
                    resultSet.add(split);
                }
            }
        }
        return resultSet;
    }
}


