package io.hhplus.tdd.point;

import java.util.List;

import org.springframework.stereotype.Repository;

import io.hhplus.tdd.database.UserPointTable;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class PointRepositoryImpl implements PointRepository {

	private final UserPointTable userPointTable;

	@Override
	public UserPoint findById(long id) {
		return userPointTable.selectById(id);
	}

	@Override
	public UserPoint saveOrUpdate(long id, long amount) {
		return userPointTable.insertOrUpdate(id, amount);
	}
}
