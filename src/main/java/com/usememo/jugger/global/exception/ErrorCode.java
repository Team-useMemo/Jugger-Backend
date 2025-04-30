package com.usememo.jugger.global.exception;

import static org.springframework.http.HttpStatus.*;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public enum ErrorCode {
	CATEGORY_ALREADY_EXIST(BAD_REQUEST, 400, "이미 존재하는 카테고리입니다"),
	CATEGORY_IS_NULL(BAD_REQUEST, 400, "카테고리는 NULL값일 수 없습니다"),
	FILE_UPLOAD_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, 500, "S3 파일 업로드에 실패했습니다."),
	NO_REFRESH_TOKEN(UNAUTHORIZED,400,"리프레시 토큰이 없습니다."),
	EXPIRED_REFRESH_TOKEN(UNAUTHORIZED,400, "만료된 토큰입니다"),
	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 500, "서버 내부 오류입니다.");

	private final HttpStatus httpStatus;
	private final int code;
	private final String message;

	ErrorCode(HttpStatus httpStatus, int code, String message) {
		this.httpStatus = httpStatus;
		this.code = code;
		this.message = message;
	}

}
