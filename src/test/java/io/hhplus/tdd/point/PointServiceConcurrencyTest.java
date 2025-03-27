package io.hhplus.tdd.point;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

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
	@DisplayName("0 포인트를 가진 사용자를 대상으로 100 포인트를 충전하는 요청이 20개 동시에 들어오면 포인트는 2_000이 되고 이력도 20개가 남는다.")
	void chargePointConcurrencyTest() throws InterruptedException {

		// given
		long userId = 1L;
		int threadCount = 20;
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
			() -> assertThat(findUserPoint.point()).isEqualTo(FIXED_AMOUNT * threadCount),
			() -> assertThat(allHistoryByUserId.size()).isEqualTo(threadCount)
		);
	}

	@Test
	@DisplayName("2명의 유저를 대상으로 포인트가 0인 유저에게 100 포인트를 충전하는 요청을 각 20번 동시 요청하면 두 유저의 포인트는 각 2_000이고 이력도 20개씩 남는다.")
	void chargePointConcurrencyTestMultiUser() throws InterruptedException {

		// given
		long userId1 = 1L;
		long userId2 = 2L;
		int threadCount = 40;
		pointRepository.saveOrUpdate(userId1, 0L);
		pointRepository.saveOrUpdate(userId2, 0L);

		ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
		CountDownLatch latch = new CountDownLatch(threadCount);

		// when
		for (int i = 0; i < threadCount; i++) {
			long userId = (i % 2 == 0) ? userId1 : userId2;
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
		UserPoint findUser1Point = pointRepository.findById(userId1);
		UserPoint findUser2Point = pointRepository.findById(userId2);
		List<PointHistory> allHistoryByUserId1 = pointHistoryRepository.findAllHistoryByUserId(userId1);
		List<PointHistory> allHistoryByUserId2 = pointHistoryRepository.findAllHistoryByUserId(userId2);

		assertAll(
			() -> assertThat(findUser1Point.point()).isEqualTo(FIXED_AMOUNT * 20),
			() -> assertThat(findUser2Point.point()).isEqualTo(FIXED_AMOUNT * 20),
			() -> assertThat(allHistoryByUserId1.size()).isEqualTo(20),
			() -> assertThat(allHistoryByUserId2.size()).isEqualTo(20)
		);
	}

	@Test
	@DisplayName("90,000 포인트를 가진 사용자를 대상으로 1,000 포인트를 충전하는 요청이 15개 동시에 들어오면 10개는 성공하고 5개는 IllegalStateException 예외가 발생한다.")
	void chargePointConcurrencyExceptionTest() throws InterruptedException {
		// given
		long userId = 1L;
		long initialAmount = 90_000L;
		long chargeAmount = 1_000L;
		pointRepository.saveOrUpdate(userId, initialAmount);
		int threadCount = 15; // 15 * 1,000 = 15,000 + 90,000 = 105,000 5,000 만큼을 초과하고 5번 실패해야한다.

		ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
		CountDownLatch latch = new CountDownLatch(threadCount);
		AtomicInteger successCount = new AtomicInteger(0);
		List<Exception> exceptions = Collections.synchronizedList(new ArrayList<>());

		// when
		for (int i = 0; i < threadCount; i++) {
			executorService.submit(() -> {
				try {
					pointService.chargePoint(userId, chargeAmount);
					successCount.incrementAndGet();
				} catch (Exception e) {
					exceptions.add(e);
				} finally {
					latch.countDown();
				}
			});
		}
		latch.await();
		executorService.shutdown();

		// then
		UserPoint findUserPoint = pointRepository.findById(userId);
		assertAll(
			() -> assertThat(findUserPoint.point()).isEqualTo(100_000L),
			() -> assertThat(successCount.get()).isEqualTo(10),
			() -> assertThat(exceptions.size()).isEqualTo(5)
		);
	}

	@Test
	@DisplayName("10_000 포인트를 가진 사용자를 대상으로 100 포인트를 사용하는 요청이 20개 동시에 들어오면 포인트는 8_000이 되고 이력도 20개가 남는다.")
	void usePointConcurrentcyTest() throws InterruptedException {
		// given
		long userId = 1L;
		long initialAmount = 10_000L;
		int threadCount = 20;
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
			() -> assertThat(findUserPoint.point()).isEqualTo(initialAmount - (FIXED_AMOUNT * threadCount)),
			() -> assertThat(allHistoryByUserId.size()).isEqualTo(threadCount)
		);
	}

	@Test
	@DisplayName("2명의 유저를 대상으로 100 포인트 사용하는 요청을 각 20번 동시 요청하면 두 유저의 포인트는 각 2_000씩 차감되어 8_000이되고 이력도 20개씩 남는다.")
	public void usePointConcurrencyTestMultiUser() throws InterruptedException {

		// given
		long userId1 = 1L;
		long userId2 = 2L;
		int threadCount = 40;
		long initialAmount = 10_000L;
		pointRepository.saveOrUpdate(userId1, initialAmount);
		pointRepository.saveOrUpdate(userId2, initialAmount);

		ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
		CountDownLatch latch = new CountDownLatch(threadCount);

		// when
		for (int i = 0; i < threadCount; i++) {

			long userId = (i % 2 == 0) ? userId1 : userId2;
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
		UserPoint findUser1Point = pointRepository.findById(userId1);
		UserPoint findUser2Point = pointRepository.findById(userId2);
		List<PointHistory> allHistoryByUserId1 = pointHistoryRepository.findAllHistoryByUserId(userId1);
		List<PointHistory> allHistoryByUserId2 = pointHistoryRepository.findAllHistoryByUserId(userId2);

		assertAll(
			() -> assertThat(findUser1Point.point()).isEqualTo(initialAmount - (FIXED_AMOUNT * 20)),
			() -> assertThat(findUser2Point.point()).isEqualTo(initialAmount - (FIXED_AMOUNT * 20)),
			() -> assertThat(allHistoryByUserId1.size()).isEqualTo(20),
			() -> assertThat(allHistoryByUserId2.size()).isEqualTo(20)
		);
	}

	@Test
	@DisplayName("10,000 포인트를 가진 사용자를 대상으로 2,000 포인트를 사용하는 요청이 동시에 10번이 들어오면 5번은 성공해서 포인트가 0원이 되고 5번은 IlleagalArgumentException 예외가 발생한다.")
	public void usePointConcurrencyExceptionTest() throws InterruptedException {
		// given
		long userId = 1L;
		long initialAmount = 10_000L;
		long useAmount = 2_000L;
		pointRepository.saveOrUpdate(userId, initialAmount);
		int threadCount = 10;

		ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
		CountDownLatch latch = new CountDownLatch(threadCount);
		AtomicInteger successCount = new AtomicInteger(0);
		List<Exception> exceptions = Collections.synchronizedList(new ArrayList<>());

		// when
		for (int i = 0; i < threadCount; i++) {
			executorService.submit(() -> {
				try {
					pointService.usePoint(userId, useAmount);
					successCount.incrementAndGet();
				} catch (Exception e) {
					exceptions.add(e);
				} finally {
					latch.countDown();
				}
			});
		}
		latch.await();
		executorService.shutdown();

		// then
		UserPoint findUserPoint = pointRepository.findById(userId);
		assertAll(
			() -> assertThat(findUserPoint.point()).isEqualTo(0L),
			() -> assertThat(successCount.get()).isEqualTo(5),
			() -> assertThat(exceptions.size()).isEqualTo(5)
		);
	}
}
