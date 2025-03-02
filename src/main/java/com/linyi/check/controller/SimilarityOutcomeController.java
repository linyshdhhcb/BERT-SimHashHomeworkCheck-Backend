package com.linyi.check.controller;

import com.linyi.check.common.model.PageResult;
import com.linyi.check.common.model.Result;
import com.linyi.check.entity.SimilarityOutcome;
import com.linyi.check.entity.vo.SimilarityOutcomeCheckUsualVo;
import com.linyi.check.entity.vo.similarityOutcome.SimilarityOutcomeAddVo;
import com.linyi.check.entity.vo.similarityOutcome.SimilarityOutcomeCheckVo;
import com.linyi.check.entity.vo.similarityOutcome.SimilarityOutcomeQueryVo;
import com.linyi.check.entity.vo.similarityOutcome.SimilarityOutcomeUpdateVo;
import com.linyi.check.service.SimilarityOutcomeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;
import java.util.List;
import java.util.Map;


/**
* @Author: linyi
* @Date: 2025-02-27 20:49:47
* @ClassName: SimilarityOutcomeController
* @Version: 1.0
* @Description: 相似度结果表 控制层
*/

@Tag(name = "相似度结果表管理模块")
@Slf4j
@Validated
@RestController
@RequestMapping("/similarityOutcome")
@SuppressWarnings({"unchecked", "rawtypes"})
public class SimilarityOutcomeController {

    @Autowired
    private SimilarityOutcomeService similarityOutcomeService;


    @PostMapping("/check")
    @Operation(summary = "查重生成Excel表")
    public Result<List<SimilarityOutcome>> checkTextSimilarity(@RequestBody SimilarityOutcomeCheckVo similarityOutcomeCheckVo) {
        return Result.success(similarityOutcomeService.checkTextSimilarity(similarityOutcomeCheckVo));
    }

    @PostMapping("/checkUsual")
    @Operation(summary = "与往年作业查重生成Excel表")
    public Result<List<SimilarityOutcome>> checkTextSimilarityUsual(@RequestBody SimilarityOutcomeCheckUsualVo similarityOutcomeCheckVo) {
        return Result.success(similarityOutcomeService.checkTextSimilarityUsual(similarityOutcomeCheckVo));
    }

    /**
     * 分页查询相似度结果表
     *
     * @param similarityOutcomeQueryVo 分页查询实体
     * @return Result<PageResult<SimilarityOutcome>> 返回分页数据
     */
    @Operation(summary = "分页查询相似度结果表")
    @PostMapping("/similarityOutcomePage")
    public Result<PageResult<SimilarityOutcome>> similarityOutcomePage(@RequestBody SimilarityOutcomeQueryVo similarityOutcomeQueryVo) {
        return Result.success(similarityOutcomeService.similarityOutcomePage(similarityOutcomeQueryVo));
    }

    /**
     * 新增相似度结果表
     *
     * @param similarityOutcomeAddVo 新增实体
     * @return Result<Boolean> 返回结果(true/false)
     */
    @Operation(summary = "新增相似度结果表")
    @PostMapping("/similarityOutcomeAdd")
    public Result<Boolean> similarityOutcomeAdd(@RequestBody SimilarityOutcomeAddVo similarityOutcomeAddVo) {
        return Result.success(similarityOutcomeService.similarityOutcomeAdd(similarityOutcomeAddVo));
    }

    /**
     * 根据主键ID删除相似度结果表
     *
     * @param id 主键id
     * @return Result<Boolean> 返回结果(true/false)
     */
    @Operation(summary = "根据主键ID删除相似度结果表")
    @DeleteMapping("similarityOutcomeDelete")
    public Result<Boolean> similarityOutcomeDelete(@RequestParam Serializable id) {
        return Result.success(similarityOutcomeService.removeById(id));
    }

    /**
    * 根据主键ID批量删除相似度结果表
    *
    * @param ids 主键id集合
    * @return Result<Boolean> 返回结果(true/false)
    */
    @Operation(summary = "根据主键ID批量删除相似度结果表")
    @DeleteMapping("similarityOutcomeListDelete")
    public Result<Boolean> similarityOutcomeListDelete(@RequestParam List<Serializable> ids) {
        return Result.success(similarityOutcomeService.removeByIds(ids));
        }

    /**
     * 根据主键ID修改相似度结果表
     *
     * @param similarityOutcomeUpdateVo 修改实体
     * @return Result<Boolean> 返回结果(true/false)
     */
    @Operation(summary = "根据主键ID修改相似度结果表")
    @PutMapping("similarityOutcomeUpdate")
    public Result<Boolean> similarityOutcomeUpdate(@RequestBody SimilarityOutcomeUpdateVo similarityOutcomeUpdateVo) {
        return Result.success(similarityOutcomeService.similarityOutcomeUpdate(similarityOutcomeUpdateVo));
    }

    /**
     * 根据主键ID查询相似度结果表
     *
     * @param id 主键id
     * @return Result<SimilarityOutcome> 返回相似度结果表
     */
    @Operation(summary = "根据主键ID查询相似度结果表")
    @GetMapping("/getInfo")
    public Result<SimilarityOutcome> similarityOutcomeUpdate(@RequestParam Serializable id) {
        return Result.success(similarityOutcomeService.getById(id));
    }

}
