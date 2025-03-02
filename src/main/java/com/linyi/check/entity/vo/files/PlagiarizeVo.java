package com.linyi.check.entity.vo.files;


import lombok.*;

/**
 * @Author: linyi
 * @Date: 2025/2/27
 * @ClassName: Plagiarize
 * @Version: 1.0
 * @Description: 疑似名单实体类
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PlagiarizeVo {
    /**
     * 疑似抄袭文件名
     **/
    private String textName;
    
    
}
