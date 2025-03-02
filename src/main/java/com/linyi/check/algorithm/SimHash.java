package com.linyi.check.algorithm;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import com.hankcs.hanlp.seg.common.Term;
import com.hankcs.hanlp.tokenizer.StandardTokenizer;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Author: linyi
 * @Date: 2025/2/27
 * @ClassName: SimHash
 * @Version: 1.0
 * @Description: SimHash类实现了一个近似哈希算法，用于计算两个文本的相似度。
 */
public class SimHash {
    private final String tokens;
    private final BigInteger strSimHash;
    private final int hashbits;

    public SimHash(String tokens) {
        this(tokens, 64);
    }

    /**
     * 构造函数，用于初始化SimHash对象。
     * @param tokens 文本字符串
     * @param hashbits 哈希值的位数，必须为正的整数
     */
    public SimHash(String tokens, int hashbits) {
        if (tokens == null) {
            throw new IllegalArgumentException("tokens不能为空");
        }
        if (hashbits <= 0) {
            throw new IllegalArgumentException("Hashbits必须是正的");
        }
        this.tokens = tokens;
        this.hashbits = hashbits;
        this.strSimHash = simHash();
    }

    /**
     * 清除HTML标签并格式化字符串
     *
     * @param content 需要清理和格式化的原始内容
     * @return 清理和格式化后的字符串
     */
    public static String cleanResume(String content) {
        // 清除所有HTML标签
        content = Jsoup.clean(content, Whitelist.none());
        // 将所有字符转换为小写
        content = StringUtils.lowerCase(content);
        // 使用正则表达式移除所有空白字符和不可断空格
        content = content.replaceAll("[\\s\\u00A0]+", "");
        return content;
    }

    /**
     * 计算并返回简历文本的SimHash值
     * SimHash是一种近似哈希算法，用于高效地识别文档的相似性
     *
     * @return 简历文本的SimHash值，作为一个BigInteger返回
     */
    private BigInteger simHash() {
        // 清洗简历文本 tokens，移除不必要的信息
        String processedTokens = cleanResume(tokens);
        // 初始化一个长度为hashbits的整型数组v，用于累加每个位的权重
        int[] v = new int[hashbits];

        // 对处理后的文本进行分词，得到词项列表
        List<Term> termList = StandardTokenizer.segment(processedTokens);

        // 初始化一个映射，用于设置词性的权重
        Map<String, Integer> weightOfNature = new HashMap<>();
        weightOfNature.put("n", 2); // 例如，名词的权重为2
        // 初始化一个集合，用于存储需要忽略的词性
        Set<String> stopNatures = new HashSet<>();
        stopNatures.add("w"); // 例如，忽略标点符号
        // 设置词频阈值，超过该阈值的词将被忽略
        int overCount = 5;
        // 初始化一个映射，用于记录每个词的出现次数
        Map<String, Integer> wordCount = new HashMap<>();

        // 遍历词项列表，计算每个词的哈希值和权重，并更新数组v
        for (Term term : termList) {
            String word = term.word;
            String nature = term.nature.toString();

            int currentCount = wordCount.getOrDefault(word, 0);
            if (currentCount > overCount) {
                continue;
            }
            wordCount.put(word, currentCount + 1);

            if (stopNatures.contains(nature)) {
                continue;
            }

            BigInteger t = hash(word);
            int weight = weightOfNature.getOrDefault(nature, 1);

            for (int i = 0; i < hashbits; i++) {
                BigInteger bitmask = BigInteger.ONE.shiftLeft(i);
                if (t.and(bitmask).signum() != 0) {
                    v[i] += weight;
                } else {
                    v[i] -= weight;
                }
            }
        }

        // 根据数组v的值生成最终的指纹fingerprint
        BigInteger fingerprint = BigInteger.ZERO;
        for (int i = 0; i < hashbits; i++) {
            if (v[i] >= 0) {
                fingerprint = fingerprint.add(BigInteger.ONE.shiftLeft(i));
            }
        }
        return fingerprint;
    }

    /**
     * 计算给定字符串的哈希值
     * 该方法实现了一个自定义的哈希函数，用于将字符串映射到一个大整数
     * 特别处理了短字符串的情况，并使用了位操作来确保哈希值的均匀分布
     *
     * @param source 输入字符串
     * @return 计算得到的哈希值，以BigInteger形式返回
     */
    private BigInteger hash(String source) {
        // 空字符串的特殊处理：返回0
        if (source.isEmpty()) {
            return BigInteger.ZERO;
        }
        // 短词处理：长度不足时重复首字符
        while (source.length() < 3) {
            source += source.charAt(0);
        }
        // 将字符串转换为字符数组，便于逐个字符处理
        char[] chars = source.toCharArray();
        // 初始化哈希值计算的起始值，使用了移位操作以增加差异性
        BigInteger x = BigInteger.valueOf(chars[0]).shiftLeft(7);
        // 哈希计算中的乘数，用于增加哈希值的随机性
        BigInteger m = new BigInteger("1000003");
        // 创建一个掩码，确保哈希值的位数符合预期
        BigInteger mask = BigInteger.ONE.shiftLeft(hashbits).subtract(BigInteger.ONE);
        // 遍历每个字符，更新哈希值
        for (char c : chars) {
            BigInteger temp = BigInteger.valueOf(c);
            // 使用乘法、异或和与操作来计算哈希值，确保良好的扩散性
            x = x.multiply(m).xor(temp).and(mask);
        }
        // 将字符串长度与哈希值进行异或，以考虑字符串长度对哈希值的影响
        x = x.xor(BigInteger.valueOf(source.length()));
        // 特殊值处理：如果哈希值为-1，则返回-2，以避免特定值的冲突
        return x.equals(BigInteger.ONE.negate()) ? BigInteger.valueOf(-2) : x;
    }

    /**
     * 计算两个SimHash对象之间的汉明距离
     * 汉明距离是指两个字符串对应位不同的数量
     *
     * @param other 另一个SimHash对象，用于比较
     * @return 返回两个SimHash对象之间的汉明距离
     * @throws IllegalArgumentException 如果两个SimHash对象的hashbits长度不一致，则抛出异常
     */
    public int hammingDistance(SimHash other) {
        // 检查两个SimHash对象的hashbits长度是否一致
        if (this.hashbits != other.hashbits) {
            throw new IllegalArgumentException("hashbits length mismatch");
        }
        // 创建一个掩码，用于确保后续操作只考虑hashbits指定的位数
        BigInteger mask = BigInteger.ONE.shiftLeft(hashbits).subtract(BigInteger.ONE);
        // 执行异或操作，并应用掩码，然后计算结果中1的个数，即为汉明距离
        BigInteger xor = strSimHash.xor(other.strSimHash).and(mask);
        return xor.bitCount();
    }

    /**
     * 计算当前SimHash对象与另一个SimHash对象的相似度
     * 相似度的计算基于海明距离，即两个二进制串在多少位上不同
     * 相似度的值域为[0,1]，值越接近1表示两个对象越相似
     *
     * @param other 另一个SimHash对象，用于比较相似度
     * @return 返回两个SimHash对象的相似度，值域为[0,1]
     */
    public double getSemblance(SimHash other) {
        // 计算当前SimHash对象与另一个SimHash对象的海明距离
        int hamming = hammingDistance(other);
        // 通过海明距离计算相似度，相似度 = 1 - 海明距离/哈希位数
        return 1.0 - (double) hamming / hashbits;
    }

    public static void main(String[] args) {

        String s1 = "借鉴hashmap算法找出可以hash的key值，因为我们使用的simhash是局部敏感哈希，这个算法的特点是只要相似的字符串只有个别的位数是有差别变化。那这样我们可以推断两个相似的文本，至少有16位的simhash是一样的。具体选择16位、8位、4位，大家根据自己的数据测试选择，虽然比较的位数越小越精准，但是空间会变大。分为4个16位段的存储空间是单独simhash存储空间的4倍。之前算出5000w数据是 382 Mb，扩大4倍1.5G左右，还可以接受：）  通过这样计算，我们的simhash查找过程全部降到了1毫秒以下。就加了一个hash效果这么厉害？我们可以算一下，原来是5000w次顺序比较，现在是少了2的16次方比较，前面16位变成了hash查找。后面的顺序比较的个数是多少？ 2^16 = 65536， 5000w/65536 = 763 次。。。。实际最后链表比较的数据也才 763次！所以效率大大提高！  到目前第一点降到3.6毫秒、支持5000w数据相似度比较做完了。还有第二点同一时刻发出的文本如果重复也只能保留一条和短文本相识度比较怎么解决。其实上面的问题解决了，这两个就不是什么问题了。  之前的评估一直都是按照线性计算来估计的，就算有多线程提交相似度计算比较，我们提供相似度计算服务器也需要线性计算。比如同时客户端发送过来两条需要比较相似度的请求，在服务器这边都进行了一个排队处理，一个接着一个，第一个处理完了在处理第二个，等到第一个处理完了也就加入了simhash库。所以只要服务端加了队列，就不存在同时请求不能判断的情况。 simhash如何处理短文本？换一种思路，simhash可以作为局部敏感哈希第一次计算缩小整个比较的范围，等到我们只有比较700多次比较时，就算使用我们之前精准度高计算很慢的编辑距离也可以搞定。当然如果觉得慢了，也可以使用余弦夹角等效率稍微高点的相似度算法";
        String s2 = "计算当前SimHash对象与另一个SimHash对象的相似度，相似度的计算基于海明距离，即两个二进制串在多少位上不同，相似度的值域为[0,1]，值越接近1表示两个对象越相似";
        String s3 = "Must be at least 18. Apple and its trade-in partners reserve the right to refuse or limit any Trade ind transaction for any reason. ";
        long l3 = System.currentTimeMillis();
        SimHash hash3 = new SimHash(s1, 64);
        SimHash hash4 = new SimHash(s3, 64);
        System.out.println("======================================");
        System.out.println("海明距离："+hash4.hammingDistance(hash3));
        System.out.println("相似度："+hash3.getSemblance(hash4));
        System.out.println("====================耗时ms==================");
        long l4 = System.currentTimeMillis();
        System.out.println(l4 - l3);
    }
}