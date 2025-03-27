package io.hhplus.tdd.point;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PointService {

	private final PointRepository pointRepository;
	private final PointHistoryRepository pointHistoryRepository;

	private final ConcurrentHashMap<Long, Lock> userIdLockMap = new ConcurrentHashMap<>();

	public UserPoint findPointById(long id) {
		return pointRepository.findById(id);
	}

	public UserPoint chargePoint(long id, long amount) {

		Lock userIdLock = getUserIdKey(id);

		userIdLock.lock();
		try {
			UserPoint findUserPoint = pointRepository.findById(id);

			UserPoint charged = findUserPoint.charge(amount);
			pointHistoryRepository.saveUserPoint(id, amount, TransactionType.CHARGE, System.currentTimeMillis());
			return pointRepository.saveOrUpdate(charged.id(), charged.point());
		} finally {
			userIdLock.unlock();
		}
	}

	public UserPoint usePoint(long id, long amount) {
		Lock userIdLock = getUserIdKey(id);

		userIdLock.lock();
		try {
			UserPoint findUserPoint = pointRepository.findById(id);

			UserPoint used = findUserPoint.use(amount);
			pointHistoryRepository.saveUserPoint(id, amount, TransactionType.USE, System.currentTimeMillis());

			return pointRepository.saveOrUpdate(used.id(), used.point());
		} finally {
			userIdLock.unlock();
		}
	}

	public List<PointHistory> findPointHistoriesOfUser(long userId) {
		return pointHistoryRepository.findAllHistoryByUserId(userId);
	}

	private Lock getUserIdKey(long userId) {
		return userIdLockMap.computeIfAbsent(userId, id -> new ReentrantLock(true));
	}
}
