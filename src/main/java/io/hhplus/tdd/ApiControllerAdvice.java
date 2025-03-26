package io.hhplus.tdd;

import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
class ApiControllerAdvice extends ResponseEntityExceptionHandler {

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(
		MethodArgumentTypeMismatchException e) {
		return ResponseEntity.status(400).body(new ErrorResponse("400", "올바르지 않은 타입입니다."));
	}

	@ExceptionHandler(value = ConstraintViolationException.class)
	public ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException e) {
		Map<String, String> errors = e.getConstraintViolations().stream()
			.collect(Collectors.toMap(
				cv -> cv.getPropertyPath().toString(),
				ConstraintViolation::getMessage
			));
		return ResponseEntity.status(400).body(new ErrorResponse("400", "유효성 검사 실패", errors));
	}

	@ExceptionHandler(value = IllegalArgumentException.class)
	public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException e) {
		return ResponseEntity.status(400).body(new ErrorResponse("400", e.getMessage()));
	}

	@ExceptionHandler(value = Exception.class)
	public ResponseEntity<ErrorResponse> handleException(Exception e) {
		return ResponseEntity.status(500).body(new ErrorResponse("500", "에러가 발생했습니다."));
	}
}
