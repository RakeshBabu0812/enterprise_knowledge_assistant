package com.telusko.rag.EnterpriseKnowledgeAssistant.Controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.telusko.rag.EnterpriseKnowledgeAssistant.dto.ConversationMessage;
import com.telusko.rag.EnterpriseKnowledgeAssistant.dto.ConversationSummary;
import com.telusko.rag.EnterpriseKnowledgeAssistant.dto.PagedResponse;
import com.telusko.rag.EnterpriseKnowledgeAssistant.service.ChatService;

@RestController
@RequestMapping("/api/conversations")
public class ConversationController {
	
	
	
	private final ChatService chatService;
	
	public ConversationController(ChatService chatService) {
		
		this.chatService=chatService;
		
	}
	
	@GetMapping("/{conversationId}/messages")
	public List<ConversationMessage> getConversationHistory(@PathVariable UUID conversationId){
		
		
		return chatService.getConversationHistory(conversationId);
		
	}
	
	@GetMapping
	public ResponseEntity<PagedResponse<ConversationSummary>> getAllConversations(@RequestParam(defaultValue="0") int page,
			                                           @RequestParam(defaultValue="10") int size){
		
		if(page<0  || size<=0  || size>100) {
			
			return ResponseEntity.badRequest().build();
			
			
		}
		
		
		return ResponseEntity.ok(
				chatService.getAllConversations(page,size)
				);
		
		
	}
	
	@DeleteMapping("/{conversationId}")
	public ResponseEntity<Void> deleteConversation(@PathVariable UUID conversationId){
		
		
		boolean deleted=chatService.deleteConversation(conversationId);
		
		if(!deleted) {
			
			return ResponseEntity.notFound().build();
		
		}
		
		return ResponseEntity.noContent().build();
		
		
		
		
		
		
		
		
		
		
		
		
	}
	
	
	

}
