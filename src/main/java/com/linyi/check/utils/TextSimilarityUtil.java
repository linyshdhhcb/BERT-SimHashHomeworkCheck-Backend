package com.linyi.check.utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.linyi.check.algorithm.SimHash.cleanResume;

/**
 * @Author: linyi
 * @Date: 2025/2/28
 * @ClassName: TextSimilarityUtil
 * @Version: 1.0
 * @Description:
 */
public class TextSimilarityUtil {
    public static double computeTextSimilarity(String text1, String text2) throws Exception {
        // 获取 Python 脚本的绝对路径
        String pythonScriptPath = Paths.get(System.getProperty("user.dir"), "src", "main", "java", "com", "linyi", "check", "algorithm", "py", "bert.py").toString();
        // 创建 ProcessBuilder 并传递文本参数
        ProcessBuilder processBuilder = new ProcessBuilder(
                "python",
//                "C:\\Intellij_IDEA\\DEV\\BERT-SimHashHomeworkCheck\\src\\main\\java\\com\\linyi\\check\\algorithm\\py\\bert.py",
                pythonScriptPath,
                text1,
                text2);

        // 添加调试信息
        System.out.println("Starting Python script with arguments: " + text1 + ", " + text2);

        Process process = processBuilder.start();

        // 获取输出流
        InputStream inputStream = process.getInputStream();
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        BufferedReader reader = new BufferedReader(inputStreamReader);

        // 获取错误流
        InputStream errorStream = process.getErrorStream();
        InputStreamReader errorStreamReader = new InputStreamReader(errorStream, StandardCharsets.UTF_8);
        BufferedReader errorReader = new BufferedReader(errorStreamReader);

        // 获取退出码
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            StringBuilder errorOutput = new StringBuilder();
            String errorLine;
            while ((errorLine = errorReader.readLine()) != null) {
                errorOutput.append(errorLine).append("\n");
            }
            throw new RuntimeException("Python脚本失败，提示退出代码" + exitCode + ". Error: " + errorOutput.toString());
        }

        // 读取 Python 脚本的输出
        String line = reader.readLine();
        if (line == null || line.isEmpty()) {
            throw new RuntimeException("Python脚本没有产生任何输出。");
        }

        // 添加调试信息
        System.out.println("Python script output: " + line);

        // 提取文本相似度的值
        Pattern pattern = Pattern.compile("文本相似度:\\s*(\\d+(\\.\\d+)?)");
        Matcher matcher = pattern.matcher(line);
        if (matcher.find()) {
            return Double.parseDouble(matcher.group(1));
        } else {
            throw new RuntimeException("Python脚本的输出格式无效：" + line);
        }
    }

    public static double sim(String text1, String text2){
        try {
            return computeTextSimilarity(cleanResume(text1), cleanResume(text2));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public static void main(String[] args) {
        long l = System.currentTimeMillis();
        String text1 = "这是第一个文本";
        String text2 = "这是第二个文本";
        try {
            TextSimilarityUtil service = new TextSimilarityUtil();
            double similarity = service.computeTextSimilarity(cleanResume(text1), cleanResume(text2));
            System.out.println("耗时：" + (System.currentTimeMillis() - l));
            System.out.println("计算结果为：" + similarity);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
