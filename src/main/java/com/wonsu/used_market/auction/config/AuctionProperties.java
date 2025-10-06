package com.wonsu.used_market.auction.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@ConfigurationProperties(prefix = "auction")
public class AuctionProperties {

    private final QuickBid quickBid;
    private final int freeMin;

    public AuctionProperties(QuickBid quickBid, int freeMin) {
        this.quickBid = quickBid;
        this.freeMin = freeMin;
    }

    @Getter
    public static class QuickBid {
        private final int threshold;
        private final int under;
        private final int over;

        public QuickBid(int threshold, int under, int over) {
            this.threshold = threshold;
            this.under = under;
            this.over = over;
        }
    }
}
