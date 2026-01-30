package job.search.kg.service;

import job.search.kg.dto.request.user.SaveMessageRequest;
import job.search.kg.dto.response.admin.ChatHistoryResponse;
import job.search.kg.dto.response.admin.ChatMessageDTO;
import job.search.kg.entity.ChatMessage;
import job.search.kg.entity.User;
import job.search.kg.repo.ChatMessageRepository;
import job.search.kg.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatMessageService {
    private final UserRepository userRepository;
    private final ChatMessageRepository chatMessageRepository;

    @Transactional
    public void saveMessage(SaveMessageRequest request) {
        User user = userRepository.findByTelegramId(request.getTelegramUserId())
                .orElseThrow(() -> new RuntimeException("User not found: " + request.getTelegramUserId()));

        ChatMessage message = new ChatMessage();
        message.setUser(user);
        message.setSenderType(parseSenderType(request.getSenderType()));
        message.setMessageText(request.getMessageText());
        message.setTelegramMessageId(Long.valueOf(request.getTelegramMessageId()));
        message.setMessageType(parseMessageType(request.getMessageType()));

        chatMessageRepository.save(message);
    }

    @Transactional(readOnly = true)
    public ChatHistoryResponse getUserChatHistory(Long userId, int page, int size) {
        // Создаем запрос с сортировкой по дате (новые сверху)
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<ChatMessage> messagesPage = chatMessageRepository
                .findByUserIdOrderByCreatedAtDesc(userId, pageRequest);

        // Преобразуем в DTO
        List<ChatMessageDTO> messages = messagesPage.getContent().stream()
                .map(msg -> new ChatMessageDTO(
                        msg.getId(),
                        msg.getSenderType().name(),
                        msg.getMessageText(),
                        msg.getCreatedAt()
                ))
                .collect(Collectors.toList());

        return new ChatHistoryResponse(
                messages,
                messagesPage.getNumber(),
                messagesPage.getTotalPages(),
                messagesPage.getTotalElements()
        );
    }
    private ChatMessage.SenderType parseSenderType(String type) {
        try {
            return ChatMessage.SenderType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid sender type: " + type);
        }
    }

    private ChatMessage.MessageType parseMessageType(String type) {
        if (type == null) return ChatMessage.MessageType.TEXT;
        try {
            return ChatMessage.MessageType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ChatMessage.MessageType.TEXT;
        }
    }
}
