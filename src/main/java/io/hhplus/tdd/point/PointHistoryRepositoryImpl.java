package io.hhplus.tdd.point;

import java.util.List;

import org.springframework.stereotype.Repository;

import io.hhplus.tdd.database.PointHistoryTable;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class PointHistoryRepositoryImpl implements PointHistoryRepository {

	private final PointHistoryTable pointHistoryTable;

	@Override
	public PointHistory saveUserPoint(long userId, long amount, TransactionType type, long updateMillis) {
		return pointHistoryTable.insert(userId, amount, type, updateMillis);
	}

	@Override
	public List<PointHistory> findAllHistoryByUserId(long userId) {
		return pointHistoryTable.selectAllByUserId(userId);
	}
}
