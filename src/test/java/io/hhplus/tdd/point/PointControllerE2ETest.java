package io.hhplus.tdd.point;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.hhplus.tdd.ErrorResponse;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class PointControllerE2ETest {

	@LocalServerPort
	private int port;

	@Autowired
	PointRepository pointRepository;

	@Autowired
	PointHistoryRepository pointHistoryRepository;

	RestClient restClient;

	private final long FIXED_TIME = System.currentTimeMillis();

	@BeforeEach
	void setUp() {
		String baseUrl = "http://localhost:" + port;
		restClient = RestClient.builder()
			.baseUrl(baseUrl)
			.build();

	}

	@Test
	@DisplayName("GET /point/{id} API 요청이 성공한다면 200 OK와 UserPoint를 반환한다.")
	void findPointById() {
		// given
		long userId = 1L;
		String path = "/point/" + userId;

		ResponseEntity<UserPoint> response = restClient.get()
			.uri(path)
			.retrieve()
			.toEntity(UserPoint.class);
		// then
		assertAll(
			() -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
			() -> assertThat(response.getBody())
				.isNotNull()
				.extracting(UserPoint::id, UserPoint::point)
				.containsExactly(userId, 0L)
		);
	}

	@ParameterizedTest
	@ValueSource(longs = {0, -1})
	@DisplayName("GET /point/{id} API 요청 시 id가 0 이거나 음수라면 400 Bad Request와 ErrorResponse를 반환한다.")
	void findPointByIdFail(long invalidId) {
		// given
		String path = "/point/" + invalidId;

		// when
		try {
			restClient.get()
				.uri(path)
				.retrieve()
				.toEntity(UserPoint.class);
		} catch (HttpClientErrorException e) {
			// then
			assertAll(
				() -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST),
				() -> assertThat(e.getResponseBodyAs(ErrorResponse.class))
					.isNotNull()
					.extracting(ErrorResponse::code, ErrorResponse::message, ErrorResponse::validation)
					.containsExactly("400", "유효성 검사 실패",
						Map.of(
							"point.id", "사용자 id는 0 보다 큰 정수여야 합니다."
						))
			);
		}

	}

	@Test
	@DisplayName("GET /point/{id} API 요청 시 id가 정수형이 아니라면 400 Bad Request와 ErrorResponse를 반환한다.")
	void findPointByIdFailWhenInputTypeIsString() {
		// given
		String userId = "asdfasdf";
		String path = "/point/" + userId;

		// when
		try {
			restClient.get()
				.uri(path)
				.retrieve()
				.body(UserPoint.class);
		} catch (HttpClientErrorException e) {
			// then
			assertAll(
				() -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST),
				() -> assertThat(e.getResponseBodyAs(ErrorResponse.class))
					.isNotNull()
					.extracting(ErrorResponse::code, ErrorResponse::message)
					.containsExactly("400", "올바르지 않은 타입입니다.")
			);
		}
	}

	@Test
	@DisplayName("GET /point/{id}/history API 요청이 성공한다면 200 OK와 List<PointHistory>를 반환한다.")
	void findPointHistoryById() {
		// given
		long userId = 1L;
		String path = "/point/" + userId + "/histories";
		pointHistoryRepository.saveUserPoint(1L, 1000L, TransactionType.CHARGE, FIXED_TIME);
		pointHistoryRepository.saveUserPoint(1L, 1000L, TransactionType.USE, FIXED_TIME);
		List<PointHistory> allHistoryByUserId = pointHistoryRepository.findAllHistoryByUserId(userId);

		// when
		ResponseEntity<List<PointHistory>> response = restClient.get()
			.uri(path)
			.retrieve()
			.toEntity(new ParameterizedTypeReference<>() {
			});

		// then
		assertAll(
			() -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
			() -> assertThat(response.getBody())
				.asList()
				.hasSize(allHistoryByUserId.size())
		);
	}

	@ParameterizedTest
	@ValueSource(longs = {0, -1})
	@DisplayName("GET /point/{id}/history API 요청 시 id가 0 이거나 음수라면 400 Bad Request와 ErrorResponse를 반환한다.")
	void findPointHistoryByIdFail(long invalidId) {
		// given
		String path = "/point/" + invalidId + "/histories";

		// when
		try {
			restClient.get()
				.uri(path)
				.retrieve()
				.toEntity(new ParameterizedTypeReference<>() {
				});
		} catch (HttpClientErrorException e) {
			// then
			assertAll(
				() -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST),
				() -> assertThat(e.getResponseBodyAs(ErrorResponse.class))
					.isNotNull()
					.extracting(ErrorResponse::code, ErrorResponse::message, ErrorResponse::validation)
					.containsExactly("400", "유효성 검사 실패",
						Map.of(
							"history.id", "사용자 id는 0 보다 큰 정수여야 합니다."
						))
			);
		}
	}

	@Test
	@DisplayName("GET /point/{id}/history API 요청 시 id가 정수형이 아니라면 400 Bad Request와 ErrorResponse를 반환한다.")
	void findPointHistoryByIdFailWhenInputTypeIsString() {
		// given
		String userId = "asdfasdf";
		String path = "/point/" + userId + "/histories";

		// when
		try {
			restClient.get()
				.uri(path)
				.retrieve()
				.toEntity(new ParameterizedTypeReference<>() {
				});
		} catch (HttpClientErrorException e) {
			// then
			assertAll(
				() -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST),
				() -> assertThat(e.getResponseBodyAs(ErrorResponse.class))
					.isNotNull()
					.extracting(ErrorResponse::code, ErrorResponse::message)
					.containsExactly("400", "올바르지 않은 타입입니다.")
			);
		}

	}

	@Test
	@DisplayName("PATCH /point/{id}/charge API 요청이 성공한다면 200 OK와 충전이 반영된 UserPoint를 반환한다.")
	void chargePoint() {
		// given
		long userId = 1L;
		long initialPoint = 1000L;
		long chargeAmount = 1000L;
		String path = "/point/" + userId + "/charge";
		pointRepository.saveOrUpdate(userId, initialPoint);

		// when
		ResponseEntity<UserPoint> response = restClient.patch()
			.uri(path)
			.body(chargeAmount)
			.retrieve()
			.toEntity(UserPoint.class);

		// then
		assertAll(
			() -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
			() -> assertThat(response.getBody())
				.isNotNull()
				.extracting(UserPoint::id, UserPoint::point)
				.containsExactly(userId, initialPoint + chargeAmount)
		);
	}

	@ParameterizedTest
	@ValueSource(longs = {0, -1})
	@DisplayName("PATCH /point/{id}/charge API 요청 시 충전할 포인트가 0 이하라면 400 Bad Request와 ErrorResponse를 반환한다.")
	void chargePointFailWhenChargeAmountIsZeroOrUnderZero(long invalidChargeAmount) {
		// given
		long userId = 1L;
		String path = "/point/" + userId + "/charge";

		// when
		try {
			restClient.patch()
				.uri(path)
				.body(invalidChargeAmount)
				.retrieve()
				.toEntity(UserPoint.class);
		} catch (HttpClientErrorException e) {
			// then
			assertAll(
				() -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST),
				() -> assertThat(e.getResponseBodyAs(ErrorResponse.class))
					.isNotNull()
					.extracting(ErrorResponse::code, ErrorResponse::message, ErrorResponse::validation)
					.containsExactly("400", "유효성 검사 실패",
						Map.of(
							"charge.amount", "충전할 포인트는 0 보다 큰 정수여야 합니다."
						))
			);
		}
	}

	@Test
	@DisplayName("PATCH /point/{id}/charge API 요청 시 포인트가 최대 한도(10만)를 넘는다면 400 Bad Request와 ErrorResponse를 반환한다.")
	void chargePointFailWhenOverMaxLimit() {
		// given
		long userId = 1L;
		long chargeAmount = 100_001L;
		String path = "/point/" + userId + "/charge";

		// when
		try {
			restClient.patch()
				.uri(path)
				.body(chargeAmount)
				.retrieve()
				.toEntity(UserPoint.class);
		} catch (HttpClientErrorException e) {
			// then
			assertAll(
				() -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST),
				() -> assertThat(e.getResponseBodyAs(ErrorResponse.class))
					.isNotNull()
					.extracting(ErrorResponse::code, ErrorResponse::message)
					.containsExactly("400", "최대 포인트 한도는 100000 입니다. 입력값: %d".formatted(chargeAmount))
			);
		}
	}

	@Test
	@DisplayName("PATCH /point/{id}/use API 요청이 성공한다면 200 OK와 사용 포인트가 차감된 UserPoint를 반환한다.")
	void usePoint() {
		// given
		long userId = 1L;
		long initialPoint = 1000L;
		long useAmount = 500L;
		String path = "/point/" + userId + "/use";
		pointRepository.saveOrUpdate(userId, initialPoint);

		// when
		ResponseEntity<UserPoint> response = restClient.patch()
			.uri(path)
			.body(useAmount)
			.retrieve()
			.toEntity(UserPoint.class);

		// then
		assertAll(
			() -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
			() -> assertThat(response.getBody())
				.isNotNull()
				.extracting(UserPoint::id, UserPoint::point)
				.containsExactly(userId, initialPoint - useAmount)
		);
	}

	@ParameterizedTest
	@ValueSource(longs = {0, -1})
	@DisplayName("PATCH /point/{id}/use API 요청 시 사용할 포인트가 0 이하라면 400 Bad Request와 ErrorResponse를 반환한다.")
	void usePointFailWhenUseAmountIsZeroOrUnderZero(long useAmount) {
		// given
		long userId = 1L;
		String path = "/point/" + userId + "/use";

		// when
		try {
			restClient.patch()
				.uri(path)
				.body(useAmount)
				.retrieve()
				.toEntity(UserPoint.class);
		} catch (HttpClientErrorException e) {
			// then
			assertAll(
				() -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST),
				() -> assertThat(e.getResponseBodyAs(ErrorResponse.class))
					.isNotNull()
					.extracting(ErrorResponse::code, ErrorResponse::message, ErrorResponse::validation)
					.containsExactly("400", "유효성 검사 실패",
						Map.of(
							"use.amount", "사용할 포인트는 0 보다 큰 정수여야 합니다."
						))
			);
		}
	}

	@Test
	@DisplayName("PATCH /point/{id}/use API 요청 시 사용할 포인트가 최대 사용 가능한 포인트(1만)를 넘는다면 400 Bad Request와 ErrorResponse를 반환한다.")
	void usePointFailWhenOverMaxLimit() {
		// given
		long userId = 1L;
		long useAmount = 10_001L;
		String path = "/point/" + userId + "/use";

		// when
		try {
			restClient.patch()
				.uri(path)
				.body(useAmount)
				.retrieve()
				.toEntity(UserPoint.class);
		} catch (HttpClientErrorException e) {
			// then
			assertAll(
				() -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST),
				() -> assertThat(e.getResponseBodyAs(ErrorResponse.class))
					.isNotNull()
					.extracting(ErrorResponse::code, ErrorResponse::message)
					.containsExactly("400", "최대 사용 가능한 포인트는 10000입니다. 입력값: %d".formatted(useAmount))
			);
		}
	}

	@Test
	@DisplayName("PATCH /point/{id}/use API 요청 시 사용할 포인트가 보유한 포인트를 초과한다면 400 Bad Request와 ErrorResponse를 반환한다.")
	void usePointFailWhenOverUserPoint() {
		// given
		long userId = 1L;
		long useAmount = 1001L;
		String path = "/point/" + userId + "/use";
		pointRepository.saveOrUpdate(userId, 1000L);

		// when
		try {
			restClient.patch()
				.uri(path)
				.body(useAmount)
				.retrieve()
				.toEntity(UserPoint.class);
		} catch (HttpClientErrorException e) {
			// then
			assertAll(
				() -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST),
				() -> assertThat(e.getResponseBodyAs(ErrorResponse.class))
					.isNotNull()
					.extracting(ErrorResponse::code, ErrorResponse::message)
					.containsExactly("400",
						"갖고 있는 포인트를 초과해서 사용할 수 없습니다. 입력값: %d, 현재 포인트 잔고: %d".formatted(useAmount, 1000))
			);
		}
	}
}
