package com.linyi.check.algorithm;

import com.hankcs.hanlp.tokenizer.StandardTokenizer;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author: linyi
 * @Date: 2025/2/27
 * @ClassName: Jaccard
 * @Version: 1.0
 * @Description: 杰卡德相似度算法计算两个字符相似度，余弦的补充
 */
public class Jaccard {

    public static void main(String[] args) {
        String s1 = "我我我我我我我我我我我我我我我喜欢文学，喜欢地理，也喜欢化学";
        String s2 = "我不喜欢文学，喜欢化学";

        List<String> list1 = StandardTokenizer.segment(s1).stream().map(term -> term.word).collect(Collectors.toList());
        List<String> list2 = StandardTokenizer.segment(s2).stream().map(term -> term.word).collect(Collectors.toList());

        System.out.println(list1 + "\n与\n" + list2 + "\n的杰卡德相似度为：" + Jaccard.jaccardSimilarity(list1, list2));
        System.out.println();
    }

    /**
     * 求并集交集并返回杰卡德系数
     *
     * @param list1 分好词的List集合1
     * @param list2 分词分好的List集合2
     * @return 返回杰卡德相似度系数
     */
    public static double jaccardSimilarity(List<String> list1, List<String> list2) {
        Set<String> set1 = new HashSet<>(list1);
        Set<String> set2 = new HashSet<>(list2);

        // 计算交集
        set1.retainAll(set2);
        int intersectionSize = set1.size();

        // 重新加载set1
        set1.addAll(list1);
        set1.addAll(list2);
        int unionSize = set1.size();

        if (unionSize == 0) {
            return 0;
        }

        // 杰卡德相似度= 交集/并集
        return (double) intersectionSize / unionSize;
    }
}
