package com.caicui.commons.utils;

import org.apache.commons.lang.StringUtils;

import java.io.*;
import java.util.Properties;

/**
 * Description: All Rights Reserved.
 *
 * @version 1.0 2013-1-24 上午9:36:04 by 于科为 kw.yu@zuche.com
 */
public class PropertiesUtil {

    /**
     * getProperties : 取得路径下的属性信息
     *
     * @param path
     * @return Properties
     * @throws
     */
    public static Properties getProperties(String path) {
        File file = loadFile(path);
        Properties properties = new Properties();
        String baseMessage = "读取配置文件错误。";
        try {
            properties.load(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            String message = baseMessage + "文件:" + path + "不存在。";
            throw new RuntimeException(message + "异常信息如下:" + e.getMessage());
        } catch (IOException e) {
            String message = baseMessage;
            throw new RuntimeException(message + "异常信息如下:" + e.getMessage());
        } catch (Exception e) {
            String message = baseMessage;
            throw new RuntimeException(message + "异常信息如下:" + e.getMessage());
        }
        return properties;
    }

    /**
     * writeProperties : 写入路径下的属性信息
     *
     * @param properties
     * @param filePath
     * @param comments   void
     * @throws
     */
    public static void writeProperties(Properties properties, String filePath, String comments) {
        File propertiesFile = new File(filePath);
        File directoryPath = propertiesFile.getParentFile();

        if (!directoryPath.exists()) {
            directoryPath.mkdirs();
        }

        String baseMessage = "写入配置文件" + filePath + "错误。";
        OutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(propertiesFile);
            properties.store(outputStream, comments);
        } catch (IOException e) {
            throw new RuntimeException(baseMessage + "异常信息如下:" + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException(baseMessage + "异常信息如下:" + e.getMessage());
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    throw new RuntimeException("关闭文件流错误,异常信息如下:" + e.getMessage());
                } catch (Exception e) {
                    throw new RuntimeException("关闭文件流错误,异常信息如下:" + e.getMessage());
                } finally {
                    outputStream = null;
                }
            }
        }
    }

    /**
     * loadFile :读入文件
     *
     * @param filePath
     * @return File
     * @throws
     */
    private static File loadFile(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new RuntimeException("文件: " + filePath + "不存在....");
        } else if (!filePath.endsWith(".properties")) {
            throw new RuntimeException("文件: " + filePath + "非.properties文件。无法正确解析");
        } else {
            return file;
        }
    }

    public static void main(String[] args) {
        System.out.println(getProperties("config/config.properties"));
    }
}
