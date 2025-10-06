package com.wonsu.used_market;

import com.wonsu.used_market.auction.config.AuctionProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(AuctionProperties.class)
public class UsedMarketApplication {

	public static void main(String[] args) {
		SpringApplication.run(UsedMarketApplication.class, args);
	}

}
