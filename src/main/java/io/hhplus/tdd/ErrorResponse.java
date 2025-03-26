package io.hhplus.tdd;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
	String code,
	String message,
	Map<String, String> validation
) {
	public ErrorResponse(String code, String message) {
		this(code, message, null);
	}
}
