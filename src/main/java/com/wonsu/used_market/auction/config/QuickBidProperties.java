package com.wonsu.used_market.auction.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@ConfigurationProperties(prefix = "auction.quick-bid")
public class QuickBidProperties {
    private final int threshold;
    private final int under;
    private final int over;

    public QuickBidProperties(int threshold, int under, int over) {
        this.threshold = threshold;
        this.under = under;
        this.over = over;
    }
}

