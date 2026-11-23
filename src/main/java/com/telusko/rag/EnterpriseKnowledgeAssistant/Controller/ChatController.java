package com.telusko.rag.EnterpriseKnowledgeAssistant.Controller;

import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.telusko.rag.EnterpriseKnowledgeAssistant.dto.ChatResponse;
import com.telusko.rag.EnterpriseKnowledgeAssistant.service.ChatService;

@RestController
@RequestMapping("/api/chat")
public class ChatController {
	
	private final ChatService chatService;
	
	
	public ChatController(ChatService chatService) {
		
		this.chatService=chatService;
	
		
	}
	
	@GetMapping
	public ChatResponse chat(@RequestParam String question,
			                      @RequestParam(required=false) UUID documentId,
			                          @RequestParam(required=false) UUID conversationId) {
		
		
		
		UUID activeConversationId=conversationId!=null?conversationId:UUID.randomUUID();
		
		 
		
		return chatService.ask(question,documentId,activeConversationId);
		
		
		
		
	}
	
	
	
	
	
	
	
	
	
	
	
	

}
