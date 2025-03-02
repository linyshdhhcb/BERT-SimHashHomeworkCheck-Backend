package com.linyi.check.utils;

import com.linyi.check.algorithm.PHash;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.model.PicturesTable;
import org.apache.poi.ooxml.extractor.POIXMLTextExtractor;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFPictureData;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import java.nio.file.*;
import org.apache.poi.hwpf.usermodel.Picture;

/**
 * @Author: linyi
 * @Date: 2025/2/27
 * @ClassName: ImgExtractionUtil
 * @Version: 1.0
 * @Description: doc和docx图片提取工具类
 */
@Slf4j
public class ImgExtractionUtil {
    
    /**
     * 根据文档文件实体获取文档中图片的哈希列表
     * 此方法支持.doc和.docx文件格式，提取文件中的图片，并计算每张图片的哈希值
     *
     * @param files 文件实体，包含文件的绝对路径信息
     * @return 包含文档中所有图片哈希值的列表
     */
    public static List<String> getWordPicture(com.linyi.check.entity.Files files) {
        List<String> pictureHashList = new ArrayList<>();
        File file = new File(files.getAbsolutepath());

        // 检查文件是否存在、是否为目录、是否为空
        if (!file.exists() || file.isDirectory() || file.length() == 0) {
            log.warn("文件不存在或为空: " + file.getAbsolutePath());
            return pictureHashList;
        }

        try (InputStream is = new FileInputStream(file)) {
            String filePath = files.getAbsolutepath().toLowerCase();
            // 根据文件后缀名处理不同的文档格式
            if (filePath.endsWith(".doc")) {
                try (HWPFDocument doc = new HWPFDocument(is)) {
                    // 文档图片
                    PicturesTable picturesTable = doc.getPicturesTable();
                    List<org.apache.poi.hwpf.usermodel.Picture> pictures = picturesTable.getAllPictures();
                    // 获取每张图片哈希指纹
                    for (Picture picture : pictures) {
                        try (InputStream pictureFile = new ByteArrayInputStream(picture.getContent())) {
                            pictureHashList.add(PHash.getFeatureValue(pictureFile));
                        }
                    }
                }
            } else if (filePath.endsWith(".docx")) {
                try (XWPFDocument docx = new XWPFDocument(is);
                     POIXMLTextExtractor extractor = new XWPFWordExtractor(docx)) {
                    // 文档图片
                    List<XWPFPictureData> pictures = docx.getAllPictures();
                    for (XWPFPictureData picture : pictures) {
                        byte[] bytev = picture.getData();
                        try (InputStream pictureFile = new ByteArrayInputStream(bytev)) {
                            // 获取图片哈希指纹
                            pictureHashList.add(PHash.getFeatureValue(pictureFile));
                        }
                    }
                }
            } else {
                log.warn("此文件不是word文件！");
            }
        } catch (Exception e) {
            log.info( "打开文件失败: 请检查文件是否有特殊格式", e);
            throw new RuntimeException("文件处理异常", e);
        }
        return pictureHashList;
    }

    /**
     * 删除指定文件夹下所有文件
     *
     * @param path 文件夹完整绝对路径
     */
    public static void delAllFile(String path) {
        if (path == null || path.trim().isEmpty()) {
            throw new IllegalArgumentException("路径不能为空");
        }

        Path dirPath = Paths.get(path);
        File dir = dirPath.toFile();

        if (!dir.exists() || !dir.isDirectory()) {
            throw new IllegalArgumentException("指定路径不存在或不是一个文件夹");
        }

        try {
            Files.walk(dirPath)
                    .sorted((a, b) -> -a.compareTo(b)) // 先删除子文件夹和文件
                    .forEach(p -> {
                        try {
                            Files.delete(p);
                        } catch (IOException e) {
                            System.err.println("删除文件 " + p + " 失败: " + e.getMessage());
                        }
                    });
        } catch (IOException e) {
            throw new RuntimeException("遍历文件夹失败: " + e.getMessage(), e);
        }
    }

    public static void main(String[] args) {
        
        String path = "C:\\a.docx";

        com.linyi.check.entity.Files files = com.linyi.check.entity.Files.builder()
                .filename("文档")
                .absolutepath(path).build();
        getWordPicture(files);

    }
    
}
