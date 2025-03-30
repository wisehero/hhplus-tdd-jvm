package io.hhplus.tdd.point;

public record UserPoint(
	long id,
	long point,
	long updateMillis
) {

	private static final long MAX_CHARGE_POINT = 100_000; // 포인트 최대 한도는 10만이다.
	private static final long MAX_USE_POINT = 10_000; // 포인트는 한 번에 1만 까지만 사용 가능

	public static UserPoint empty(long id) {
		return new UserPoint(id, 0, System.currentTimeMillis());
	}

	public UserPoint {
		if (point < 0) {
			throw new IllegalArgumentException("포인트는 0 보다 같거나 커야 합니다. 입력값: %d".formatted(point));
		}
		if (point > MAX_CHARGE_POINT) {
			throw new IllegalArgumentException("포인트 최대 한도는 %d 입니다. 입력값: %d".formatted(MAX_CHARGE_POINT, point));
		}
	}

	public UserPoint charge(long chargePoint) {
		if (chargePoint <= 0) {
			throw new IllegalArgumentException("충전할 포인트는 0 보다 커야 합니다. 입력값: %d".formatted(chargePoint));
		}
		long newPoint = point + chargePoint;

		if (newPoint > MAX_CHARGE_POINT) {
			throw new IllegalArgumentException(
				"최대 포인트 한도는 %d 입니다. 입력값: %d".formatted(MAX_CHARGE_POINT, chargePoint));
		}

		return new UserPoint(id, newPoint, System.currentTimeMillis());
	}

	public UserPoint use(long usePoint) {
		if (usePoint <= 0) {
			throw new IllegalArgumentException("사용할 포인트는 0 보다 커야 합니다. 입력값: %d".formatted(usePoint));
		}

		if (usePoint > MAX_USE_POINT) {
			throw new IllegalArgumentException("최대 사용 가능한 포인트는 %d입니다. 입력값: %d".formatted(MAX_USE_POINT, usePoint));
		}

		long newPoint = point - usePoint;
		if (newPoint < 0) {
			throw new IllegalArgumentException(
				"갖고 있는 포인트를 초과해서 사용할 수 없습니다. 입력값: %d, 현재 포인트 잔고: %d".formatted(usePoint, point));
		}

		return new UserPoint(id, newPoint, System.currentTimeMillis());
	}
}
