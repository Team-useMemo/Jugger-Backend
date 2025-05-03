package com.usememo.jugger.global.exception;

import java.util.Map;

import lombok.Getter;

@Getter
public class KakaoException extends  BaseException{
	private final ErrorCode errorCode;
	private final Map<String,Object> maps;

	public KakaoException(ErrorCode errorCode, Map<String, Object> maps) {
		super(errorCode);
		this.errorCode = errorCode;
		this.maps = maps;
	}
}
