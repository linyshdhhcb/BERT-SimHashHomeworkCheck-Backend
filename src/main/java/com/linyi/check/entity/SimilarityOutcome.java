package com.linyi.check.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
* @Author: linyi
* @Date: 2025-02-27 20:49:47
* @ClassName: SimilarityOutcome
* @Version: 1.0
* @Description: 相似度结果表
*/

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("similarity_outcome")
@Schema(name = "相似度结果表")
public class SimilarityOutcome implements Serializable {


    private static final long serialVersionUID = 1L;

    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.AUTO)
    @Schema(name = "id",description = "主键id",type = "bigint")
    private Long id;

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
     * 杰卡德相似度
     */
    @TableField("jaccard_sim")
    @Schema(name = "jaccardSim",description = "杰卡德相似度",type = "varchar")
    private String jaccardSim;

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

    /**
     * 创建时间
     */
    @TableField("create_time")
    @Schema(name = "createTime",description = "创建时间",type = "datetime")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * 修改时间
     */
    @TableField("update_time")
    @Schema(name = "updateTime",description = "修改时间",type = "datetime")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    /**
     * 创建人
     */
    @TableField("create_user")
    @Schema(name = "createUser",description = "创建人",type = "varchar")
    private String createUser;

    /**
     * 修改人
     */
    @TableField("update_user")
    @Schema(name = "updateUser",description = "修改人",type = "varchar")
    private String updateUser;

    /**
     * 逻辑删除标识，0：未删除，1：已删除
     */
    @TableField("is_deleted")
    @Schema(name = "isDeleted",description = "逻辑删除标识，0：未删除，1：已删除",type = "tinyint")
    private Byte isDeleted;

}
