package com.leopoldhsing.digitalhippo.stripe;

import com.leopoldhsing.digitalhippo.stripe.config.StripeProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@EntityScan(basePackages = "com.leopoldhsing.digitalhippo")
@EnableFeignClients(basePackages = "com.leopoldhsing.digitalhippo.feign")
@EnableConfigurationProperties({StripeProperties.class})
@SpringBootApplication
public class StripeApplication {
    public static void main(String[] args) {
        SpringApplication.run(StripeApplication.class, args);
    }
}
