package com.usememo.jugger.domain.photo.dto;

import org.springframework.http.codec.multipart.FilePart;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class PhotoDto {
	private String user_uuid;
	private String category_uuid;
	private FilePart filePart;
}
