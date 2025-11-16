package com.wonsu.used_market;

import com.wonsu.used_market.auction.config.AuctionProperties;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import java.util.TimeZone;

@SpringBootApplication
@EnableConfigurationProperties(AuctionProperties.class)
public class UsedMarketApplication {

    @PostConstruct
    public void setTimeZone() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
        System.out.println("### JVM TimeZone Set to Asia/Seoul ###");
    }

	public static void main(String[] args) {

        SpringApplication.run(UsedMarketApplication.class, args);
	}

}
