package com.tencent.timi.annualparty;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AnnualPartyApplication {

    public static void main(String[] args) {
        SpringApplication.run(AnnualPartyApplication.class, args);
        System.out.println("服务端启动");
    }

}
