package com.linyi.check.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.linyi.check.common.model.PageResult;
import com.linyi.check.common.model.Result;
import com.linyi.check.entity.SimilarityOutcome;
import com.linyi.check.entity.vo.SimilarityOutcomeCheckUsualVo;
import com.linyi.check.entity.vo.similarityOutcome.SimilarityOutcomeAddVo;
import com.linyi.check.entity.vo.similarityOutcome.SimilarityOutcomeCheckVo;
import com.linyi.check.entity.vo.similarityOutcome.SimilarityOutcomeQueryVo;
import com.linyi.check.entity.vo.similarityOutcome.SimilarityOutcomeUpdateVo;

import java.util.List;

/**
* @Author: linyi
* @Date: 2025-02-27 20:49:47
* @ClassName: SimilarityOutcomeService
* @Version: 1.0
* @Description: 相似度结果表 服务层
*/
public interface SimilarityOutcomeService extends IService<SimilarityOutcome> {
    /**
     * 分页查询
     *
     * @param similarityOutcomeQueryVo 分页查询实体
     * @return PageResult<SimilarityOutcome>
     */
    PageResult<SimilarityOutcome> similarityOutcomePage(SimilarityOutcomeQueryVo similarityOutcomeQueryVo);

    /**
     * 新增
     *
     * @param similarityOutcomeAddVo 新增实体
     * @return Boolean
     */
    Boolean similarityOutcomeAdd(SimilarityOutcomeAddVo similarityOutcomeAddVo);

    /**
     * 修改
     *
     * @param similarityOutcomeUpdateVo 修改实体
     * @return Boolean
     */
    Boolean similarityOutcomeUpdate(SimilarityOutcomeUpdateVo similarityOutcomeUpdateVo);

    List<SimilarityOutcome> checkTextSimilarity(SimilarityOutcomeCheckVo similarityOutcomeCheckVo);

    List<SimilarityOutcome> checkTextSimilarityUsual(SimilarityOutcomeCheckUsualVo similarityOutcomeCheckVo);
}
