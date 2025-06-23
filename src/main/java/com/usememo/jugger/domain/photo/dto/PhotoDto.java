package com.usememo.jugger.domain.photo.dto;

import org.springframework.http.codec.multipart.FilePart;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class PhotoDto {
	private String userId;
	private String categoryId;
	private FilePart filePart;
}
