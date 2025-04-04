package com.usememo.jugger.domain.chat.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.usememo.jugger.domain.calendar.repository.CalendarRepository;
import com.usememo.jugger.domain.chat.dto.PostChatDto;
import com.usememo.jugger.domain.chat.entity.Chat;
import com.usememo.jugger.domain.chat.repository.ChatRepository;
import com.usememo.jugger.domain.link.entity.Link;
import com.usememo.jugger.domain.link.repository.LinkRepository;
import com.usememo.jugger.global.exception.chat.CategoryNullException;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class ChatServiceImplementationTest {

	private ChatRepository chatRepository;
	private CalendarRepository calendarRepository;
	private LinkRepository linkRepository;
	private ChatService chatService;

	@BeforeEach
	void setUp() {
		chatRepository = mock(ChatRepository.class);
		calendarRepository = mock(CalendarRepository.class);
		linkRepository = mock(LinkRepository.class);
		chatService = new ChatServiceImplementation(calendarRepository, linkRepository, chatRepository);
	}

	@Test
	@DisplayName("채팅 입력 - category id null일떄")
	void postChatCategoryNull() {
		PostChatDto postChatDto = PostChatDto.builder()
			.categoryUuid(null)
			.text("놀고싶다")
			.build();
		StepVerifier.create(chatService.postChat(postChatDto))
			.expectError(CategoryNullException.class)
			.verify();

		verify(chatRepository, never()).save(any());
	}

	@Test
	@DisplayName("정상 입력 시 Chat 저장 후 Mono<Void> 반환")
	void postChatSave() {
		PostChatDto dto = PostChatDto.builder()
			.categoryUuid("category-123")
			.text("테스트 메시지")
			.build();

		when(chatRepository.save(any(Chat.class)))
			.thenReturn(Mono.just(mock(Chat.class)));

		StepVerifier.create(chatService.postChat(dto))
			.verifyComplete();

		ArgumentCaptor<Chat> captor = ArgumentCaptor.forClass(Chat.class);
		verify(chatRepository).save(captor.capture());

		Chat chat = captor.getValue();
		assertThat(chat.getCategoryUuid()).isEqualTo("category-123");
		assertThat(chat.getData()).isEqualTo("테스트 메시지");
		assertThat(chat.getUserUuid()).isEqualTo("123456789a");
		assertThat(chat.getUuid()).isNotNull();
	}

	@Test
	@DisplayName("사용자가 link를 입력했을때, 정규표현식으로 분류하는지 확인")
	void postLinkSave() {
		PostChatDto dto = PostChatDto.builder()
			.categoryUuid("category-123")
			.text("https://www.youtube.com/watch?v=F-zSxR1zgYQ&list=RDF-zSxR1zgYQ&start_radio=1")
			.build();

		when(linkRepository.save(any(Link.class))).thenReturn(Mono.just(Link.builder().build()));
		when(chatRepository.save(any(Chat.class))).thenReturn(Mono.just(Chat.builder().build()));

		Mono<Void> result = chatService.postChat(dto);

		StepVerifier.create(result)
			.verifyComplete(); // 정상 완료 확인

		verify(linkRepository, times(1)).save(argThat(link ->
			link.getUrl().equals(dto.getText()) &&
				link.getCategoryUuid().equals(dto.getCategoryUuid())
		));

		verify(chatRepository, times(1)).save(argThat(chat ->
			chat.getData().equals(dto.getText()) &&
				chat.getCategoryUuid().equals(dto.getCategoryUuid()) &&
				chat.getRefs() != null &&
				chat.getRefs().getLinkUuid() != null
		));

	}

}