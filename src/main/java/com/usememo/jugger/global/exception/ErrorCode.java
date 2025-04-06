package com.usememo.jugger.global.exception;

import static org.springframework.http.HttpStatus.*;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public enum ErrorCode {
	CATEGORY_ALREADY_EXIST(BAD_REQUEST, 400, "이미 존재하는 카테고리입니다"),
	CATEGORY_IS_NULL(BAD_REQUEST, 400, "카테고리는 NULL값일 수 없습니다"),
	FILE_UPLOAD_FAIL(INTERNAL_SERVER_ERROR, 500, "S3 파일 업로드에 실패했습니다.");

	private final HttpStatus httpStatus;
	private final int code;
	private final String message;

	ErrorCode(HttpStatus httpStatus, int code, String message) {
		this.httpStatus = httpStatus;
		this.code = code;
		this.message = message;
	}

}
