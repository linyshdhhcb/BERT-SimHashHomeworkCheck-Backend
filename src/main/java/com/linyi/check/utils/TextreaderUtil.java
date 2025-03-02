package com.linyi.check.utils;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

import java.io.*;
import java.util.List;

/**
 * @Author: linyi
 * @Date: 2025/2/27
 * @ClassName: TextreaderUtil
 * @Version: 1.0
 * @Description: 文本读取工具类
 */
public class TextreaderUtil {
    /**
     * 读取doc
     * @param filePath 文件路径
     * @return 文本内容
     */
    public static String DocReader(String filePath){
        try (FileInputStream fis = new FileInputStream(new File(filePath));
             XWPFDocument document = new XWPFDocument(fis)) {

            List<XWPFParagraph> paragraphs = document.getParagraphs();
            StringBuilder sb = new StringBuilder();
            for (XWPFParagraph para : paragraphs) {
                sb.append(para.getText());
            }
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    /**
     * 读取docx
     *
     * @param filePath 文件路径
     *  @return 文本内容
     */
    public static String DocxReader(String filePath){
        try (FileInputStream fis = new FileInputStream(new File(filePath));
             XWPFDocument document = new XWPFDocument(fis)) {

            List<XWPFParagraph> paragraphs = document.getParagraphs();
            StringBuilder sb = new StringBuilder();
            for (XWPFParagraph para : paragraphs) {
                sb.append(para.getText());
            }
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 读取TXT
     * @param filePath 文件路径
     * @return 文本内容
     */
    public static String TxtReader (String filePath){
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            StringBuilder sb = new StringBuilder();
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 读取PDF
     * @param filePath 文件路径
     * @return 文本内容
     */
    public static String PdfReader(String filePath){
        try (PDDocument document = PDDocument.load(new File(filePath))) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            System.out.println(text);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
