package com.linyi.check.entity.vo.similarityOutcome;

import com.baomidou.mybatisplus.annotation.TableField;
import com.linyi.check.common.model.PageResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
* @Author: linyi
* @Date: 2025-02-27 20:49:47
* @ClassName: SimilarityOutcomeQueryVo
* @Version: 1.0
* @Description: 相似度结果表查询实体
*/

@Data
@Schema(name = "相似度结果表查询实体")
public class SimilarityOutcomeQueryVo extends PageResponse implements Serializable {
    /**
     * 主键ID
     */
    private Serializable id;

    /**
     * 结果
     */
    @TableField("outcome")
    @Schema(name = "outcome",description = "结果",type = "varchar")
    private String outcome;

    /**
     * 文件名
     */
    @TableField("source_file_name")
    @Schema(name = "sourceFileName",description = "文件名",type = "varchar")
    private String sourceFileName;

    /**
     * 被比较的文件名
     */
    @TableField("target_file_name")
    @Schema(name = "targetFileName",description = "被比较的文件名",type = "varchar")
    private String targetFileName;

    /**
     * 海明距离 min
     */
    @Schema(name = "hammingDistanceMin",description = "海明距离min",type = "varchar")
    private String hammingDistanceMin;

    /**
     * 海明距离 min
     */
    @Schema(name = "hammingDistanceMax",description = "海明距离max",type = "varchar")
    private String hammingDistanceMax;

    /**
     * 相似度min
     */
    @Schema(name = "hashSimMin",description = "相似度min",type = "varchar")
    private String hashSimMin;
    /**
     * 相似度max
     */
    @Schema(name = "hashSimMax",description = "相似度max",type = "varchar")
    private String hashSimMax;
    /**
     * 余弦相似度min
     */
    @Schema(name = "conSimMin",description = "余弦相似度min",type = "varchar")
    private String conSimMin;
    /**
     * 余弦相似度max
     */
    @Schema(name = "conSimMax",description = "余弦相似度max",type = "varchar")
    private String conSimMax;

    /**
     * 图片相似度min
     */
    @Schema(name = "avgpicSimMin",description = "图片相似度min",type = "varchar")
    private String avgpicSimMin;
    /**
     * 图片相似度max
     */
    @Schema(name = "avgpicSimMax",description = "图片相似度max",type = "varchar")
    private String avgpicSimMax;

    /**
     * 加权相似度min
     */
    @Schema(name = "weightedSimMin",description = "加权相似度min",type = "varchar")
    private String weightedSimMin;

    /**
     * 加权相似度max
     */
    @Schema(name = "weightedSimMax",description = "加权相似度max",type = "varchar")
    private String weightedSimMax;

    /**
     * 加权相似度数值型
     */
    @Schema(name = "weightedSimDouble",description = "加权相似度数值型",type = "double")
    private Double weightedSimDouble;

}
