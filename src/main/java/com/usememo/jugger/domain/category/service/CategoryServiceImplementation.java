package com.usememo.jugger.domain.category.service;

import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.mongodb.client.result.UpdateResult;
import com.usememo.jugger.domain.category.dto.ClassifyRequest;
import com.usememo.jugger.domain.category.dto.ClassifyResponse;
import com.usememo.jugger.domain.category.dto.GetRecentCategoryDto;
import com.usememo.jugger.domain.category.dto.PostCategoryDto;
import com.usememo.jugger.domain.category.dto.PostCategoryWithUuidDto;
import com.usememo.jugger.domain.category.dto.UpdateRequest;
import com.usememo.jugger.domain.category.dto.UpdateResponse;
import com.usememo.jugger.domain.category.entity.Category;
import com.usememo.jugger.domain.category.repository.CategoryRepository;
import com.usememo.jugger.domain.chat.entity.Chat;
import com.usememo.jugger.domain.chat.repository.ChatRepository;
import com.usememo.jugger.domain.chat.service.ChatService;
import com.usememo.jugger.global.exception.BaseException;
import com.usememo.jugger.global.exception.ErrorCode;
import com.usememo.jugger.global.exception.category.CategoryExistException;
import com.usememo.jugger.global.exception.chat.CategoryNullException;
import com.usememo.jugger.global.security.CustomOAuth2User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImplementation implements CategoryService {
	private final CategoryRepository categoryRepository;

	private final ChatRepository chatRepository;
	private final ChatService chatService;

	private final ReactiveMongoTemplate reactiveMongoTemplate;
	private final WebClient fastApiWebClient;


	public Mono<Category> createCategory(PostCategoryDto dto, CustomOAuth2User customOAuth2User) {
		return categoryRepository.findByNameAndUserUuid(dto.getName(),customOAuth2User.getUserId())
			.flatMap(existing -> Mono.<Category>error(new CategoryExistException()))
			.switchIfEmpty(Mono.defer(() -> {
				Category newCategory = Category.builder()
					.uuid(UUID.randomUUID().toString())
					.name(dto.getName())
					.color(dto.getColor())
					.userUuid(customOAuth2User.getUserId())
					.isPinned(false)
					.build();
				return categoryRepository.save(newCategory);
			}));
	}

	@Override
	public Mono<Category> createCategoryWithUuid(PostCategoryWithUuidDto postCategoryWithUuidDto,
		CustomOAuth2User customOAuth2User) {
		return categoryRepository.findByUuid(postCategoryWithUuidDto.getCategoryUuid())
			.switchIfEmpty(Mono.error(new CategoryNullException()))
			.flatMap(category -> {
				category.setName(postCategoryWithUuidDto.getName());
				category.setColor(postCategoryWithUuidDto.getColor());
				category.setPinned(false);
				return categoryRepository.save(category);
			});
	}

	@Override
	public Flux<GetRecentCategoryDto> getRecentCategories(CustomOAuth2User customOAuth2User) {
		String userId = customOAuth2User.getUserId();

		return categoryRepository.findAllByUserUuid(userId)
			.collectList()
			.map(categories -> categories.stream()
				.sorted(
					Comparator.comparing(Category::getIsPinned)// isPinned=true 우선
						.thenComparing(Category::getUpdatedAt).reversed()   // updatedAt 최신 우선
				)
				.collect(Collectors.toList()))
			.flatMapMany(sortedCategories -> Flux.fromIterable(sortedCategories))
			.flatMap(category ->
				chatService.getLatestChatByCategoryId(customOAuth2User, category.getUuid())
					.map(Chat::getData)
					.defaultIfEmpty("")
					.map(recentMessage -> GetRecentCategoryDto.builder()
						.uuid(category.getUuid())
						.name(category.getName())
						.color(category.getColor())
						.isPinned(category.getIsPinned())
						.updateAt(category.getUpdatedAt())
						.recentMessage(recentMessage)
						.build())
			);
	}

	@Override
	public Mono<UpdateResult> pinCategory(String categoryId, boolean isPinned) {
		Query query = Query.query(Criteria.where("uuid").is(categoryId));
		Update update = new Update().set("isPinned", isPinned);
		return reactiveMongoTemplate.updateFirst(query, update, Category.class);

	}

	@Override
	public Mono<Boolean> deleteCategory(String categoryId, CustomOAuth2User customOAuth2User) {
		return categoryRepository.findByUuid(categoryId)
			.switchIfEmpty(Mono.error(new BaseException(ErrorCode.NO_CATEGORY)))
			.flatMap(category -> {
				if (!category.getUserUuid().equals(customOAuth2User.getUserId())) {
					return Mono.error(new BaseException(ErrorCode.NO_AUTHORITY));
				}
				return categoryRepository.deleteByUuid(categoryId)
					.then(chatRepository.deleteByCategoryUuid(categoryId))
					.thenReturn(true);
			});
	}

	@Override
	public Mono<UpdateResponse> updateCategory(UpdateRequest updateRequest, CustomOAuth2User customOAuth2User) {
		return categoryRepository.findByUuid(updateRequest.categoryId())
			.switchIfEmpty(Mono.error(new BaseException(ErrorCode.NO_CATEGORY)))
			.flatMap(category -> {
				if (!category.getUserUuid().equals(customOAuth2User.getUserId())) {
					return Mono.error(new BaseException(ErrorCode.NO_AUTHORITY));
				}
				Query query = new Query(Criteria.where("uuid").is(updateRequest.categoryId()));

				Update update = new Update();
				if (updateRequest.newName() != null) {
					update.set("name", updateRequest.newName());
				}
				if (updateRequest.newColor() != null) {
					update.set("color", updateRequest.newColor());
				}

				return reactiveMongoTemplate.updateFirst(query, update, Category.class)
					.map(result -> new UpdateResponse(
						200,
						"카테고리 업데이트가 완료되었습니다."
					));
			});

	}

	//여기서 이제 호출부를 만들어야 함
	//어떤 리스트를 전달하는 식으로 짜야할까 ?
	@Override
	public Mono<List<String>> aiClassify(CustomOAuth2User user, String memo) {
		final double threshold = 0.5;

		return categoryRepository.findAllByUserUuid(user.getUserId())
			.switchIfEmpty(Mono.error(new BaseException(ErrorCode.NO_CATEGORY)))
			.map(Category::getName)
			.filter(name -> name != null && !name.isBlank())
			// "temp" 포함된 카테고리는 제외
			.filter(name -> !name.toLowerCase().contains("temp"))
			.distinct()
			.collectList()
			.flatMap(userCategories -> classify(
					memo,
					userCategories.isEmpty() ? null : userCategories,
					threshold
				).map(ClassifyResponse::recommendCategory)
			)
			.defaultIfEmpty(List.of())
			.onErrorResume(ex -> Mono.just(List.of()));
	}



	private Mono<ClassifyResponse> classify(String paragraph, List<String> userCategories, Double threshold) {

		ClassifyRequest body = new ClassifyRequest(
			paragraph,
			userCategories,
			threshold == null ? 0.5 : threshold
			// ,"5"
		);

		return fastApiWebClient.post()
			.uri("/ai/classify")
			.contentType(MediaType.APPLICATION_JSON)
			.accept(MediaType.APPLICATION_JSON)
			.bodyValue(body)
			.retrieve()
			.onStatus(HttpStatusCode::isError, resp ->
				resp.bodyToMono(String.class)
					.defaultIfEmpty("")
					.map(b -> WebClientResponseException.create(
						resp.statusCode().value(),
						"FastAPI /classify failed",
						resp.headers().asHttpHeaders(),
						b.getBytes(StandardCharsets.UTF_8),
						StandardCharsets.UTF_8
					))
			)
			.bodyToMono(ClassifyResponse.class);
	}

}
