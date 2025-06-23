package com.usememo.jugger.global.s3.dto;

import java.util.List;

public record FilesUploadResponse(int code, String message, List<Url> urls) {
	public static FilesUploadResponse of(int code, String message, List<String> urlList) {
		List<Url> urls = urlList.stream()
			.map(Url::new)
			.toList();

		return new FilesUploadResponse(code, message, urls);
	}

	public record Url(String url) {}
}
