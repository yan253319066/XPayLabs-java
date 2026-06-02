package com.yan.xpay.domain;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class CryptoPrice {
	private String id;// "id": "bitcoin",
	private String symbol;//"symbol": "btc",
	private String name;//"name": "Bitcoin",
	private String image;//"image": "https://coin-images.coingecko.com/coins/images/1/large/bitcoin.png?1696501400",
	private BigDecimal currentPrice;//"current_price": 114347,
	private BigDecimal totalVolume;//"total_volume": 64444108940,
	private BigDecimal high24h;// "high_24h": 115934,
	private BigDecimal low24h;//"low_24h": 113968,
	private BigDecimal priceChange24h;//"price_change_24h": -1392.280413759392,
	private String priceChangePercentage24h;//"price_change_percentage_24h": -1.20295,
	private BigDecimal totalSupply;//"total_supply": 19934193.0,
	private BigDecimal maxSupply;//"max_supply": 21000000.0,
	private Date lastUpdated;//"last_updated": "2025-10-14T02:02:02.595Z"
}
