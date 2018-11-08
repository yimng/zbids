package com.caicui.commons.utils;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JsonUtil {
    private static Logger LOGGER = LoggerFactory.getLogger(JsonUtil.class);
    private static ObjectMapper objectMapper = new ObjectMapper();

    // 将对象转换为JSON字符串
    public static String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonGenerationException e) {
            LOGGER.error("转换为json字符串失败 : " + e.getMessage());
        } catch (JsonMappingException e) {
            LOGGER.error("转换为json字符串失败 : " + e.getMessage());
        } catch (IOException e) {
            LOGGER.error("转换为json字符串失败 : " + e.getMessage());
        } catch (Exception e) {
            LOGGER.error("转换为json字符串失败 : " + e.getMessage());
        }
        return null;
    }

    public static <T> List<T> toList(String json, Class<T> clazz) {
        try {
            if (StringUtils.isNotBlank(json)) {
                List<T> list = new ArrayList<T>();
                JsonFactory f = new JsonFactory();
                JsonParser jp = f.createJsonParser(json);
                jp.nextToken();
                while (jp.nextToken() == JsonToken.START_OBJECT) {
                    Object obj = objectMapper.readValue(jp, clazz);
                    list.add((T) obj);
                }
                return list;
            }
        } catch (JsonParseException e) {
            e.printStackTrace();
            LOGGER.error("json字符串转化为 list失败,原因 :" + e.getMessage());
        } catch (JsonMappingException e) {
            e.printStackTrace();
            LOGGER.error("json字符串转化为 list失败,原因 :" + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.error("json字符串转化为 list失败,原因 :" + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("json字符串转化为 list失败,原因 :" + e.getMessage());
        }
        return null;
    }

    // 将JSON字符串转换为对象
    public static <T> T toObject(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonParseException e) {
            LOGGER.error("json字符串转化为 javabean失败 : " + e.getMessage());
        } catch (JsonMappingException e) {
            LOGGER.error("json字符串转化为 javabean失败 : " + e.getMessage());
        } catch (IOException e) {
            LOGGER.error("json字符串转化为 javabean失败 : " + e.getMessage());
        } catch (Exception e) {
            LOGGER.error("json字符串转化为 javabean失败 : " + e.getMessage());
        }
        return null;
    }


}
