package io.hhplus.tdd.point;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UserPointTest {

	@Test
	@DisplayName("사용자 포인트를 충전한다.")
	void chargeUserPoint() {
		// given
		UserPoint userPoint = UserPoint.empty(1L);
		long chargePoint = 100_000;

		// when
		UserPoint chargedPoint = userPoint.charge(chargePoint);

		// then
		assertThat(chargedPoint.point()).isEqualTo(chargePoint);
	}

	@Test
	@DisplayName("만약 충전 포인트가 최대 한도를 초과하면, 예외가 발생한다.")
	void chargePointOverMaxLimit() {
		// given
		UserPoint userPoint = UserPoint.empty(1L);
		long MAX_CHARGE_POINT = 100_000;
		long chargePoint = 100_001;

		// when then
		assertThatThrownBy(() -> userPoint.charge(chargePoint))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("최대 포인트 한도는 %d 입니다. 입력값: %d".formatted(MAX_CHARGE_POINT, chargePoint));
	}

	@Test
	@DisplayName("만약 충전 포인트가 음수라면, 예외가 발생한다.")
	void ifChargePointUnderZero() {
		// given
		UserPoint userPoint = UserPoint.empty(1L);
		long chargePoint = -1;

		// when then
		assertThatThrownBy(() -> userPoint.charge(chargePoint))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("충전할 포인트는 0 보다 커야 합니다. 입력값: %d".formatted(chargePoint));
	}

	@Test
	@DisplayName("사용자 포인트는 최대 사용 가능 포인트 내에서 사용할 수 있다.")
	void useUserPoint() {
		// given
		UserPoint userPoint = new UserPoint(1L, 20_000, System.currentTimeMillis());
		long usePoint = 10_000;

		// when
		UserPoint usedPoint = userPoint.use(usePoint);

		// then
		assertThat(usedPoint.point()).isEqualTo(10_000);
	}

	@Test
	@DisplayName("만약 사용하고자 하는 포인트가 음수라면, 예외가 발생한다.")
	void ifUsePointUnderZero() {
		// given
		UserPoint userPoint = new UserPoint(1L, 20_000, System.currentTimeMillis());
		long usePoint = -1;

		// when then
		assertThatThrownBy(() -> userPoint.use(usePoint))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("사용할 포인트는 0 보다 커야 합니다. 입력값: %d".formatted(usePoint));
	}

	@Test
	@DisplayName("만약 사용하고자 하는 포인트가 현재 보유한 포인트를 초과하면, 예외가 발생한다.")
	void ifUsePointOverCurrentPoint() {
		// given
		UserPoint userPoint = new UserPoint(1L, 5_000, System.currentTimeMillis());
		long usePoint = 5_001;

		// when then
		assertThatThrownBy(() -> userPoint.use(usePoint))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("갖고 있는 포인트를 초과해서 사용할 수 없습니다. 입력값: %d, 현재 포인트 잔고: %d".formatted(usePoint, userPoint.point()));
	}

	@Test
	@DisplayName("만약 사용하고자 하는 포인트가 최대 사용가능한 포인트를 초과하면, 예외가 발생한다.")
	void ifUsePointOverMaxLimit() {
		// given
		UserPoint userPoint = new UserPoint(1L, 20_000, System.currentTimeMillis());
		long MAX_USE_POINT = 10_000;
		long usePoint = 10_001;

		// when then
		assertThatThrownBy(() -> userPoint.use(usePoint))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("최대 사용 가능한 포인트는 %d입니다. 입력값: %d".formatted(MAX_USE_POINT, usePoint));
	}
}