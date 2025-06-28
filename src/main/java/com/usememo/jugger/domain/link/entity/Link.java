package com.usememo.jugger.domain.link.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.mongodb.core.mapping.Document;

import com.usememo.jugger.global.utils.BaseTimeEntity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Document(collection = "links")
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Getter
public class Link extends BaseTimeEntity implements Persistable<String> {
	@Id
	private String uuid;
	private String userUuid;
	private String chatUuid;

	@Setter
	private String url;
	@Setter
	private String categoryUuid;

	private String caption;


	public Link() {
	}

	@Override
	public String getId(){
		return getUuid();
	}

	@Override
	public boolean isNew() {
		return getCreatedAt() == null;
	}
}

