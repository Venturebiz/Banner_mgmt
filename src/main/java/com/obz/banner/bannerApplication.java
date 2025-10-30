package com.obz.banner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class bannerApplication {

    public static void main(String[] args) {
        SpringApplication.run(bannerApplication.class, args);
        System.out.print("Backend UP");
    }
}

