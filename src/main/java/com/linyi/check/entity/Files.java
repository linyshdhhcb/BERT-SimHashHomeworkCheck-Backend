package com.linyi.check.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @Author: linyi
 * @Date: 2025-02-27 20:40:38
 * @ClassName: SimilarityOutcome
 * @Version: 1.0
 * @Description: 相似度结果表
 */

/**
 * @Author: linyi
 * @Date: 2025-02-27 20:40:38
 * @ClassName: Files
 * @Version: 1.0
 * @Description: 文件信息表
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("files")
@Schema(name = "文件信息表")
public class Files implements Serializable {


    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    @Schema(name = "id",description = "主键ID",type = "bigint")
    private Long id;

    /**
     * 文件绝对路径
     */
    @TableField("absolutePath")
    @Schema(name = "absolutepath",description = "文件绝对路径",type = "varchar")
    private String absolutepath;

    /**
     * 文件名
     */
    @TableField("fileName")
    @Schema(name = "filename",description = "文件名",type = "varchar")
    private String filename;

    /**
     * 文件大小
     */
    @TableField("fileSize")
    @Schema(name = "filesize",description = "文件大小",type = "bigint")
    private Long filesize;

    /**
     * 文件类型（扩展名）
     */
    @TableField("fileType")
    @Schema(name = "filetype",description = "文件类型（扩展名）",type = "varchar")
    private String filetype;

    /**
     * 本文所有图片的父路径
     */
    @TableField("pictureParentPath")
    @Schema(name = "pictureparentpath",description = "本文所有图片的父路径",type = "varchar")
    private String pictureparentpath;

    /**
     * 分词结果
     */
    @TableField("word_json")
    @Schema(name = "wordJson",description = "分词结果",type = "varchar")
    private String wordJson;

    /**
     * 图片hash指纹
     */
    @TableField("picture_hash_json")
    @Schema(name = "pictureHashJson",description = "图片hash指纹",type = "varchar")
    private String pictureHashJson;

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

    /**
     * 分词结果集合
     **/
    @TableField(exist = false)
    @Schema(name = "wordList", description = "分词结果集合", type = "varchar")
    private List<String> wordList;

    /**
     * 图片hash指纹
     **/
    @TableField(exist = false)
    @Schema(name = "pictureHashList", description = "图片hash指纹", type = "varchar")
    private List<String> pictureHashList;

}
