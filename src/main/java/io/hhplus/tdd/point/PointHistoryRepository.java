package io.hhplus.tdd.point;

import java.util.List;

public interface PointHistoryRepository {
	PointHistory saveUserPoint(long userId, long amount, TransactionType type, long updateMillis);

	List<PointHistory> findAllHistoryByUserId(long userId);
}
