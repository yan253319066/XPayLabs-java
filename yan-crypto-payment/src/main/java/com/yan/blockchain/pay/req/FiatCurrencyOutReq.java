package com.yan.blockchain.pay.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FiatCurrencyOutReq {
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
	 * 收款银行编号
	 * 孟加拉银行编码   银行码码 银行名称   nagad NAGAD钱包  bkash BKASH钱包
	 *
	 *
	 * 巴基斯坦银行编码
	 * 银行码码	银行名称
	 * JAZZCASH	JAZZCASH钱包
	 * EASYPAISA	EASYPAISA钱包
	 *
	 *
	 * 菲律宾银行编码
	 * 银行码码	银行名称
	 * GCASH	gcash电子钱包
	 * MAYA	maya电子钱包
	 * GRABPAY	GRABPAY
	 *
	 *
	 * 新加坡银行编码
	 * 银行码码	银行名称
	 * PNSG	Paynow Singapore
	 * DBS	DBS Bank Limited
	 * UOB	United Overseas Bank
	 * OCBC	Oversea-Chinese Banking Corporation
	 * SCIMB	CIMB Bank Berhad
	 * SHSBC	HSBC Singapore
	 * SSCB	Standard Chartered Bank
	 * SMBB	Maybank
	 * RHBSG	RHB Bank Berhad
	 * CITISG	Citibank Berhad
	 * ICBCSG	Industrial and commercial bank of China
	 * ANBK	ANEXT BANK PTE LTD
	 * MPP	Match Move Pay Pte Ltd
	 *
	 *
	 * 泰国银行编码
	 * 银行码码	银行名称
	 * THB002	Bangkok Bank Public Company Ltd.
	 * THB003	Kasikornbank Public Company Limited ( KBank)
	 * THB004	Krung Thai Bank Public Company Ltd. (KTB)
	 * THB005	TMB Thanachart Bank Public Company Limited
	 * THB006	Siam Commercial Bank Public Company Ltd. (SCB)
	 * THB009	CIMB Thai Bank Public Company Limited
	 * THB011	Bank of Ayudhya / Krungsri
	 * THB012	Government Savings Bank (GSB)
	 * THB013	Government Housing Bank (GHB)
	 * THB014	Bank for Agriculture and Agricultural Cooperatives
	 * THB017	Kiatnakin Phatra Bank Public Company Limited (KKP)
	 * THB019	Land and Houses Bank Public Company Limited
	 *
	 *
	 * 尼日利亚银行编码
	 * 银行码码	银行名称
	 * NGN001	Opay (PAYCOM)
	 * NGN002	PalmPay
	 * NGN003	Access Bank
	 * NGN004	Ecobank Nigeria
	 * NGN005	Enterprise Bank
	 * NGN006	Fidelity Bank
	 * NGN007	First Bank of Nigeria
	 * NGN008	First City Monument Bank
	 * NGN009	Globus Bank
	 * NGN010	Jaiz Bank
	 * NGN011	Keystone Bank
	 * NGN012	Kuda Bank
	 * NGN013	One Finance
	 * NGN014	Parallex Bank
	 * NGN015	Polaris Bank
	 * NGN016	Providus Bank
	 * NGN017	Stanbic IBTC Bank
	 * NGN018	Standard Chartered Bank
	 * NGN019	Sterling Bank
	 * NGN020	Suntrust Bank
	 * NGN021	Sparkle Microfinance Bank
	 * NGN022	TAJ Bank
	 * NGN023	Union Bank of Nigeria
	 * NGN024	United Bank For Africa
	 * NGN025	Unity Bank
	 * NGN026	VFD Microfinance Bank
	 * NGN027	Wema Bank
	 * NGN028	Zenith Bank
	 * NGN029	Moniepoint Microfinance Bank
	 *
	 *
	 * 墨西哥银行编码
	 * 银行码码	银行名称
	 * 2001	BANXICO
	 * 37006	BANCOMEXT
	 * 37009	BANOBRAS
	 * 37019	BANJERCITO
	 * 37135	NAFIN
	 * 37166	BANSEFI
	 * 37168	HIPOTECARIA FED
	 * 40002	BANAMEX
	 * 40012	BBVA MEXICO
	 * 40014	SANTANDER
	 * 40021	HSBC
	 * 40030	BAJIO
	 * 40036	INBURSA
	 * 40042	MIFEL
	 * 40044	SCOTIABANK
	 * 40058	BANREGIO
	 * 40059	INVEX
	 * 40060	BANSI
	 * 40062	AFIRME
	 * 40072	BANORTE
	 * 40106	BANK OF AMERICA
	 * 40108	MUFG
	 * 40110	JP MORGAN
	 * 40112	BMONEX
	 * 40113	VE POR MAS
	 * 40126	CREDIT SUISSE
	 * 40127	AZTECA
	 * 40128	AUTOFIN
	 * 40129	BARCLAYS
	 * 40130	COMPARTAMOS
	 * 40132	MULTIVA BANCO
	 * 40133	ACTINVER
	 * 40136	INTERCAM BANCO
	 * 40137	BANCOPPEL
	 * 40138	ABC CAPITAL
	 * 40140	CONSUBANCO
	 * 40141	VOLKSWAGEN
	 * 40143	CIBANCO
	 * 40145	BBASE
	 * 40147	BANKAOOL
	 * 40148	PAGATODO.
	 * 40150	INMOBILIARIO
	 * 40151	DONDE.
	 * 40152	BANCREA
	 * 40154	BANCO FINTERRA
	 * 40155	ICBC
	 * 40156	SABADELL
	 * 40157	SHINHAN
	 * 40158	MIZUHO BANK
	 * 40159	BANK OF CHINA
	 * 40160	BANCO S3
	 * 90600	MONEXCB
	 * 90601	GBM
	 * 90602	MASARI
	 * 90605	VALUE
	 * 90608	VECTOR
	 * 90613	MULTIVA CBOLSA
	 * 90616	FINAMEX
	 * 90617	VALMEX
	 * 90620	PROFUTURO
	 * 90630	CB INTERCAM
	 * 90631	CI BOLSA
	 * 90634	FINCOMUN
	 * 90638	NU MEXICO
	 * 90642	REFORMA
	 * 90646	STP
	 * 90648	TACTIV CB
	 * 90652	CREDICAPITAL
	 * 90653	KUSPIT
	 * 90656	UNAGRA
	 * 90659	ASP INTEGRA OPC
	 * 90670	LIBERTAD
	 * 90677	CAJA POP MEXICA
	 * 90680	CRISTOBAL COLON
	 * 90683	CAJA TELEFONIST
	 * 90684	TRANSFER
	 * 90685	FONDO (FIRA)
	 * 90686	INVERCAP
	 * 90689	FOMPED
	 * 90703	TESORED
	 * 90706	ARCUS
	 * 90710	NVIO
	 * 90902	INDEVAL
	 * 90903	CoDi Valida
	 * 90728	SPIN BY OXXO
	 * 90722	Mercado Pago W
	 *
	 *
	 * 越南银行编码
	 * 银行编码	银行名称
	 * MOMO	MoMo
	 * ZALO	Zalo
	 * ABB	ABBANK
	 * ACB	ACB
	 * BAB	BacABank
	 * BIDV	BIDV
	 * BVB	BaoVietBank
	 * CBB	CBBank
	 * CIMB	CIMB
	 * DBS	DBSBank
	 * DOB	DongABank
	 * EIB	Eximbank
	 * GPB	GPBank
	 * HDB	HDBank
	 * HLBVN	HongLeong
	 * HSBC	HSBC
	 * IBKHCM	IBKHCM
	 * ICB	VietinBank
	 * IVB	IndovinaBank
	 * KLB	KienLongBank
	 * MB	MBBank
	 * MSB	MSB
	 * NAB	NamABank
	 * NCB	NCB
	 * NHBHN	Nonghyup
	 * OCB	OCB
	 * Oceanbank	Oceanbank
	 * PBVN	PublicBank
	 * PGB	PGBank
	 * PVCB	PVcomBank
	 * SCB	SCBank
	 * SCVN	StandardChartered
	 * SEAB	SeABank
	 * SGICB	SaigonBank
	 * SHB	SHB
	 * STB	Sacombank
	 * SHBVN	ShinhanBank
	 * TCB	Techcombank
	 * TPB	TPBank
	 * UOB	UnitedOverseas
	 * VAB	VietABank
	 * VBA	Agribank
	 * VCB	Vietcombank
	 * VCCB	VietCapitalBank
	 * VIB	VIB
	 * VIETBANK	VietBank
	 * VPB	VPBank
	 * VRB	VRB
	 * WVN	Woori
	 * KBHN	KookminHN
	 * COOPBANK	COOPBANK
	 * CAKE	CAKE
	 * Ubank	Ubank
	 * KBank	KBank
	 * TIMO	Timo
	 * CITIBANK	Citibank
	 * VBSP	VBSP
	 * LPB	LPBank
	 *
	 *
	 * 秘鲁银行编码
	 * 银行编码	银行名称
	 * 001	Banco Continental
	 * 002	Banco de Credito
	 * 003	Banco de Comercio
	 * 004	Scotiabank
	 * 005	Banco Interamericano de Finanzas (BanBif)
	 * 006	Interbank
	 * 007	Banco Pichincha
	 * 008	Citibank
	 * 009	Banco GNB
	 * 010	Banco Santander
	 * 011	Banco Azteca
	 * 012	Banco Cencosud
	 * 013	ICBC PERU BANK
	 * 014	Banco de la Nación
	 * 015	Caja Cusco
	 * 016	Caja Huancayo
	 * 017	Caja Maynas
	 * 018	Caja Metropolitana
	 * 019	Caja Municipal Ica
	 * 020	Caja Sullana
	 * 021	Caja Tacna
	 * 022	Caja Trujillo
	 *
	 *
	 * 韩国银行编码
	 * 银行编码	银行名称	英文名称
	 * 001	한국은행	BANK OF KOREA
	 * 002	산업은행	KOREA DEVELOPMENT BANK
	 * 003	기업은행	INDUSTRIAL BANK OF KOREA
	 * 004	국민은행	KOOKMIN BANK
	 * 005	외환은행	KOREA EXCHANGE BANK
	 * 007	수협	NATIONAL FEDERATION OF FISHERIES COOPERATIVES
	 * 008	한국수출입은행	KOREA EXIM BANK
	 * 011	농협은행	AGRICULTURAL COOPERATION UNIT
	 * 012	단위농협	AGRICULTURAL COOPERATION UNIT
	 * 020	우리은행	WOORI BANK
	 * 023	SC제일은행	STANDARD CHARTERED FIRST BANK KOREA
	 * 027	한국씨티은행	CITI BANK KOREA
	 * 031	대구은행	DAEGU BANK
	 * 032	부산은행	PUSAN BANK
	 * 034	광주은행	KWANGJU BANK
	 * 035	제주은행	JEJU BANK
	 * 037	전북은행	JEONBUK BANK
	 * 039	경남은행	KYONGNAM BANK
	 * 045	새마을금고	KOREAN FEDERATION OF COMMUNITY CREDIT COOPERATIVES
	 * 048	신협	NATIONAL CREDIT UNION FEDERATION OF KOREA
	 * 050	상호저축은행	SAVINGS BANK
	 * 053	구)씨티은행	CITI BANK
	 * 064	산림조합중앙회	NATIONAL FORESTRY COOPERATIVES FEDERATION
	 * 071	우체국	KOREA POST OFFICE
	 * 081	하나은행	KOREA EXCHANGE BANK
	 * 088	신한은행(신한,조흥통합)	SHINHAN BANK
	 * 089	K뱅크	K BANK
	 * 090	카카오뱅크	KAKAO BANK
	 * 092	토스뱅크	TOSS BANK
	 *
	 *
	 * 马来西亚一类银行编码
	 * bank code	银行名称
	 * MAYBANK	MAYBANK
	 * CIMB	CIMB
	 * HLB	HLB
	 * PUBLIC BANK	PUBLIC BANK
	 * RHB	RHB
	 * AMBANK	AMBANK
	 * Standard Chartered Bank	Standard Chartered Bank
	 * BANK ISLAM	BANK ISLAM
	 * BSN	BSN
	 * AI-RAHJI BANK	AI-RAHJI BANK
	 * UOB	UOB
	 * HSBC	HSBC
	 * ALIIANCE BANK	ALIIANCE BANK
	 * CITI BANK	CITI BANK
	 * AFFIN BANK	AFFIN BANK
	 * Bank Rakyat	Bank Rakyat
	 * TOUCH N GO	TOUCH N GO
	 * GXBANK	GXBANK
	 *
	 *
	 * 印尼银行编码
	 * 银行编码	银行名称
	 * BCA	Bank BCA
	 * BRI	Bank BRI
	 * MANDIRI	BANK MANDIRI
	 * BNI	BANK BNI 46
	 * CIMB	BANK CIMB NIAGA
	 * PERMATA	BANK PERMATA
	 * BJB	Bank BJB
	 * DANAMON	BANK DANAMON INDONESIA
	 * BTN	Bank BTN
	 * MAYBANK	BANK MAYBANK INDONESIA
	 * SINARMAS	BANK SINARMAS
	 * PANIN	BANK PANIN
	 * BNI_SYR	BANK BNI SYARIAH
	 * MANDIRI_SYR	BANK SYARIAH MANDIRI
	 * DKI	BPD DKI JAKARTA
	 * MEGA	BANK MEGA
	 * BSI	BSI (Bank Syariah Indonesia)
	 * BTPN	Bank BTPN
	 * BRI_SYR	BANK BRI SYARIAH
	 * MUAMALAT	BANK MUAMALAT INDONESIA
	 * OCBC	BANK OCBC NISP
	 * OVO	OVO
	 * SHOPEEPAY	SHOPEEPAY
	 * DANA	DANA
	 * GOPAY	GOPAY
	 *
	 *
	 * 哥伦比亚银行编码
	 * 银行编码
	 * BANCAMIA S.A.
	 * BANCO AGRARIO
	 * BANCO AV VILLAS
	 * BANCO BBVA COLOMBIA S.A.
	 * BANCO CAJA SOCIAL
	 * BANCO COOPERATIVO COOPCENTRAL
	 * BANCO CREDIFINANCIERA
	 * BANCO DAVIVIENDA
	 * BANCO DE BOGOTA
	 * BANCO DE OCCIDENTE
	 * BANCO FALABELLA
	 * BANCO GNB SUDAMERIS
	 * BANCO ITAU
	 * BANCO PICHINCHA S.A.
	 * BANCO POPULAR
	 * BANCO SANTANDER COLOMBIA
	 * BANCO SERFINANZA
	 * BANCO UNION antes GIROS
	 * BANCOLOMBIA
	 * BANCOOMEVA S.A.
	 * CFA COOPERATIVA FINANCIERA
	 * CITIBANK
	 * COLTEFINANCIERA
	 * CONFIAR COOPERATIVA FINANCIERA
	 * COOFINEP COOPERATIVA FINANCIERA
	 * COTRAFA
	 * DAVIPLATA
	 * IRIS
	 * LULO BANK
	 * MOVII
	 * NEQUI
	 * SCOTIABANK COLPATRIA
	 *
	 * 南非银行编码
	 * 需要提交的代付银行编号	英文银行名称
	 * ABSA	Absa
	 * NEDBANK	NedBank
	 * CAPITEC	Capitec
	 * STANDARD	Standard
	 * FNBANK	Fnb
	 * AFRICANB	African Bank
	 * BIDVESTB	Bidvest Bank
	 * DISCOVERY	Discovery
	 * FIRSTRANDB	FirstRand Bank
	 * GRIDRODB	Grindrod Bank
	 * IMPERIALB	Imperial Bank
	 * INVESTECB	Investec Bank
	 * SASFINB	Sasfin Bank
	 * UBANK	Ubank
	 * TYMEBANK	TymeBank
	 * MERBANK	Mercantile Bank
	 * ALBARAKA	Albaraka Bank
	 * HBZBANK	HBZ Bank
	 * HABIBO	Habib Overseas Bank
	 * WESBANK	Wesbank
	 * RANDMER	Rand Merchant Bank
	 * BOATHENS	Bank of Athens
	 *
	 *
	 * 马来西亚二类银行
	 * 银行代码	银行名称
	 * CIMB	CIMB BANK
	 * CITI	Citibank Berhad
	 * HSBC	HSBC Bank Malaysia Berhad
	 * ICBC	Industrial And Commercial Bank Of China
	 * SCB	Standard Chartered Bank Malaysia
	 * SMBC	Sumitomo Mitsui Banking Corporation (Malaysia) Berhad
	 * ABB	Affin Bank Berhad (ABB)
	 * ABMB	Alliance Bank Malaysia Berhad
	 * AEON	AEON Bank
	 * AFFINISLAMIC	AFFIN ISLAMIC BANK BERHAD
	 * AGRO	Agrobank (AGRO)
	 * ALLIANCEISLAMIC	ALLIANCE ISLAMIC BANK (M) BERHAD
	 * AMBG	Ambank Berhad
	 * AMISLAMIC	AMISLAMIC BANK BERHAD (AMISLAMIC)
	 * ARB	Al Rajhi Corporation (Malaysia) Berhad
	 * BBB	Bangkok Bank Berhad
	 * BIGPAY	BigPay
	 * BIMB	BANK ISLAM MALAYSIA BERHAD
	 * BKRM	Bank Kerjasama Rakyat Malaysia Berhad
	 * BKRMMYKL	BANK KERJASAMA RAKYAT (M) BERHAD
	 * BMMB	Bank Muamalat Malaysia Berhad
	 * BNPP	BNP Paribas Malaysia Berhad
	 * BOCM	Bank Of China (Malaysia) Berhad
	 * BOFA	Bank Of America (Malaysia) Berhad
	 * BOOST	Boost Bank
	 * BOSTMYNB	Boost eWallet
	 * BSN	Bank Simpanan Nasional Berhad
	 * BSNBSN	BANK SIMPANAN NASIONAL - SPI
	 * CIMBISLAMIC	CIMB ISLAMIC BANK BERHAD
	 * CITISPI	CITIBANK - SPI (CITISPI)
	 * DBB	Deutsche Bank (Malaysia) Berhad
	 * DBBSPI	Deutsche Bank (Malaysia) Berhad - SPI (DBBSPI)
	 * FCSD	Finexus Cards Sdn Bhd (Non-Bank) (FCSD)
	 * GXBANK	GX Bank (GXBANK)
	 * HLB	Hong Leong Bank Berhad
	 * HLISLAMIC	HONG LEONG ISLAMIC BANK BERHAD
	 * HSBCAMANAH	HSBC AMANAH (M) BERHAD
	 * JPMC	JP Morgan Chase Bank Berhad
	 * KAFBMYK2	KAF Digital Bank
	 * KFH	Kuwait Finance House (Malaysia) Berhad
	 * MAYBANKISLAMIC	MAYBANK ISLAMIC BERHAD
	 * MBB	Maybank Berhad
	 * MBSB	MBSB Bank Berhad
	 * MCBM	Mizuho Bank (Malaysia) Berhad
	 * MERCHANTRADE	MERCHANTRADE
	 * MUFG	MUFG Bank (Malaysia) Berhad
	 * OCBC	OCBC Bank (Malaysia) Berhad
	 * OCBCALAMIN	OCBC AL-AMIN BANK BERHAD
	 * PBB	Public Bank Berhad
	 * PCBC	China Construction Bank (Malaysia) Berhad
	 * PUBLICISLAMIC	PUBLIC ISLAMIC BANK BERHAD (PUBLICISLAMIC)
	 * RHB	RHB Bank Berhad (RHB)
	 * SCSAADIQ	STANDARD CHARTERED SAADIQ BHD (SCSAADIQ)
	 * SHOPEE	Shopee
	 * TNGD	Touch n Go eWallet
	 * UOB	United Overseas Bank Berha
	 *
	 * 印度
	 * IFSC编码
	 */
	@NotBlank(message = "bankCode不能为空")
	private String bankCode;//	String	是	收款银行编号	参考文档：[银行编码]

	/**
	 * 收款人手机号
	 */
	private String phone;//	是	收款人手机号	无特殊说明可填固定手机号

	/**
	 * 收款人姓名
	 */
	@NotBlank(message = "name不能为空")
	private String name;// 是	收款人姓名	tom （一般为字母）
	/**
	 * 收款人邮箱
	 */
	private String email;//是	收款人邮箱	无特殊说明可填固定邮箱

	/**
	 *收款人账号
	 */
	@NotBlank(message = "account不能为空")
	private String account;//是	收款人账号	银行账号

	/**
	 *交易结果接收地址
	 */
	@NotBlank(message = "notifyUrl不能为空")
	private String notifyUrl;//是	交易结果接收地址
}
