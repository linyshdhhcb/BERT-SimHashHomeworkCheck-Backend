package com.linyi.check.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.linyi.check.common.model.PageResult;
import com.linyi.check.entity.Files;
import com.linyi.check.entity.vo.files.FilesAddVo;
import com.linyi.check.entity.vo.files.FilesQueryVo;
import com.linyi.check.entity.vo.files.FilesUpdateVo;

/**
* @Author: linyi
* @Date: 2025-02-27 20:40:38
* @ClassName: FilesService
* @Version: 1.0
* @Description: 文件信息表 服务层
*/
public interface FilesService extends IService<Files> {
    /**
     * 分页查询
     *
     * @param filesQueryVo 分页查询实体
     * @return PageResult<Files>
     */
    PageResult<Files> filesPage(FilesQueryVo filesQueryVo);

    /**
     * 新增
     *
     * @param filesAddVo 新增实体
     * @return Boolean
     */
    Boolean filesAdd(FilesAddVo filesAddVo);

    /**
     * 修改
     *
     * @param filesUpdateVo 修改实体
     * @return Boolean
     */
    Boolean filesUpdate(FilesUpdateVo filesUpdateVo);
}
