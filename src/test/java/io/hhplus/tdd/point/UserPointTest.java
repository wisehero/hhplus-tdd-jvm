package io.hhplus.tdd.point;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class UserPointTest {

	@Test
	@DisplayName("포인트가 0인 사용자가 100_000 포인트를 충전하면 100_000 포인트가 정상적으로 충전된다.")
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
	@DisplayName("만약 충전 포인트가 최대 한도를 초과하면, 포인트가 충전되지 않고 IllegalArgumentException 예외가 발생한다.")
	void chargePointOverMaxLimit() {
		// given
		UserPoint userPoint = UserPoint.empty(1L);
		long MAX_CHARGE_POINT = 100_000;
		long chargePoint = 100_001;

		// when then
		assertThatThrownBy(() -> userPoint.charge(chargePoint))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("최대 포인트 한도는 %d 입니다. 입력값: %d".formatted(MAX_CHARGE_POINT, chargePoint));
		assertThat(userPoint.point()).isEqualTo(0);
	}

	@ParameterizedTest
	@ValueSource(longs = {0, -1})
	@DisplayName("만약 충전 포인트가 0이거나 음수라면, 포인트가 충전되지 않고 IllegalArgumentException 예외가 발생한다.")
	void ifChargePointUnderZero(long chargePoint) {
		// given
		UserPoint userPoint = UserPoint.empty(1L);

		// when then
		assertThatThrownBy(() -> userPoint.charge(chargePoint))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("충전할 포인트는 0 보다 커야 합니다. 입력값: %d".formatted(chargePoint));
		assertThat(userPoint.point()).isEqualTo(0);
	}

	@ParameterizedTest
	@ValueSource(longs = {5_000, 10_000})
	@DisplayName("포인트를 사용자가 가진 포인트 범위 내에서 사용되면 그만큼 포인트가 차감된다.")
	void useUserPoint(long usePoint) {
		// given
		UserPoint userPoint = new UserPoint(1L, 10_000, System.currentTimeMillis());

		// when
		UserPoint usedPoint = userPoint.use(usePoint);

		// then
		assertThat(usedPoint.point()).isEqualTo(userPoint.point() - usePoint);
	}

	@ParameterizedTest
	@ValueSource(longs = {0, -1})
	@DisplayName("만약 사용하고자 하는 포인트 0이거나 음수라면, 포인트는 변함없고 IllegalArgumentException 예외가 발생한다.")
	void ifUsePointUnderZero(long usePoint) {
		// given
		long initialPoint = 10_000L;
		UserPoint userPoint = new UserPoint(1L, initialPoint, System.currentTimeMillis());

		// when then
		assertThatThrownBy(() -> userPoint.use(usePoint))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("사용할 포인트는 0 보다 커야 합니다. 입력값: %d".formatted(usePoint));
		assertThat(userPoint.point()).isEqualTo(initialPoint);
	}

	@Test
	@DisplayName("만약 사용하고자 하는 포인트가 현재 보유한 포인트를 초과하면, 포인트는 변함없고 IllegalArgumentException 예외가 발생한다.")
	void ifUsePointOverCurrentPoint() {
		// given
		long initialPoint = 5_000L;
		UserPoint userPoint = new UserPoint(1L, initialPoint, System.currentTimeMillis());
		long usePoint = 5_001L;

		// when then
		assertThatThrownBy(() -> userPoint.use(usePoint))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("갖고 있는 포인트를 초과해서 사용할 수 없습니다. 입력값: %d, 현재 포인트 잔고: %d".formatted(usePoint, userPoint.point()));
		assertThat(userPoint.point()).isEqualTo(initialPoint);
	}

	@Test
	@DisplayName("만약 사용하고자 하는 포인트가 최대 사용가능한 포인트를 초과하면, 포인트는 변함없고 IllegalArgumentException 예외가 발생한다.")
	void ifUsePointOverMaxLimit() {
		// given
		long initialPoint = 20_000L;
		UserPoint userPoint = new UserPoint(1L, initialPoint, System.currentTimeMillis());
		long MAX_USE_POINT = 10_000;
		long usePoint = 10_001;

		// when then
		assertThatThrownBy(() -> userPoint.use(usePoint))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("최대 사용 가능한 포인트는 %d입니다. 입력값: %d".formatted(MAX_USE_POINT, usePoint));
		assertThat(userPoint.point()).isEqualTo(initialPoint);
	}
}