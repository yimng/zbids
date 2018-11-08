package com.caicui.commons.utils;

import org.apache.commons.io.FilenameUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.util.Date;

public class FileUtil {

    /**
     * 复制文件至文件目录
     *
     * @param file 文件
     * @return 文件路径
     */
    public static String copyFile(HttpServletRequest request, MultipartFile file) {
        if (file == null || file.getSize() <= 0) {
            return null;
        }

        String destFilePath = "upload/" + DateUtil.getFormatDate(new Date(), "yyyyMM") + "/" + CommonUtil.getUUID() + "." + FilenameUtils.getExtension(file.getOriginalFilename());
        File destFile = new File(request.getSession().getServletContext().getRealPath(destFilePath));
        File destParentFile = destFile.getParentFile();
        if (!destParentFile.isDirectory()) {
            destParentFile.mkdirs();
        }
        try {
            FileUtil.saveFileFromInputStream(file.getInputStream(), destFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return destFilePath;
    }

    /**
     * 复制会员文件至文件目录
     *
     * @param file 文件
     * @param id   会员id
     * @return 文件路径
     */
    public static String copyFile(HttpServletRequest request, MultipartFile file, String id) {
        if (file == null || file.getSize() <= 0) {
            return null;
        }
        String destFilePath = "upload/" + DateUtil.getFormatDate(new Date(), "yyyyMM") + "/" + CommonUtil.getUUID() + "." + FilenameUtils.getExtension(file.getOriginalFilename());
        File destFile = new File(request.getSession().getServletContext().getRealPath(destFilePath));
        File destParentFile = destFile.getParentFile();
        if (!destParentFile.isDirectory())
            destParentFile.mkdirs();
        try {
            FileUtil.saveFileFromInputStream(file.getInputStream(), destFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return destFilePath;
    }

    /**
     * 复制会员文件至文件目录
     *
     * @param file 文件
     * @param id   会员id
     * @return 文件路径
     */
    public static String copyResoumeFile(HttpServletRequest request,
                                         MultipartFile file) {
        if (file == null || file.getSize() <= 0) {
            return null;
        }
        String destFilePath = "upload/" + DateUtil.getFormatDate(new Date(), "yyyyMM") + "/" + CommonUtil.getUUID() + "." + FilenameUtils.getExtension(file.getOriginalFilename());
        File destFile = new File(request.getSession().getServletContext().getRealPath(destFilePath));
        File destParentFile = destFile.getParentFile();
        if (!destParentFile.isDirectory()) destParentFile.mkdirs();
        try {
            FileUtil.saveFileFromInputStream(file.getInputStream(), destFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return destFilePath;
    }

    /*
     * Get the extension of a file.
     */
    public static String getSuffix(File file) {
        String ext = null;
        String suffix = file.getName();
        int i = suffix.lastIndexOf('.');

        if (i > 0 && i < suffix.length() - 1) {
            ext = suffix.substring(i + 1).toLowerCase();
        }
        return ext;
    }

    /*
     * Get the extension of a file.
     */
    public static String getSuffix(String fileName) {
        String ext = null;
        int i = fileName.lastIndexOf('.');
        if (i > 0 && i < fileName.length() - 1) {
            ext = fileName.substring(i + 1).toLowerCase();
        }
        return ext;
    }

    public static String readFile(File file) {
        StringBuilder str = new StringBuilder();
        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
            String s;
            try {
                while ((s = in.readLine()) != null)
                    str.append(s + "<br />");
            } finally {
                in.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return str.toString();
    }

    /**
     * 把输入流保存为文件
     *
     * @param 输入流
     * @param 文件
     * @throws IO异常
     */
    public static void saveFileFromInputStream(InputStream inputStream, File destFile) throws IOException {
        FileOutputStream fs = new FileOutputStream(destFile);
        byte[] buffer = new byte[1024 * 1024];
        // int bytesum = 0;
        int byteread = 0;
        while ((byteread = inputStream.read(buffer)) != -1) {
            // bytesum += byteread;
            fs.write(buffer, 0, byteread);
            fs.flush();
        }
        fs.close();
        inputStream.close();
    }

}
