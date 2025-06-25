package com.usememo.jugger.domain.photo.dto;

import java.time.Instant;
import java.util.List;

import com.usememo.jugger.domain.photo.entity.Photo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GetPhotoDto {
	private String url;
	private String categoryId;
	private String description;
	private Instant timestamp;


}
