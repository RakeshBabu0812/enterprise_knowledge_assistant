package com.telusko.rag.EnterpriseKnowledgeAssistant.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ConversationSummary(
		UUID conversationId,
		String title,
		LocalDateTime lastActivity,
		Long messageCount
		) {
	

}
