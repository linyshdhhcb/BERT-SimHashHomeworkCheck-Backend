package com.linyi.check.entity.vo.similarityOutcome;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
* @Author: linyi
* @Date: 2025-02-27 20:49:47
* @ClassName: SimilarityOutcomeUpdateVo
* @Version: 1.0
* @Description: 相似度结果表修改实体
*/

@Data
@Schema(name = "相似度结果表修改实体")
public class SimilarityOutcomeUpdateVo implements Serializable {
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
     * 海明距离
     */
    @TableField("hamming_distance")
    @Schema(name = "hammingDistance",description = "海明距离",type = "varchar")
    private String hammingDistance;

    /**
     * 相似度
     */
    @TableField("hash_sim")
    @Schema(name = "hashSim",description = "相似度",type = "varchar")
    private String hashSim;

    /**
     * 余弦相似度
     */
    @TableField("con_sim")
    @Schema(name = "conSim",description = "余弦相似度",type = "varchar")
    private String conSim;

    /**
     * 图片相似度
     */
    @TableField("avgpic_sim")
    @Schema(name = "avgpicSim",description = "图片相似度",type = "varchar")
    private String avgpicSim;

    /**
     * 加权相似度
     */
    @TableField("weighted_sim")
    @Schema(name = "weightedSim",description = "加权相似度",type = "varchar")
    private String weightedSim;

    /**
     * 加权相似度数值型
     */
    @TableField("weighted_sim_double")
    @Schema(name = "weightedSimDouble",description = "加权相似度数值型",type = "double")
    private Double weightedSimDouble;

}
