package com.linyi.check.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.linyi.check.common.model.PageResult;
import com.linyi.check.entity.SimilarityOutcome;
import com.linyi.check.entity.vo.SimilarityOutcomeCheckUsualVo;
import com.linyi.check.entity.vo.similarityOutcome.SimilarityOutcomeAddVo;
import com.linyi.check.entity.vo.similarityOutcome.SimilarityOutcomeCheckVo;
import com.linyi.check.entity.vo.similarityOutcome.SimilarityOutcomeQueryVo;
import com.linyi.check.entity.vo.similarityOutcome.SimilarityOutcomeUpdateVo;
import com.linyi.check.mapper.SimilarityOutcomeMapper;
import com.linyi.check.service.SimilarityOutcomeService;
import com.linyi.check.utils.TextReaderCompareOptimizeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
* @Author: linyi
* @Date: 2025-02-27 20:49:47
* @ClassName: SimilarityOutcomeServiceImpl
* @Version: 1.0
* @Description: 相似度结果表 服务实现层
*/
@Slf4j
@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
@SuppressWarnings({"unchecked", "rawtypes"})
public class SimilarityOutcomeServiceImpl extends ServiceImpl<SimilarityOutcomeMapper, SimilarityOutcome> implements SimilarityOutcomeService {

    @Autowired
    private SimilarityOutcomeMapper similarityOutcomeMapper;

    @Override
    public List<SimilarityOutcome> checkTextSimilarityUsual(SimilarityOutcomeCheckUsualVo similarityOutcomeCheckVo) {
        long l = System.currentTimeMillis();
        List<SimilarityOutcome> similarityOutcomeList = null;
        try {
            if (Optional.ofNullable(similarityOutcomeCheckVo.getExcelPath()).isEmpty() || similarityOutcomeCheckVo.getExcelPath().isBlank()) {
                String excelPath = similarityOutcomeCheckVo.getPath() + "//查重结果"
                        .concat(new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()))
                        .concat(".xlsx");
                similarityOutcomeCheckVo.setExcelPath(excelPath);
            }
            similarityOutcomeList = TextReaderCompareOptimizeUtil.getSimilarityModeUsual(
                    similarityOutcomeCheckVo.getPath(),
                    similarityOutcomeCheckVo.getPathUsual(),
                    similarityOutcomeCheckVo.getIsFlag(),
                    similarityOutcomeCheckVo.getPictureSimFlag(),
                    similarityOutcomeCheckVo.getThreshold(),
                    similarityOutcomeCheckVo.getExcelPath(),
                    similarityOutcomeCheckVo.getMultithreadingFlag(),
                    similarityOutcomeCheckVo.getBertFlag());

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        String msg = "计算完成，耗时：" + (System.currentTimeMillis() - l)/1000 + "s";
        //将计算结果存入数据库
        this.saveBatch(similarityOutcomeList);

        return similarityOutcomeList;
    }

    @Override
    public List<SimilarityOutcome> checkTextSimilarity(SimilarityOutcomeCheckVo similarityOutcomeCheckVo) {
        long l = System.currentTimeMillis();
        List<SimilarityOutcome> similarityOutcomeList = null;
        try {
            if (Optional.ofNullable(similarityOutcomeCheckVo.getExcelPath()).isEmpty() || similarityOutcomeCheckVo.getExcelPath().isBlank()){
                String excelPath = similarityOutcomeCheckVo.getPath() + "//查重结果"
                        .concat(new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()))
                        .concat(".xlsx");
                similarityOutcomeCheckVo.setExcelPath(excelPath);
            }
            //计算相似度
            similarityOutcomeList = TextReaderCompareOptimizeUtil.getSimilarityMode(
                    similarityOutcomeCheckVo.getPath(),
                    similarityOutcomeCheckVo.getIsFlag(),
                    similarityOutcomeCheckVo.getPictureSimFlag(),
                    similarityOutcomeCheckVo.getThreshold(),
                    similarityOutcomeCheckVo.getExcelPath(),
                    similarityOutcomeCheckVo.getMultithreadingFlag(),
                    similarityOutcomeCheckVo.getBertFlag()
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        String msg = "计算完成，耗时：" + (System.currentTimeMillis() - l)/1000 + "s";
        //将计算结果存入数据库
        this.saveBatch(similarityOutcomeList);

        return similarityOutcomeList;
    }

    @Override
    public PageResult<SimilarityOutcome> similarityOutcomePage(SimilarityOutcomeQueryVo similarityOutcomeQueryVo) {
        LambdaQueryWrapper<SimilarityOutcome> queryWrapper = new LambdaQueryWrapper<>();
        //TODO 需要补充条件查询

        //分页数据
        Page<SimilarityOutcome> page = new Page<>(similarityOutcomeQueryVo.getPageNum(),similarityOutcomeQueryVo.getPageSize());
        //查询数据
        Page<SimilarityOutcome> pageNew = similarityOutcomeMapper.selectPage(page, queryWrapper);
        //返回分页数据
        return new PageResult<>(pageNew.getRecords(), pageNew.getTotal(), pageNew.getPages(), similarityOutcomeQueryVo.getPageNum(), similarityOutcomeQueryVo.getPageSize());
    }

    @Override
    public Boolean similarityOutcomeAdd(SimilarityOutcomeAddVo similarityOutcomeAddVo){
        //创建实体对象
        SimilarityOutcome similarityOutcome = new SimilarityOutcome();
        //复制属性
        BeanUtils.copyProperties(similarityOutcomeAddVo, similarityOutcome);
        //插入数据
        return similarityOutcomeMapper.insert(similarityOutcome) > 0 ? true : false;
    }

    @Override
    public Boolean similarityOutcomeUpdate(SimilarityOutcomeUpdateVo similarityOutcomeUpdateVo){
        //根据ID查询数据
        SimilarityOutcome byId=this.getById(similarityOutcomeUpdateVo.getId());
        //判断数据是否存在
        if(Optional.ofNullable(byId).isEmpty()){
            log.error("数据不存在");
            return false;
        }
        //复制属性
        BeanUtils.copyProperties(similarityOutcomeUpdateVo, byId);
        //修改数据
        return similarityOutcomeMapper.updateById(byId) > 0 ? true : false;
    }




}
