package com.usememo.jugger.global.exception;

import static org.springframework.http.HttpStatus.*;

import org.springframework.http.HttpStatus;
import lombok.Getter;

@Getter
public enum ErrorCode {

	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 500, "서버 내부 오류입니다."),
	FILE_UPLOAD_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, 501, "S3 파일 업로드에 실패했습니다."),
	CATEGORY_ALREADY_EXIST(BAD_REQUEST, 400, "이미 존재하는 카테고리입니다."),
	CATEGORY_IS_NULL(BAD_REQUEST, 401, "카테고리는 NULL값일 수 없습니다."),
	NO_REFRESH_TOKEN(UNAUTHORIZED, 402, "리프레시 토큰이 없습니다."),
	EXPIRED_REFRESH_TOKEN(UNAUTHORIZED, 403, "만료된 토큰입니다."),
	KAKAO_TOKEN_REQUEST_FAILED(BAD_REQUEST, 410, "카카오 인가 코드로 토큰을 요청하는 데 실패했습니다."),
	KAKAO_TOKEN_MISSING(BAD_REQUEST, 411, "카카오에서 access_token을 받지 못했습니다."),
	KAKAO_USERINFO_REQUEST_FAILED(BAD_REQUEST, 412, "카카오 사용자 정보를 가져오는 데 실패했습니다."),
	KAKAO_USERINFO_INCOMPLETE(BAD_REQUEST, 413, "카카오 사용자 정보가 불완전합니다."),
	KAKAO_EMAIL_MISSING(BAD_REQUEST, 414, "카카오 응답에 이메일이 존재하지 않습니다."),
	KAKAO_NAME_MISSING(BAD_REQUEST, 415, "카카오 응답에 닉네임이 존재하지 않습니다."),
	KAKAO_ACCESS_TOKEN_EMPTY(UNAUTHORIZED, 416, "카카오 access token이 비어 있습니다."),
	KAKAO_INVALID_AUTH_CODE(BAD_REQUEST, 417, "카카오 인가 코드가 유효하지 않습니다."),
	KAKAO_CONNECTION_FAILED(BAD_GATEWAY, 418, "카카오 API 서버와 통신에 실패했습니다."),
	KAKAO_JSON_PARSE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 419, "카카오 응답 JSON 파싱에 실패했습니다."),
	KAKAO_UNKNOWN_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 420, "카카오 로그인 중 알 수 없는 오류가 발생했습니다."),
	KAKAO_JWT_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 421, "카카오 jwt 토큰 제공시 에러"),

	KAKAO_USER_NOT_FOUND(BAD_REQUEST,427,"존재하지 않는 회원입니다."),

	DUPLICATE_USER(BAD_REQUEST,428,"중복된 회원정보입니다."),

	JWT_KEY_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, 422, "JWT 키 생성에 실패했습니다."),
	JWT_ACCESS_TOKEN_CREATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, 423, "액세스 토큰 생성 실패"),
	JWT_REFRESH_TOKEN_CREATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, 424, "리프레시 토큰 생성 실패"),
	JWT_PARSE_FAILED(HttpStatus.UNAUTHORIZED, 425, "JWT 파싱 실패"),
	JWT_BUNDLE_CREATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, 426, "JWT 번들 생성 실패");

	private final HttpStatus httpStatus;
	private final int code;
	private final String message;

	ErrorCode(HttpStatus httpStatus, int code, String message) {
		this.httpStatus = httpStatus;
		this.code = code;
		this.message = message;
	}
}
