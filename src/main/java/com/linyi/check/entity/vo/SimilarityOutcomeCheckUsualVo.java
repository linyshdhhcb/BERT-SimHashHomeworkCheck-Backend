package com.linyi.check.entity.vo;

import com.linyi.check.entity.vo.similarityOutcome.SimilarityOutcomeCheckVo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Author: linyi
 * @Date: 2025/3/1
 * @ClassName: SimilarityOutcomeCheckUsualVo
 * @Version: 1.0
 * @Description:
 */
@Data
@Schema(description = "往年作业查重")
public class SimilarityOutcomeCheckUsualVo extends SimilarityOutcomeCheckVo {

    @Schema(name = "pathUsual",description = "往年作业文件夹路径",type = "varchar")
    private String pathUsual;
}
