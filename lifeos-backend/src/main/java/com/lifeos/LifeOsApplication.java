package com.lifeos;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.lifeos.mapper")
public class LifeOsApplication {

    public static void main(String[] args) {
        SpringApplication.run(LifeOsApplication.class, args);
        System.out.println("==========================================");
        System.out.println("LifeOS 后端服务启动成功！");
        System.out.println("API 文档地址: http://localhost:8080/doc.html");
        System.out.println("Swagger 地址: http://localhost:8080/swagger-ui/index.html");
        System.out.println("==========================================");
    }
}
