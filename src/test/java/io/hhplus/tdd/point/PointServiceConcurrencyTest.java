package io.hhplus.tdd.point;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class PointServiceConcurrencyTest {

	@Autowired
	private PointService pointService;

	@Autowired
	PointHistoryRepository pointHistoryRepository;

	@Autowired
	PointRepository pointRepository;

	private final long FIXED_AMOUNT = 100L;

	@Test
	@DisplayName("PointService#chargePoint를 실행해서 한 명의 유저 포인트 충전을 동시에 50개 요청하면 포인트는 5_000이 되고, 이력도 50개가 남지 않는다.")
	public void pointServiceConcurrencyTest() throws InterruptedException {

		// given
		long userId = 1L;
		int threadCount = 50;
		pointRepository.saveOrUpdate(userId, 0L);

		ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
		CountDownLatch latch = new CountDownLatch(threadCount);

		// when
		for (int i = 0; i < threadCount; i++) {
			executorService.submit(() -> {
				try {
					pointService.chargePoint(userId, FIXED_AMOUNT);
				} finally {
					latch.countDown();
				}
			});
		}
		latch.await();
		executorService.shutdown();

		// then
		UserPoint findUserPoint = pointRepository.findById(userId);
		List<PointHistory> allHistoryByUserId = pointHistoryRepository.findAllHistoryByUserId(userId);

		assertAll(
			() -> assertThat(findUserPoint.point()).isEqualTo(FIXED_AMOUNT * 50),
			() -> assertThat(allHistoryByUserId.size()).isEqualTo(threadCount)
		);
	}

	@Test
	@DisplayName("PointService#usePoint를 실행해서 한 명의 유저 포인트 충전을 동시에 50개 요청하면 충전 포인트는 5_000이고 이력은 50개가 생긴다.")
	void pointServiceConcurrencyTestWithSync() throws InterruptedException {
		// given
		long userId = 1L;
		long initialAmount = 10_000L;
		int threadCount = 50;
		pointRepository.saveOrUpdate(userId, initialAmount);
		ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
		CountDownLatch latch = new CountDownLatch(threadCount);

		// when
		for (int i = 0; i < threadCount; i++) {
			executorService.submit(() -> {
				try {
					pointService.usePoint(userId, FIXED_AMOUNT);
				} finally {
					latch.countDown();
				}
			});
		}

		latch.await();
		executorService.shutdown();

		// then
		UserPoint findUserPoint = pointRepository.findById(userId);
		List<PointHistory> allHistoryByUserId = pointHistoryRepository.findAllHistoryByUserId(userId);
		assertAll(
			() -> assertThat(findUserPoint.point()).isEqualTo(initialAmount - (FIXED_AMOUNT * 50)),
			() -> assertThat(allHistoryByUserId.size()).isEqualTo(threadCount)
		);
	}
}
