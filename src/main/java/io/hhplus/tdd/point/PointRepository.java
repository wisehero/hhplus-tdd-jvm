package io.hhplus.tdd.point;

public interface PointRepository {
	UserPoint findById(long id);

	UserPoint saveOrUpdate(long id, long amount);
}
