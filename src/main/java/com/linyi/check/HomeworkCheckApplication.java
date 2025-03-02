package com.linyi.check;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @Author: linyi
 * @Date: 2025/2/27
 * @ClassName: HomeworkCheckApplication
 * @Version: 1.0
 * @Description:
 */
@SpringBootApplication
@MapperScan("com.linyi.check.mapper")
public class HomeworkCheckApplication {
    public static void main(String[] args) {
        org.springframework.boot.SpringApplication.run(HomeworkCheckApplication.class, args);
    }
}
