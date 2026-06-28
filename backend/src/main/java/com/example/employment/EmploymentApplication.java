package com.example.employment;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.example.employment.mapper")
@SpringBootApplication
public class EmploymentApplication {

    public static void main(String[] args) {
        SpringApplication.run(EmploymentApplication.class, args);
    }
}
