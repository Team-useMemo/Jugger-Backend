package com.usememo.jugger.domain.photo.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GetPhotoRequestDto {
	private String userUuid;
	private String categoryUuid;
}
