package com.iwhalecloud.bote.sms.bill;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 启动之前需要注释模块pom中的bote-service依赖
 * 同时修改BillSmsServiceClient文件：移除对BillSmsClient的继承。调试完之后改回去即可
 */
@SpringBootApplication
@SuppressWarnings("PMD.UseUtilityClass")
public class MockBillApplication {

    public static void main(String[] args) {
        SpringApplication.run(MockBillApplication.class, args);
    }

}
