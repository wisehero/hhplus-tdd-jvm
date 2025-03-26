package io.hhplus.tdd.point;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/point")
@RequiredArgsConstructor
@Validated
public class PointController {

	private static final Logger log = LoggerFactory.getLogger(PointController.class);
	private final PointService pointService;

	/**
	 * TODO - 특정 유저의 포인트를 조회하는 기능을 작성해주세요.
	 */
	@GetMapping(value = "{id}", produces = "application/json; charset=utf-8")
	public UserPoint point(
		@PathVariable @Positive(message = "사용자 id는 0 보다 큰 정수여야 합니다.")
		long id
	) {
		return pointService.findPointById(id);
	}

	/**
	 * TODO - 특정 유저의 포인트 충전/이용 내역을 조회하는 기능을 작성해주세요.
	 */
	@GetMapping("{id}/histories")
	public List<PointHistory> history(
		@PathVariable @Positive(message = "사용자 id는 0 보다 큰 정수여야 합니다.")
		long id
	) {
		return pointService.findPointHistoriesOfUser(id);
	}

	/**
	 * TODO - 특정 유저의 포인트를 충전하는 기능을 작성해주세요.
	 */
	@PatchMapping("{id}/charge")
	public UserPoint charge(
		@PathVariable @Positive(message = "사용자 id는 0 보다 큰 정수여야 합니다.")
		long id,
		@RequestBody @Positive(message = "충전할 포인트는 0 보다 큰 정수여야 합니다.")
		long amount
	) {
		return pointService.chargePoint(id, amount);
	}

	/**
	 * TODO - 특정 유저의 포인트를 사용하는 기능을 작성해주세요.
	 */
	@PatchMapping("{id}/use")
	public UserPoint use(
		@PathVariable @Positive(message = "사용자 id는 0 보다 큰 정수여야 합니다.")
		long id,
		@RequestBody @Positive(message = "사용할 포인트는 0 보다 큰 정수여야 합니다.")
		long amount
	) {
		return pointService.usePoint(id, amount);
	}
}
