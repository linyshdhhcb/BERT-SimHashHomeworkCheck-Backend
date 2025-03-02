package com.linyi.check.entity.vo.files;

import com.linyi.check.common.model.PageResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
* @Author: linyi
* @Date: 2025-02-27 20:40:38
* @ClassName: FilesQueryVo
* @Version: 1.0
* @Description: 文件信息表查询实体
*/

@Data
@Schema(name = "文件信息表查询实体")
public class FilesQueryVo extends PageResponse implements Serializable {

}
