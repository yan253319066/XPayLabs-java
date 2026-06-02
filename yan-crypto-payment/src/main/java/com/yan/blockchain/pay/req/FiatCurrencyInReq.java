package com.yan.blockchain.pay.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FiatCurrencyInReq {
	@NotBlank(message = "The sign cannot be left blank.")
	private String sign;
	/**
	 * timestamp
	 */
	@NotNull(message = "The timestamp cannot be left blank.")
	private Long timestamp;
	/**
	 * nonce
	 */
	@NotBlank(message = "The nonce cannot be left blank.")
	private String nonce;

	/**
	 * 商户订单号
	 */
	@NotBlank(message = "orderNo不能为空")
	private String orderNo;//	String	是	商户订单号	12345678
	/**
	 * 交易金额	支持两位小数 单位：元，
	 */
	@NotBlank(message = "amount不能为空")
	private String amount;//	String	是	交易金额	支持两位小数 单位：元，
	/**
	 * 交易币种
	 * BDT 孟加拉塔卡  NGN 尼日利亚奈拉  EGP 埃及镑  INR 印度卢比  BRL 巴西  PHP 菲律宾比索  IDR 印尼卢比  MXN 墨西哥比索  SGD 新加坡元  PEN 秘鲁新索尔  VND 越南盾  KRW 韩币  MYR 马来西亚  USD 美元  COP 哥伦比亚  ZAR 南非  PKR 巴基斯坦  MMK 缅币  CIVXOF 科特迪瓦西非法郎
	 */
	@NotBlank(message = "currency不能为空")
	private String currency;//	String	是	交易币种	详细参考文档：[交易币种]
	/**
	 * 支付类型编码
	 * 8010	孟加拉钱包
	 * 8011	孟加拉NAGAD
	 * 8012	孟加拉BKASH
	 * 8013	孟加拉Rocket
	 * 8020	埃及网银
	 * 8030	尼日利亚网银
	 * 8041	印度唤醒
	 * 8042	印度原生
	 * 8050	泰国网银
	 * 8051	泰国扫码
	 * 8060	巴西网银
	 * 8070	新加坡网银
	 * 8080	印尼网银
	 * 8081	印尼钱包LINKAJA
	 * 8082	印尼钱包OVO
	 * 8083	印尼钱包DANA
	 * 8084	印尼QRIS
	 * 8091	巴基斯坦EASYPAISA
	 * 8092	巴基斯坦JAZZCASH
	 * 8101	菲律宾MAYA
	 * 8102	菲律宾QRPH
	 * 8103	菲律宾Gcash扫码
	 * 8110	墨西哥网银
	 * 8121	越南bank
	 * 8122	超南momo
	 * 8123	越南zalo
	 * 8124	越南bank-scan
	 * 8125	越南Viettelpay
	 * 8130	秘鲁网银
	 * 8140	马来西亚网银
	 * 8141	马来西亚扫码
	 * 8150	韩国网银
	 * 8151	韩国虚拟卡
	 * 8161	美国信用卡
	 * 8162	美国cash钱包
	 * 8170	哥仑比亚网银
	 * 8171	哥伦比亚PSE
	 * 8172	哥伦比亚NEQUI
	 * 8180	南非网银
	 * 8191	缅甸KBZPAY
	 * 8192	缅甸KBZPAY扫码
	 * 8193	缅甸WAVEPAY
	 * 8194	缅甸WAVEPAY扫码
	 * 8200	科特迪瓦网银
	 */
	@NotBlank(message = "payCode不能为空")
	private String payCode;//	String	是	支付类型编码	详细参考文档：[支付类型编码]

	/**
	 * 手机号
	 */
	private String phone;

	/**
	 * 交易结果接收地址
	 */
	@NotBlank(message = "notifyUrl不能为空")
	private String notifyUrl;//是	交易结果接收地址
}
