package com.telusko.rag.EnterpriseKnowledgeAssistant.dto;

import java.time.Instant;

public record ConversationMessage(
		String role,
		String content,
		Instant timeStamp
		){
	

	
	
	

}
