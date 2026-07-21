package com.fanfaction;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.fanfaction.mapper")
public class FanFactionApplication {
    public static void main(String[] args) {
        SpringApplication.run(FanFactionApplication.class, args);
    }
}
