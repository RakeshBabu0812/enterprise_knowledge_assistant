package com.telusko.rag.EnterpriseKnowledgeAssistant.dto;

import java.util.List;
import java.util.UUID;



public record ChatResponse(
		
		UUID conversationId,
		String answer,
		List<SourceCitation> sources,
		ResponseStatus status
		) {
	
	
	public enum ResponseStatus{
		
		NO_RELEVANT_DOCUMENTS,
		INSUFFICIENT_EVIDENCE,
		ANSWERED
	}
	
	
	
	public record SourceCitation(
			
			UUID documentId,
			String fileName,
			Integer pageNumber,
			Integer chunkIndex

			) {  }
	
	
	

}
