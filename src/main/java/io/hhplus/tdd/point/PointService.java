package io.hhplus.tdd.point;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PointService {

	private final PointRepository pointRepository;
	private final PointHistoryRepository pointHistoryRepository;

	public UserPoint findPointById(long id) {
		return pointRepository.findById(id);
	}

	public UserPoint chargePoint(long id, long amount) {
		UserPoint findUserPoint = pointRepository.findById(id);

		UserPoint charged = findUserPoint.charge(amount);
		pointHistoryRepository.saveUserPoint(id, amount, TransactionType.CHARGE, System.currentTimeMillis());

		return pointRepository.saveOrUpdate(charged.id(), charged.point());
	}

	public UserPoint usePoint(long id, long amount) {
		UserPoint findUserPoint = pointRepository.findById(id);

		UserPoint used = findUserPoint.use(amount);
		pointHistoryRepository.saveUserPoint(id, amount, TransactionType.USE, System.currentTimeMillis());

		return pointRepository.saveOrUpdate(used.id(), used.point());
	}

	public List<PointHistory> findPointHistoriesOfUser(long userId) {
		return pointHistoryRepository.findAllHistoryByUserId(userId);
	}
}
