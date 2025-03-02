package com.linyi.check.entity.vo.similarityOutcome;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @Author: linyi
 * @Date: 2025/2/28
 * @ClassName: SimilarityOutcomeCheckVo
 * @Version: 1.0
 * @Description: 相似度结果表比较实体
 */
@Data
@Schema(name = "相似度结果表比较实体")
public class SimilarityOutcomeCheckVo {
    /**
     * 文件夹路径
     */
    @Schema(name = "path",description = "文件夹路径",type = "varchar")
    private String path;

    /**
     * 是否智能分词
     */
    @Schema(name = "isFlag",description = "是否智能分词",type = "varchar",example = "true")
    private Boolean isFlag;

    /**
     * 是否计算文档图片
     */
    @Schema(name = "pictureSimFlag",description = "是否计算文档图片",type = "varchar",example = "false")
    private Boolean pictureSimFlag;

    /**
     * 相似度阈值
     */
    @Schema(name = "threshold",description = "相似度阈值",type = "double",example = "0.8")
    private Double threshold;

    /**
     * 生成excel路径
     */
    @Schema(name = "excelPath",description = "生成excel路径",type = "varchar")
    private String excelPath;

    /**
     *
     * 是否多线程
     */
    @Schema(name = "multithreadingFlag",description = "是否多线程",type = "varchar",example = "true")
    private Boolean multithreadingFlag;

    /**
     * 是否使用bert模型
     */
    @Schema(name = "bertFlag",description = "是否使用bert模型",type = "varchar",example = "false")
    private Boolean bertFlag;
}
