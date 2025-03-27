package io.hhplus.tdd.point.lock;

import java.util.concurrent.locks.Lock;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Aspect
@Component
@RequiredArgsConstructor
public class UserIdLockAspect {

	private final UserIdLockProvider userIdLockProvider;

	@Around("@annotation(UserIdLock)")
	public Object applyLock(ProceedingJoinPoint joinPoint) throws Throwable {
		Object[] args = joinPoint.getArgs();

		// 첫 번째 파라미터인 id를 Lock의 Key로 사용한다. -> 너무 제한적인 사용의 어노테이션 같기는 하다.
		Long key = (Long)args[0];
		Lock userIdLock = userIdLockProvider.getUserIdLock(key);
		userIdLock.lock();
		try {
			return joinPoint.proceed(args);
		} finally {
			userIdLock.unlock();
		}

	}
}
