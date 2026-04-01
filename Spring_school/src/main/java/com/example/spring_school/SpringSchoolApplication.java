package com.example.spring_school;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.spring_school.mapper")
public class SpringSchoolApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringSchoolApplication.class, args);
    }

}
