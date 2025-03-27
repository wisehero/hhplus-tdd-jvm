package io.hhplus.tdd.point;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;

import org.springframework.stereotype.Service;

import io.hhplus.tdd.point.lock.UserIdLock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PointService {

	private final PointRepository pointRepository;
	private final PointHistoryRepository pointHistoryRepository;

	public UserPoint findPointById(long id) {
		return pointRepository.findById(id);
	}

	@UserIdLock
	public UserPoint chargePoint(long id, long amount) {
		UserPoint findUserPoint = pointRepository.findById(id);

		UserPoint charged = findUserPoint.charge(amount);
		pointHistoryRepository.saveUserPoint(id, amount, TransactionType.CHARGE, System.currentTimeMillis());
		return pointRepository.saveOrUpdate(charged.id(), charged.point());
	}

	@UserIdLock
	public UserPoint usePoint(long id, long amount) {
		UserPoint findUserPoint = pointRepository.findById(id);

		UserPoint user = findUserPoint.use(amount);
		pointHistoryRepository.saveUserPoint(id, amount, TransactionType.USE, System.currentTimeMillis());

		return pointRepository.saveOrUpdate(user.id(), user.point());
	}

	public List<PointHistory> findPointHistoriesOfUser(long userId) {
		return pointHistoryRepository.findAllHistoryByUserId(userId);
	}
}
