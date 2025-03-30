package io.hhplus.tdd.point.lock;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.stereotype.Component;

@Component
public class UserIdLockProvider {

	private final ConcurrentHashMap<Long, ReentrantLock> lockMap = new ConcurrentHashMap<>();

	public Lock getUserIdLock(Long id) {
		return lockMap.computeIfAbsent(id, k -> new ReentrantLock(true));
	}
}
