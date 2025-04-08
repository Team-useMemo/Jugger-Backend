package com.usememo.jugger.domain.link.dto;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GetLinkDto {
	private String categoryUuid;
	private String categoryName;

	private List<LinkData> linkData;

	@Data
	@Builder
	public static class LinkData {
		private String caption;
		private String link;
	}
}
