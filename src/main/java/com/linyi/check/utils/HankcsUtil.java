package com.linyi.check.utils;

import com.hankcs.hanlp.seg.common.Term;
import com.hankcs.hanlp.tokenizer.StandardTokenizer;
import com.linyi.check.algorithm.SimHash;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author: linyi
 * @Date: 2025/2/27
 * @ClassName: HankcsUtil
 * @Version: 1.0
 * @Description: 分词工具类
 */
public class HankcsUtil {

    /**
     * 使用标准分词器对文本进行分词
     * 该方法首先对简历文本进行清理，然后使用StandardTokenizer进行分词处理
     * 主要解决了如何对文本进行标准化分词的问题
     *
     * @param text 需要进行分词处理的文本
     * @return 分词后的词语列表
     */
    public static List<String> standardTokenizer(String text){
        // 对输入的文本进行清理，如去除不必要的字符等，以提高分词的准确性
        String cleanedText = SimHash.cleanResume(text);
        // 使用StandardTokenizer对清理后的文本进行分词处理
        List<Term> segmentedTerms = StandardTokenizer.segment(cleanedText);
        // 将分词处理后的Term对象列表转换为词语字符串列表
        return segmentedTerms.stream().map(term -> term.word).collect(Collectors.toList());
    }
}
