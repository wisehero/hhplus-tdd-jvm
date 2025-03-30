package io.hhplus.tdd.point;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class PointServiceIntegrationTest {

	@Autowired
	private PointService pointService;

	@Autowired
	private PointRepository pointRepository;

	@Autowired
	private PointHistoryRepository pointHistoryRepository;

	private final long FIXED_TIME = System.currentTimeMillis();

	@Test
	@DisplayName("유저 ID를 입력받으면 해당 유저의 포인트를 조회할 수 있다.")
	void shouldReturnUserPointWhenUserIdIsGiven() {
		// given
		long userId = 1L;
		UserPoint userPoint = new UserPoint(userId, 1000L, FIXED_TIME);
		pointRepository.saveOrUpdate(userPoint.id(), userPoint.point());

		// when
		UserPoint result = pointService.findPointById(userId);

		// then
		assertThat(result.point()).isEqualTo(userPoint.point());
	}

	@Test
	@DisplayName("만약 존재하지 않는 유저 ID를 입력받으면, 포인트가 0인 새로운 유저를 생성한다.")
	void shouldCreateNewUserPointWhenUserIdIsNotExist() {
		// given
		long userId = 10000L;

		// when
		UserPoint result = pointService.findPointById(userId);

		// then
		assertThat(result.point()).isEqualTo(0);
	}

	@Test
	@DisplayName("사용자가 포인트를 충전했다면, 포인트가 증가하고 이력도 생긴 것을 확인할 수 있다.")
	void shouldReturnChargedPointWhenUserChargePoint() {
		// given
		long userId = 1L;
		UserPoint userPoint = new UserPoint(userId, 1000L, FIXED_TIME);
		pointRepository.saveOrUpdate(userPoint.id(), userPoint.point());
		long chargePoint = 1000L;

		// when
		UserPoint result = pointService.chargePoint(userId, chargePoint);

		// then
		List<PointHistory> allHistoryByUserId = pointHistoryRepository.findAllHistoryByUserId(userId);
		assertAll(
			() -> assertThat(result.point()).isEqualTo(userPoint.point() + chargePoint),
			() -> assertThat(allHistoryByUserId.size()).isEqualTo(1),
			() -> assertThat(allHistoryByUserId.get(0).type()).isEqualTo(TransactionType.CHARGE)
		);

	}

	@Test
	@DisplayName("사용자가 포인트 충전을 했을 때, 포인트가 최대 한도(10만)를 넘는다면 IllegalArgumentException이 발생하고 이력도 남지 않는다.")
	void shouldThrowIllegalArgumentExceptionWhenUserChargePointIsOverMaxLimit() {
		// given
		long userId = 1L;
		long chargePoint = 10_001L;
		UserPoint userPoint = new UserPoint(userId, 90_000L, FIXED_TIME);
		pointRepository.saveOrUpdate(userPoint.id(), userPoint.point());

		// when
		assertThatThrownBy(() -> pointService.chargePoint(userId, chargePoint))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("최대 포인트 한도는 100000 입니다. 입력값: %d".formatted(chargePoint));

		// then
		List<PointHistory> allHistoryByUserId = pointHistoryRepository.findAllHistoryByUserId(userId);
		assertThat(allHistoryByUserId.size()).isEqualTo(0);
	}

	@Test
	@DisplayName("사용자가 포인트를 사용했다면, 포인트가 감소된 걸 확인할 수 있고 이력도 생긴 것을 확인할 수 있다.")
	void shouldReturnUsedPointWhenUserUsePoint() {
		// given
		long userId = 1L;
		UserPoint userPoint = new UserPoint(userId, 1000L, FIXED_TIME);
		pointRepository.saveOrUpdate(userPoint.id(), userPoint.point());
		long usePoint = 500L;

		// when
		UserPoint result = pointService.usePoint(userId, usePoint);

		// then
		List<PointHistory> allHistoryByUserId = pointHistoryRepository.findAllHistoryByUserId(userId);
		assertAll(
			() -> assertThat(result.point()).isEqualTo(userPoint.point() - usePoint),
			() -> assertThat(allHistoryByUserId.size()).isEqualTo(1),
			() -> assertThat(allHistoryByUserId.get(0).type()).isEqualTo(TransactionType.USE)
		);
	}

	@Test
	@DisplayName("사용자가 포인트 사용을 했을 때, 갖고있는 포인트 이상을 사용하면 IllegalArgumentException이 발생하고 이력도 남지 않는다.")
	void shouldThrowIllegalArgumentExceptionWhenUserUsePointIsOverMaxLimit() {
		// given
		long userId = 1L;
		long usePoint = 1001L;
		UserPoint userPoint = new UserPoint(userId, 1000L, FIXED_TIME);
		pointRepository.saveOrUpdate(userPoint.id(), userPoint.point());

		// when
		assertThatThrownBy(() -> pointService.usePoint(userId, usePoint))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("갖고 있는 포인트를 초과해서 사용할 수 없습니다. 입력값: %d, 현재 포인트 잔고: %d".formatted(usePoint, userPoint.point()));

		// then
		List<PointHistory> allHistoryByUserId = pointHistoryRepository.findAllHistoryByUserId(userId);
		assertThat(allHistoryByUserId.size()).isEqualTo(0);
	}
}
