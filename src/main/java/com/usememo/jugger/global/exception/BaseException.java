package com.usememo.jugger.global.exception;

import lombok.Getter;

@Getter
public class BaseException extends RuntimeException {
	private final ErrorCode errorCode;
	private final String message;

	public BaseException(ErrorCode errorCode) {
		super(errorCode.getMessage());
		this.errorCode = errorCode;
		this.message = errorCode.getMessage();
	}

	public BaseException(ErrorCode errorCode, Throwable cause) {
		super(errorCode.getMessage(), cause); // RuntimeException에 메시지 + 원인 등록
		this.errorCode = errorCode;
		this.message = errorCode.getMessage();
	}
}
