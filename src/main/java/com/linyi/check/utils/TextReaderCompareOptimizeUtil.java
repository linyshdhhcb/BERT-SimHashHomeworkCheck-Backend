package com.linyi.check.utils;

import cn.hutool.core.util.IdUtil;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.linyi.check.algorithm.CosineSimilarity;
import com.linyi.check.algorithm.Jaccard;
import com.linyi.check.algorithm.PHash;
import com.linyi.check.algorithm.SimHash;
import com.linyi.check.entity.Files;
import com.linyi.check.entity.vo.files.PlagiarizeVo;
import com.linyi.check.entity.SimilarityOutcome;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @Author: linyi
 * @Date: 2025/2/28
 * @ClassName: TextReaderCompareOptimizeUtil
 * @Version: 1.0
 * @Description: 文本读取比较优化工具类
 */
@Slf4j
public class TextReaderCompareOptimizeUtil {

    /**
     * 测试
     */
    public static void main(String[] args) throws Exception {
        //查重的路径
        String path = "C:\\Users\\a1830\\Desktop\\a";
        //获取开始时间
        long startTime = System.currentTimeMillis();
        String excelPath =
                path + "\\查重结果"
                        .concat(new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()))
                        .concat(".xlsx");

        getSimilarityMode(path, true, true, 0.5, excelPath, true,true);
        // 获取结束时间
        long endTime = System.currentTimeMillis();
        // 输出程序运行时间
        System.out.println("所有文档相似度计算完成，共耗时：" + (endTime - startTime) / 1000 + "s");
    }

    /*
     * 线程池核心参数说明（基于ThreadPoolExecutor）：
     *
     * [核心工作机制]
     * 1. 当任务数 < corePoolSize 时，立即创建新线程处理任务（即使存在空闲线程）
     * 2. 当 corePoolSize ≤ 任务数 ≤ queueCapacity + maximumPoolSize 时，将任务放入队列
     * 3. 当队列满且线程数 < maximumPoolSize 时，创建新线程处理任务
     * 4. 当队列满且线程数 = maximumPoolSize 时，触发拒绝策略
     *
     * @param corePoolSize   核心线程数（默认常驻线程，除非设置allowCoreThreadTimeOut）
     * @param maximumPoolSize 最大线程数（实际最大值：0x7fffffff = 2^31-1）
     * @param keepAliveTime  空闲线程存活时间（仅对非核心线程有效，除非开启核心线程超时）
     * @param unit           时间单位（TimeUnit枚举：NANOSECONDS, MILLISECONDS等）
     * @param workQueue      任务队列（选择策略：
     *                       - 快速任务：SynchronousQueue（无容量，直接移交）
     *                       - 无界队列：LinkedBlockingQueue（慎用，可能引起OOM）
     *                       - 有界队列：ArrayBlockingQueue（需合理设置容量））
     * @param threadFactory 线程工厂（建议自定义命名，示例：
     *                       new ThreadFactoryBuilder().setNameFormat("pool-%d").build()）
     * @param handler       拒绝策略（四种预定义策略）：
     *                       1. AbortPolicy（默认）：拒绝并抛出RejectedExecutionException
     *                       2. CallerRunsPolicy：由提交任务的线程直接执行
     *                       3. DiscardPolicy：静默丢弃新任务（不通知）
     *                       4. DiscardOldestPolicy：丢弃队列头（最旧）任务，重试提交
     *
     * [重要注意事项]
     * 1. 使用无界队列时maximumPoolSize参数将失效（永远不会触发创建非核心线程）
     * 2. 默认核心线程不会终止（可通过allowCoreThreadTimeOut(true)修改）
     * 3. 建议通过ThreadPoolExecutor构造函数创建，避免Executors工厂方法的内存风险
     */

    /**
     * 本机CPU核数
     **/
    final static int CORE_NUM = Runtime.getRuntime().availableProcessors();

    /**
     * 文件读取线程池，核心线程数=2CPU核数，最大线程数2Cpu核数
     * 创建一个固定大小的线程池，用于处理文件相关的任务
     **/
    final static ExecutorService fileThreadPool = new ThreadPoolExecutor(
            2 * CORE_NUM, // 核心线程数，设置为CPU核心数的两倍，以充分利用CPU资源
            2 * CORE_NUM, // 最大线程数，同样设置为CPU核心数的两倍，限制线程总数
            10L, // 空闲线程存活时间，单位为秒，允许空闲线程在终止前等待新任务
            TimeUnit.SECONDS, // 时间单位，秒
            new LinkedTransferQueue<>(), // 任务队列，使用LinkedTransferQueue作为等待任务的队列
            new ThreadFactoryBuilder().setNameFormat("doc-ini-pool-%d").build(), // 线程工厂，用于命名线程，便于调试和日志记录
            new ThreadPoolExecutor.CallerRunsPolicy() // 拒绝执行处理器，当任务数量超过最大线程数时，由调用线程自己来执行任务
    );
    /**
     * 单线程线程池
     * <p>
     * 该线程池配置了一个核心线程和一个最大线程数，都为1，意味着线程池中始终只有1个线程在执行任务。
     * 线程池的线程保持存活时间为10秒，即使没有任务执行，线程也会在达到存活时间后继续存活，以便执行下一项任务。
     * 使用LinkedTransferQueue作为任务队列，该队列是一个无界的并发队列，它支持高并发的场景。
     * 线程的命名格式设置为"single-pool-%d"，便于在多线程环境下识别和调试。
     * 当线程池拒绝执行新任务时（例如当队列已满时），调用线程池的调用者线程将会执行该任务，作为拒绝策略。
     */
    final static ExecutorService singleThreadPool = new ThreadPoolExecutor(1, 1, 10L, TimeUnit.SECONDS,
            new LinkedTransferQueue<>(), // 任务队列，使用LinkedTransferQueue作为等待任务的队列
            new ThreadFactoryBuilder().setNameFormat("single-pool-%d").build(), // 线程工厂，用于命名线程，便于调试和日志记录
            new ThreadPoolExecutor.CallerRunsPolicy() // 拒绝执行处理器，当任务数量超过最大线程数时，由调用线程自己来执行任务
    );

    /**
     * 文档比较线程池，核心线程数=CPU核数，最大线程数Cpu核数
     * 该线程池主要用于执行文档比较任务，其配置完全根据CPU核数来设定
     * 为什么核心线程数和最大线程数都设置为CPU核数？这是因为线程池旨在充分利用CPU资源，减少线程创建和销毁的开销
     * 同时，设置超时时间为10秒，意味着如果线程在空闲状态下超过10秒，则会终止
     * 使用LinkedTransferQueue作为工作队列，它是一个无界的阻塞队列，可以提高线程池的吞吐量
     * 线程工厂使用ThreadFactoryBuilder构建，统一线程名称格式，便于追踪和管理
     * CallerRunsPolicy是一个拒绝执行处理策略，如果线程池处于饱和状态，它会回退到调用者线程来执行任务，这有助于降低任务提交速度，防止系统过载
     **/
    final static ExecutorService compareThreadPool = new ThreadPoolExecutor(CORE_NUM, CORE_NUM, 10L, TimeUnit.SECONDS,
            new LinkedTransferQueue<>(), // 任务队列，使用LinkedTransferQueue作为等待任务的队列
            new ThreadFactoryBuilder().setNameFormat("compare-pool-%d").build(), // 线程工厂，用于命名线程，便于调试和日志记录
            new ThreadPoolExecutor.CallerRunsPolicy() // 拒绝执行处理器，当任务数量超过最大线程数时，由调用线程自己来执行任务
    );

    /**
     * 将小数格式化为百分数
     **/
    static DecimalFormat numFormat = new DecimalFormat("0.00%");


    private static final int DETAIL_SIZE_THRESHOLD = 100000;
    private static final ExecutorService fileThreadPool1 = Executors.newFixedThreadPool(10);
    private static final ExecutorService singleThreadPool2 = Executors.newSingleThreadExecutor();
    private static final ExecutorService compareThreadPool3 = Executors.newFixedThreadPool(10);

    /**
     * 递归遍历入参path目录下所有文档，并两两比较相似度
     *
     * @param path               需要查重的文件夹
     * @param ikFlag             是否打开智能分词，为false显示最小粒度分词结果
     * @param pictureSimFlag     是否计算文档中图片相似度，为是会增加准确率，但会极大增加运算时间
     * @param threshold          相似度阈值
     * @param excelPath          excel绝对路径
     * @param multithreadingFlag 是否开启多线程
     * @param bertFlag           是否使用bert模型计算文档相似度，为是会增加准确率，但会极大增加运算时间
     **/
    public static List<SimilarityOutcome> getSimilarityMode(String path, Boolean ikFlag, Boolean pictureSimFlag,
                                                            Double threshold, String excelPath, Boolean multithreadingFlag,
                                                            Boolean bertFlag) throws Exception {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        System.out.println("开始扫描文档,当前时间:" + LocalDateTime.now().format(formatter));

        //  递归遍历目录；获取所有文档绝对路径
        List<String> allDocAbsolutePath = recursionWord(path);
        if (allDocAbsolutePath.isEmpty()) {
            System.out.println("没有找到任何文档，结束程序。");
            return null;
        }

        //总计算次数
        int sumCount = (allDocAbsolutePath.size() - 1) * allDocAbsolutePath.size() / 2;
        // 存储所有文档
        List<Files> allDocEntityList = new CopyOnWriteArrayList<>();
        //选择线程类型
        ExecutorService threadPool = multithreadingFlag ? fileThreadPool1 : singleThreadPool2;

        CountDownLatch cdl = new CountDownLatch(allDocAbsolutePath.size());
        //遍历处理所有文件
        for (String s : allDocAbsolutePath) {
            Runnable run = () -> {
                try {
                    allDocEntityList.add(getDocEntity(s, pictureSimFlag, ikFlag));
                } finally {
                    cdl.countDown();
                }
            };
            //执行线程
            threadPool.execute(run);
        }

        //线程执行完后再执行主线程
        try {
            cdl.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("阻塞子线程中断异常", e);
        }
        System.out.println("文档读取完成,开始计算相似度,共计" + allDocAbsolutePath.size() + "个文件,需计算" + sumCount + "次,当前时间:" + LocalDateTime.now().format(formatter));

        int detailSize = sumCount > DETAIL_SIZE_THRESHOLD ? 1 : sumCount;
        // 中详细所有数据
        List<SimilarityOutcome> detailList = new CopyOnWriteArrayList<>();
        // 中简略结果数据
        List<SimilarityOutcome> sortMaxResultList = new CopyOnWriteArrayList<>();
        // 中抄袭名单
        List<PlagiarizeVo> plagiarizeVoList = new CopyOnWriteArrayList<>();
        //选择线程类型
        ExecutorService comThreadPool = multithreadingFlag ? compareThreadPool3 : singleThreadPool2;
        CountDownLatch compareCdl = new CountDownLatch(allDocEntityList.size() - 1);
        // 遍所有文档信息冒泡原理两两比较文档相似度
        for (int i = 0; i < allDocEntityList.size() - 1; i++) {
            int finalI = i;
            Runnable run = () -> {
                try {
                    getFinishDocCountModel(pictureSimFlag, threshold, sumCount, allDocEntityList, detailList, sortMaxResultList, plagiarizeVoList, finalI,bertFlag);
                } finally {
                    compareCdl.countDown();
                }
            };
            //执行线程
            comThreadPool.execute(run);
        }
        //线程执行完后再执行主线程
        try {
            compareCdl.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("阻塞子线程中断异常", e);
        }

        if (detailList.isEmpty()) {
            SimilarityOutcome similarityOutEntity =
                    SimilarityOutcome.builder().outcome("本次比较详细结果将超过" + sumCount + "行,防止excel崩溃,此次详细结果不输出,请参考简略结果").build();
            detailList.add(similarityOutEntity);
        }
        // 排序并导出excel
        sortAndImportExcel(excelPath, detailList, sortMaxResultList, plagiarizeVoList);

        // 关闭线程池
        fileThreadPool1.shutdown();
        singleThreadPool2.shutdown();
        compareThreadPool3.shutdown();

        return sortMaxResultList;
    }


    /**
     * 模式1 外层循环调用
     *
     * @param pictureSimFlag    是否计算文档中图片相似度
     * @param threshold         相似度阈值
     * @param sumCount          总次数
     * @param allDocEntityList  所有文档
     * @param detailList        详细结果
     * @param sortMaxResultList 冒泡结果
     * @param plagiarizeVoList  抄袭名单
     * @param i                 索引
     **/
    private static void getFinishDocCountModel(Boolean pictureSimFlag, Double threshold, int sumCount,
                                               List<Files> allDocEntityList, List<SimilarityOutcome> detailList,
                                               List<SimilarityOutcome> sortMaxResultList, List<PlagiarizeVo> plagiarizeVoList,
                                               int i,Boolean bertFlag) {
        // 文档1与其后所有文档的相似度
        List<SimilarityOutcome> docLeftAllSimList = new ArrayList<>();
        if (i < 0 || i >= allDocEntityList.size()) {
            throw new IndexOutOfBoundsException("索引超出了所有docentitylist的边界");
        }
        // 文档1
        Files docLeft = allDocEntityList.get(i);
        // 被比较文本
        for (int j = i + 1; j < allDocEntityList.size(); j++) {
            if (j >= allDocEntityList.size()) {
                throw new IndexOutOfBoundsException("索引超出了所有docentitylist的边界");
            }
            // 被比较文本
            Files docRight = allDocEntityList.get(j);
            SimilarityOutcome cellSimEntity = comparingTwoDoc(docLeft, docRight, pictureSimFlag, threshold, plagiarizeVoList,bertFlag);
            docLeftAllSimList.add(cellSimEntity);
        }

        if (sumCount <= 100000) {
            // 相似度实体加到详细结果中
            detailList.addAll(docLeftAllSimList);
        }

        // 找出和文档1最相似的文档，先降序排序
        docLeftAllSimList = docLeftAllSimList.stream()
                .sorted(Comparator.comparing(SimilarityOutcome::getWeightedSimDouble, Comparator.reverseOrder()))
                .collect(Collectors.toList());

        if (!docLeftAllSimList.isEmpty()) {
            System.out.println(docLeft.getAbsolutepath() + " 与其后的" + docLeftAllSimList.size() + "个文档比较完成,最大相似度:" + docLeftAllSimList.get(0).getWeightedSim());
        } else {
            System.out.println(docLeft.getAbsolutepath() + " 没有找到相似的文档");
        }

        /*  求出每个文档的最大值，如果最大值有多个，只保留10个*/
        final int MAX_RESULTS = 10;
        double maxSimilarity = docLeftAllSimList.isEmpty() ? 0 : docLeftAllSimList.get(0).getWeightedSimDouble();
        int m = 0;
        for (SimilarityOutcome similarityOutEntity : docLeftAllSimList) {
            if (m >= MAX_RESULTS) {
                break;
            }
            if (similarityOutEntity.getWeightedSimDouble().equals(maxSimilarity)) {
                /*  将相似度实体加入简略结果*/
                sortMaxResultList.add(similarityOutEntity);
                m++;
            }
        }
    }


    /**
     * 将几个sheet表数据排序去重并输出excel
     *
     * @param excelPath         excel绝对路径
     * @param detailList        详细名单
     * @param sortMaxResultList 简略名单
     * @param plagiarizeVoList  抄袭名单
     **/
    private static void sortAndImportExcel(String excelPath, List<SimilarityOutcome> detailList, List<SimilarityOutcome> sortMaxResultList, List<PlagiarizeVo> plagiarizeVoList) {

        // 参数校验
        if (excelPath == null || excelPath.isEmpty()) {
            log.error("Excel路径不能为空");
            return;
        }
        if (detailList == null) {
            detailList = Collections.emptyList();
        }
        if (sortMaxResultList == null) {
            sortMaxResultList = Collections.emptyList();
        }
        if (plagiarizeVoList == null) {
            plagiarizeVoList = Collections.emptyList();
        }

        try {
            // 排序详细结果
            List<SimilarityOutcome> sortedDetailList = detailList.stream()
                    .sorted(Comparator.comparing(SimilarityOutcome::getWeightedSimDouble, Comparator.reverseOrder()))
                    .collect(Collectors.toList());

            // 排序简略结果
            List<SimilarityOutcome> sortedSortMaxResultList = sortMaxResultList.stream()
                    .sorted(Comparator.comparing(SimilarityOutcome::getWeightedSimDouble, Comparator.reverseOrder()))
                    .collect(Collectors.toList());

            // 去重抄袭名单
            List<PlagiarizeVo> uniquePlagiarizeListVo = plagiarizeVoList.stream()
                    .collect(Collectors.collectingAndThen(
                            Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(PlagiarizeVo::getTextName))),
                            ArrayList::new));

            // 记录日志
            log.info("相似度计算完成, 开始导出excel文件, 当前时间: {}", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));

            // 导出Excel
            EasyExcelUtil.writeExcel(excelPath, sortedDetailList, sortedSortMaxResultList, uniquePlagiarizeListVo);

            // 记录日志
            log.info("相似度计算结果已存入：{}", excelPath);
        } catch (Exception e) {
            log.error("导出Excel文件时发生错误", e);
        }
    }


    /**
     * 查重方式2：今年的文档两两比较，今年的与往年的比较；往年的互相之间不需要比较
     *
     * @param pathYear       待查重今年文件夹
     * @param pathUsual      待查重往年文件夹
     * @param ikFlag         ik智能分词开关
     * @param pictureSimFlag 图片相似度开关
     * @param threshold      重复度判定阈值
     * @param excelPath      导出的excel绝对路径
     * @author HuDaoquan
     * @date 2022/6/15 13:15
     **/
    public static List<SimilarityOutcome> getSimilarityModeUsual(String pathYear,String pathUsual, Boolean ikFlag, Boolean pictureSimFlag,
                                              Double threshold, String excelPath, Boolean multithreadingFlag,
                                              Boolean bertFlag) throws Exception {
        System.out.println("开始扫描文档,当前时间:" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
        /*  递归遍历目录；获取所有今年文档绝对路径*/
        List<String> thisYearDocAbsolutePath = recursionWord(pathYear);
        // 往年文档路径
        List<String> historyYearDocAbsolutePath = recursionWord(pathUsual);
        //总计算次数
        int sumCount =
                (thisYearDocAbsolutePath.size() - 1) * thisYearDocAbsolutePath.size() / 2 + thisYearDocAbsolutePath.size() * historyYearDocAbsolutePath.size();

        // 存储今年文档
        List<Files> thisYearDocEntityList =
                Collections.synchronizedList(new ArrayList<>(thisYearDocAbsolutePath.size()));
        // 存储往年文档
        List<Files> historyYearDocEntityList =
                Collections.synchronizedList(new ArrayList<>(historyYearDocAbsolutePath.size()));
        //选择线程类型
        ExecutorService threadPool = fileThreadPool;
        ExecutorService comThreadPool = fileThreadPool;
        if (!multithreadingFlag) {
            threadPool = singleThreadPool;
        }
        // 线程计数器
        CountDownLatch thisYearCdl = new CountDownLatch(thisYearDocAbsolutePath.size());
        //遍历处理所有今年文档
        for (String s : thisYearDocAbsolutePath) {
            Runnable run = new Runnable() {
                @Override
                public void run() {
                    //获取今年文档实体
                    thisYearDocEntityList.add(getDocEntity(s, pictureSimFlag, ikFlag));
                    //计数器递减
                    thisYearCdl.countDown();
                }
            };
            //执行线程
            threadPool.execute(run);
        }

        CountDownLatch historyCdl = new CountDownLatch(historyYearDocAbsolutePath.size());
        // 遍历处理所有往年文档
        for (String s : historyYearDocAbsolutePath) {
            Runnable run = new Runnable() {
                @Override
                public void run() {
                    //获取往年文档实体
                    historyYearDocEntityList.add(getDocEntity(s, pictureSimFlag, ikFlag));
                    //计数器递减
                    historyCdl.countDown();
                }
            };
            //执行线程
            threadPool.execute(run);
        }


        //线程执行完后再执行主线程
        try {
            thisYearCdl.await();
            historyCdl.await();
        } catch (InterruptedException e) {
            System.out.println("阻塞子线程中断异常:" + e);
        }
        System.out.println("今年文档数量:" + thisYearDocEntityList.size());
        System.out.println("往年文档数量:" + historyYearDocEntityList.size());

        System.out.println("开始计算相似度,需计算" + sumCount + "次,当前时间:" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
        // 详情名单初始长度
        int detailSize = sumCount;
        if (sumCount > 100000) {
            detailSize = 1;
        }
        // sheet1中详细所有数据
        List<SimilarityOutcome> detailList = Collections.synchronizedList(new ArrayList<>(detailSize));
        // sheet2中简略结果数据
        List<SimilarityOutcome> sortMaxResultList = Collections.synchronizedList(new ArrayList<>(
                thisYearDocEntityList.size()));
        // sheet3中抄袭名单
        List<PlagiarizeVo> plagiarizeVoList = Collections.synchronizedList(new ArrayList<>());

        CountDownLatch compareCdl = new CountDownLatch(thisYearDocEntityList.size());

        // 冒泡排序原理遍历比较文件，遍所有文档信息冒泡原理两两比较文档相似度
        for (int i = 0; i < thisYearDocEntityList.size(); i++) {
            int finalI = i;
            Runnable run = new Runnable() {
                @Override
                public void run() {
                    getFinishDocCountModeUsual(pictureSimFlag, threshold, sumCount, thisYearDocEntityList, historyYearDocEntityList, detailList, sortMaxResultList, plagiarizeVoList, finalI,bertFlag);
                    //计数器递减
                    compareCdl.countDown();
                }
            };
            //执行线程
            comThreadPool.execute(run);
        }
        //线程执行完后再执行主线程
        try {
            compareCdl.await();
        } catch (InterruptedException e) {
            System.out.println("阻塞子线程中断异常:" + e);
        }
        //关闭线程池
        compareThreadPool.shutdown();
        if (detailList.isEmpty()) {
            SimilarityOutcome similarityOutEntity =
                    SimilarityOutcome.builder().outcome("本次比较详细结果将超过" + sumCount + "行,防止excel崩溃,此次详细结果不输出,请参考简略结果").build();
            detailList.add(similarityOutEntity);
        }
        sortAndImportExcel(excelPath, detailList, sortMaxResultList, plagiarizeVoList);
        return detailList;
    }

    /**
     * 获取文档相似度计算结果（模式2）
     * 该方法比较指定年份的文档与历史年份文档之间的相似度，并记录详细的比较结果和最相似的文档
     *
     * @param pictureSimFlag           是否进行图片相似度比较
     * @param threshold                相似度阈值，超过该值的比较结果将被记录
     * @param sumCount                 文档总数，用于判断是否将所有详细比较结果添加到detailList中
     * @param thisYearDocEntityList    今年的文档列表
     * @param historyYearDocEntityList 历史年份的文档列表
     * @param detailList               保存所有详细比较结果的列表
     * @param sortMaxResultList        保存每个文档与最相似文档的比较结果列表
     * @param plagiarizeVoList         保存抄袭检测结果的列表
     * @param i                        当前处理的文档索引
     * @param bertFlag                  是否使用bert模型进行相似度计算
     */
    private static void getFinishDocCountModeUsual(Boolean pictureSimFlag, Double threshold, int sumCount,
                                               List<Files> thisYearDocEntityList, List<Files> historyYearDocEntityList,
                                               List<SimilarityOutcome> detailList, List<SimilarityOutcome> sortMaxResultList,
                                               List<PlagiarizeVo> plagiarizeVoList, int i,Boolean bertFlag) {
        if (thisYearDocEntityList == null || historyYearDocEntityList == null) {
            log.warn("Input lists cannot be null");
            return;
        }

        if (i >= thisYearDocEntityList.size()) {
            return;
        }

        Files docLeft = thisYearDocEntityList.get(i);
        List<SimilarityOutcome> allSimilarityOutcomes = new ArrayList<>();

        try {
            // 比较今年的文档
            for (int j = i + 1; j < thisYearDocEntityList.size(); j++) {
                Files docRight = thisYearDocEntityList.get(j);
                allSimilarityOutcomes.add(comparingTwoDoc(docLeft, docRight, pictureSimFlag, threshold, plagiarizeVoList,bertFlag));
            }

            // 比较往年文档
            for (Files docRight : historyYearDocEntityList) {
                allSimilarityOutcomes.add(comparingTwoDoc(docLeft, docRight, pictureSimFlag, threshold, plagiarizeVoList,bertFlag));
            }
        } catch (Exception e) {
            log.error("Error comparing documents", e);
            return;
        }

        if (sumCount <= 100000 && !allSimilarityOutcomes.isEmpty()) {
            detailList.addAll(allSimilarityOutcomes);
        }

        if (!allSimilarityOutcomes.isEmpty()) {
            // 找出和文档1最相似的文档，先降序排序
            PriorityQueue<SimilarityOutcome> maxHeap = new PriorityQueue<>(Comparator.comparing(SimilarityOutcome::getWeightedSimDouble, Comparator.reverseOrder()));
            maxHeap.addAll(allSimilarityOutcomes);

            log.info("{} 与其后的 {} 个文档比较完成, 最大相似度: {}", docLeft.getAbsolutepath(), allSimilarityOutcomes.size(), maxHeap.peek().getWeightedSim());

            // 求出每个文档的最大值，如果最大值有多个，只保留10个
            double maxSimilarity = maxHeap.peek().getWeightedSimDouble();
            int m = 0;
            while (!maxHeap.isEmpty() && m < 10) {
                SimilarityOutcome similarityOutEntity = maxHeap.poll();
                if (similarityOutEntity.getWeightedSimDouble() != maxSimilarity) {
                    break;
                }
                sortMaxResultList.add(similarityOutEntity);
                m++;
            }
        }
    }


    // 常量定义
    private static final double SIMILARITY_THRESHOLD = 0.95;  // 相似度阈值
    private static final double TEXT_SIM_EXPONENT = 1.5; // 文本相似度指数
    private static final double TEXT_SIM_WEIGHT = 0.6; // 文本相似度权重
    private static final double PICTURE_SIM_WEIGHT = 0.4; // 图片相似度权重

    /**
     * 比较两个文档的相似度，返回相似度实体
     *
     * @param docLeft          文档1
     * @param docRight         文档2
     * @param pictureSimFlag   图片相似度
     * @param threshold        相似度判定阈值
     * @param plagiarizeVoList 抄袭名单
     * @return {@link SimilarityOutcome} 计算得到的相似度实体
     * @author HuDaoquan
     * @date 2022/6/15 13:38
     **/
    public static SimilarityOutcome comparingTwoDoc(Files docLeft, Files docRight, Boolean pictureSimFlag,
                                                    Double threshold, List<PlagiarizeVo> plagiarizeVoList,Boolean bertFlag) {
        // 检查输入参数是否为空
        if (docLeft == null || docRight == null) {
            throw new IllegalArgumentException("文档对象不能为空");
        }
        // 获取文件路径
        String path1 = docLeft.getAbsolutepath();
        String path2 = docRight.getAbsolutepath();
        String text1 = readFileContent(path1);
        String text2 = readFileContent(path2);
        // 余弦相似度
        double conSim = 0D;
        if (bertFlag){
            //  余弦相似度 BERT模型计
             conSim = TextSimilarityUtil.sim(text1, text2);
        }else {
            //余弦相似度 分词+算法
             conSim = CosineSimilarity.sim(docLeft.getWordList(), docRight.getWordList());
        }


        // 获取分词结果
        List<String> docLeftWords = docLeft.getWordList();
        List<String> docRightWords = docRight.getWordList();
        if (docLeftWords == null || docRightWords == null) {
            throw new IllegalArgumentException("文档分词结果不能为空");
        }

        //  杰卡德相似度
        double jaccardSim = Jaccard.jaccardSimilarity(docLeftWords, docRightWords);

        // 海明距离
        SimHash simHash = new SimHash(text1, 64);
        SimHash simHash1 = new SimHash(text2, 64);
        double hammingDistance =(double) simHash.hammingDistance(simHash1)/100;

        //相似度
        double semblance = simHash.getSemblance(simHash1);

        // 文本相似度
        double textSim = (conSim + jaccardSim) / 2;

        //  图片相似度
        double averagePictureSimilarity = 0D;

        //  最终加权相似度
        double weightedSim;

        // 判断结果
        String judgeResult = "";

        if (pictureSimFlag) {
            // 获取图片哈希列表
            List<String> docLeftPictureHashList = docLeft.getPictureHashList();
            List<String> docRightPictureHashList = docRight.getPictureHashList();
            if (docLeftPictureHashList == null) {
                docLeftPictureHashList = Collections.emptyList();
            }
            if (docRightPictureHashList == null) {
                docRightPictureHashList = Collections.emptyList();
            }

            // 文档1中每张图片与文档2中所有图片相似度的最大值的集合
            List<Double> docLeftAllPictureMaxSim = new ArrayList<>(docLeftPictureHashList.size());

            for (String hashLeft : docLeftPictureHashList) {
                List<Double> leftDocPictureSimList = new ArrayList<>(docRightPictureHashList.size());
                for (String hashRight : docRightPictureHashList) {
                    double pictureSim = PHash.getSimilarity(hashLeft, hashRight);
                    leftDocPictureSimList.add(pictureSim);
                    //  找到某张图相似度超过90%就不再比较后面了，直接比较文档1的下一张图
                    if (pictureSim > SIMILARITY_THRESHOLD) {
                        break;
                    }
                }
                // 求出文档1中某张图片与文档2中所有图片相似度的最大值
                double docLeftOnePictureSimMax = Collections.max(leftDocPictureSimList, Comparator.naturalOrder());
                docLeftAllPictureMaxSim.add(docLeftOnePictureSimMax);
            }

            // 求出文档1的所有图片相似度均值作为本次的图片相似度
            averagePictureSimilarity = docLeftAllPictureMaxSim.stream().mapToDouble(Double::doubleValue).average().orElse(0D);

            // 如果任意一个文本图片为空，则总相似度不考虑图片相似度
            if (docLeftPictureHashList.isEmpty() || docRightPictureHashList.isEmpty()) {
                //  将文本相似度结果平方，调整相似度
                weightedSim = Math.pow(textSim, TEXT_SIM_EXPONENT);

            } else {
                //  将文本相似度结果算1.5次方，调整相似度
                weightedSim = Math.pow(textSim, TEXT_SIM_EXPONENT) * TEXT_SIM_WEIGHT + averagePictureSimilarity * PICTURE_SIM_WEIGHT;

            }
        } else {

            // 不计算图片相似度
            textSim = (conSim + jaccardSim) / 2;

            //  将文本相似度结果平方，调整相似度
            weightedSim = Math.pow(textSim, TEXT_SIM_EXPONENT);
        }

        // 如果任一相似度指标超过设定阈值，则判断为疑似抄袭
        if (weightedSim > threshold ||  // 最终加权相似度
            jaccardSim > SIMILARITY_THRESHOLD || // 杰卡德相似度
            conSim > SIMILARITY_THRESHOLD || // 余弦相似度
            averagePictureSimilarity > SIMILARITY_THRESHOLD // 图片相似度
        ) {
            judgeResult = "疑似抄袭";
        } else {
            judgeResult = "非抄袭";
        }
        // 同步块，确保线程安全地访问共享的抄袭名单列表
        synchronized (plagiarizeVoList) {
            // 将疑似抄袭的文档路径添加到抄袭名单中
            plagiarizeVoList.add(PlagiarizeVo.builder().textName(docLeft.getAbsolutepath()).build());
            plagiarizeVoList.add(PlagiarizeVo.builder().textName(docRight.getAbsolutepath()).build());
        }

        return SimilarityOutcome.builder()
                .id(IdUtil.getSnowflakeNextId())
                .outcome(judgeResult)
                .conSim(numFormat.format(conSim))
                .avgpicSim(numFormat.format(averagePictureSimilarity))
                .jaccardSim(numFormat.format(jaccardSim))
                .hammingDistance(numFormat.format(hammingDistance))
                .hashSim(numFormat.format(semblance))
                .sourceFileName(docLeft.getAbsolutepath())
                .weightedSim(numFormat.format(weightedSim))
                .targetFileName(docRight.getAbsolutepath())
                .weightedSimDouble(threshold)
                // TODO 2025/2/28 创建人写死
                .createUser("linyi")
                .createTime(new Date())
                .updateTime(new Date())
                .build();
    }


    private static final Pattern NON_CHAR_PATTERN = Pattern.compile("[0-9a-zA-Z]");

    /**
     * 根据文档路径获取文档实体信息
     * 该方法根据提供的文档绝对路径，返回一个文档实体对象，该对象包含文档的绝对路径、图片路径、分词结果和图片hash结果等信息
     *
     * @param path           文档绝对路径，用于定位文档在文件系统中的位置
     * @param pictureSimFlag 是否处理图片，如果为true，则会计算文档中图片的hash指纹
     * @param ikFlag         ik智能分词开关，如果为true，则使用ik分词器进行分词处理
     * @return 返回文档实体对象，包含文档的绝对路径、图片路径、分词结果和图片hash结果等信息
     **/
    public static Files getDocEntity(String path, Boolean pictureSimFlag, Boolean ikFlag) {
        // 检查路径是否为空或空字符串，确保路径有效性
        if (path == null || path.isEmpty()) {
            throw new IllegalArgumentException("Path cannot be null or empty");
        }

        // 根据路径创建File对象，用于后续操作
        File docFile = new File(path);
        // 确保路径对应的是一个文件，防止路径指向目录或其他无效路径
        if (!docFile.exists() || !docFile.isFile()) {
            throw new IllegalArgumentException("Invalid file path: " + path);
        }

        // 获取文件名，用于后续设置文档实体的文件名属性
        String name = docFile.getName();
        // 构建文档实体对象，设置文件名和绝对路径
        Files docEntity = Files.builder()
                .filename(name)
                .absolutepath(docFile.getAbsolutePath())
                .build();

        try {
            // 读取文件的内容，用于后续分词处理
            String text = readFileContent(path);

            // 移除文本中的数字和字母，保留非字符内容
            if (text != null) {
                text = NON_CHAR_PATTERN.matcher(text).replaceAll("");
                // 对文本进行分词处理，并设置到文档实体
                docEntity.setWordList(HankcsUtil.standardTokenizer(text));
            }

            // 如果需要处理图片，则计算文档中图片的hash指纹
            if (Boolean.TRUE.equals(pictureSimFlag)) {
                List<String> oneDocPictureHashList = ImgExtractionUtil.getWordPicture(docEntity);
                docEntity.setPictureHashList(oneDocPictureHashList);
                // 记录文档的图片数量
                log.info("{} 的图片数量为: {}", docEntity.getFilename(), oneDocPictureHashList.size());
            }
        } catch (Exception e) {
            // 处理过程中发生异常时，记录错误信息
            log.error("Error processing document at path: {}", path, e);
        }
        // 返回构建的文档实体对象
        return docEntity;
    }

    /**
     * 遍历文件夹中的文本文件
     *
     * @param root 遍历的跟路径
     * @return List<String> 存储有所有文本文件绝对路径的字符串数组
     */
    public static List<String> recursionWord(String root) throws Exception {
        // 存储所有文本文件的绝对路径
        List<String> allDocAbsolutePathList = new ArrayList<>();
        // 创建File对象
        File file = new File(root);
        // 判断文件夹是否存在
        if (!file.exists()) {
            throw new Exception("文件夹不存在:" + root);
        }
        // 获取文件夹下的所有文件和文件夹
        File[] subFile = file.listFiles();
        // 判断是否为空
        if (subFile != null) {
            // 遍历文件和文件夹
            for (File value : subFile) {
                String fileName = value.getName();
                /*  判断是文件还是文件夹*/
                if (value.isDirectory()) {
                    /*  文件夹则递归*/
                    List<String> childPathList = recursionWord(value.getAbsolutePath());
                    allDocAbsolutePathList.addAll(childPathList);
                } else if (fileName.endsWith(".doc") || fileName.endsWith(".docx") || fileName.endsWith(".txt")) {
                    /*  绝对路径*/
                    String absolutePath = value.getAbsolutePath();
                    allDocAbsolutePathList.add(absolutePath);
                }
            }
        }
        // 返回所有文本文件的绝对路径
        return allDocAbsolutePathList;
    }

    //通过文件读取文件内容
    public static String readFileContent(String path) {
        //通过文件路径读取text
        try {
            // 获取文件类型，用于选择合适的文本提取方法
            String type = path.substring(path.lastIndexOf("."), path.length()).toLowerCase();
            String text = null;
            // 根据文件类型调用相应的文本提取方法
            switch (type) {
                case ".doc":
                    text = TextreaderUtil.DocReader(path);
                    break;
                case ".docx":
                    text = TextreaderUtil.DocxReader(path);
                    break;
                case ".pdf":
                    text = TextreaderUtil.PdfReader(path);
                    break;
                case ".txt":
                    text = TextreaderUtil.TxtReader(path);
                    break;
                default:
                    // 如果文件类型不受支持，则记录警告并返回空的文档实体
                    log.warn("Unsupported file type: {}", type);
                    return null;
            }

            // 移除文本中的数字和字母，保留非字符内容
            return text == null ? null : text;
        } catch (Exception e) {
            log.error("Error processing document at path: {}", path, e);
            return null;
        }

    }

}

