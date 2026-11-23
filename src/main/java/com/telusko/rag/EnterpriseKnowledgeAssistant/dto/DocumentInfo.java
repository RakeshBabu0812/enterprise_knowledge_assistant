package com.telusko.rag.EnterpriseKnowledgeAssistant.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record DocumentInfo(
		UUID documentId,
		String fileName,
		String fileHash,
		LocalDateTime ingestedAt,
		long totalChunks
		) {
	
	
	
	
	

}
