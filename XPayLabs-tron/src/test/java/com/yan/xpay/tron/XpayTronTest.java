package com.yan.xpay.tron;

import com.yan.blockchain.pay.tron.service.TronService;
import com.yan.xpay.enums.Chain;
import com.yan.xpay.listener.BlockchainListener;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.tron.trident.proto.Response;

import java.math.BigDecimal;
import java.math.BigInteger;

@Slf4j
@SpringBootTest
public class XpayTronTest {
	@Autowired
	BlockchainListener blockchainListener;
	@Autowired
	TronService tronService;

	public static void main(String[] args) {
		Long energy = TronService.estimateEnergy("TPBsXfpPP39CSAFvXHqJdWmwwNeKauSTay");
		log.info("{}", energy);
		BigDecimal trx = TronService.calculateGas("TPBsXfpPP39CSAFvXHqJdWmwwNeKauSTay", true);
		log.info("{}", trx);
	}

//	@Test
//	public void pay() {
//		Transaction t = new Transaction();
//		t.setDecimals(8);
//		t.setSymbol("TRX");
//		t.setAmount(BigDecimal.ONE);
////		blockchainListener.onPayinPending();
//	}
//	@Test
//	public void test(){
//		String res = HttpRequest.get("https://www.baidu.com").execute().body();
//		log.info("res {}",res);
//	}
//	@Autowired
//	private TronService tronService;
//
//	@Test
//	void testWaitingExecution() {
//		// 记录开始时间
//		long start = System.currentTimeMillis();
//
//		// 模拟20个并发请求（限流10次/秒）
//		IntStream.range(0,  20).parallel().forEach(i -> {
//			try {
//				Response.AccountResourceMessage accountResourceMessage = tronService.getAccountResource( Chain.TRON, "TWMvPngdGHG6vDcE36vr7HVBqiPpdmmkEx");
//				System.out.printf("[%dms]  请求%d完成: %s%n",
//					System.currentTimeMillis()  - start,
//					i,
//					accountResourceMessage.getFreeNetLimit()
//				);
//			} catch (Exception e) {
//				System.err.printf("[%dms]  请求%d失败: %s%n",
//					System.currentTimeMillis()  - start,
//					i,
//					e.getMessage()
//				);
//			}
//		});
//	}

	@Test
	public void testTronExecution() throws InterruptedException {
//		ExecutorService exec = Executors.newFixedThreadPool(20); // 并发线程模拟
//		System.out.println("开始执行");
//		List<CompletableFuture<?>> futures = new ArrayList<>();
//
//		for (int i = 0; i < 50; i++) {
//			final int idx = i;
//			futures.add(tronService.callAsync(() -> {
//				Response.AccountResourceMessage msg =
//					tronService.getAccountResource(Chain.TRON, "TWMv...");
//				System.out.printf(idx + " 请求完成: %s%n", msg.getFreeNetLimit());
//				return msg;
//			}, exec));
//		}
//
//		CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
//
//
//		exec.shutdown();
//		exec.awaitTermination(30, TimeUnit.SECONDS);

		for (int i = 0; i < 50; i++) {
			int finalI = i;
			tronService.executeWithRateLimit(() -> {
				Response.AccountResourceMessage accountResourceMessage = tronService.getAccountResource( Chain.TRON, "TWMvPngdGHG6vDcE36vr7HVBqiPpdmmkEx");
				System.out.printf(finalI +" 请求完成: %s%n",
					accountResourceMessage.getFreeNetLimit());
				return accountResourceMessage;
			});
		}
	}
}
