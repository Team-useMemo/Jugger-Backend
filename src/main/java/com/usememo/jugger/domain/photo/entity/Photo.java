package com.usememo.jugger.domain.photo.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import com.usememo.jugger.global.utils.BaseTimeEntity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Document(collection = "photos")
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Getter
public class Photo extends BaseTimeEntity  implements Persistable<String> {

	@Id
	private String uuid;

	@Field("user_uuid")
	private String userUuid;

	@Field("url")
	private String url;

	@Setter
	@Field("category_uuid")
	private String categoryUuid;

	@Setter
	@Field("description")
	private String description;

	public Photo() {
	}

	@Override
	public String getId() {
		return getUuid();
	}

	@Override
	public boolean isNew() {
		return getCreatedAt() == null;
	}


}
