package com.telusko.rag.EnterpriseKnowledgeAssistant.Controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.telusko.rag.EnterpriseKnowledgeAssistant.dto.ConversationMessage;
import com.telusko.rag.EnterpriseKnowledgeAssistant.dto.ConversationSummary;
import com.telusko.rag.EnterpriseKnowledgeAssistant.dto.PagedResponse;
import com.telusko.rag.EnterpriseKnowledgeAssistant.dto.RenameConversationRequest;
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
			                                           @RequestParam(defaultValue="10") int size,
			                                           @RequestParam(required=false) String search ){
		
		if(page<0  || size<=0  || size>100) {
			
			return ResponseEntity.badRequest().build();
			
			
		}
		
		
		return ResponseEntity.ok(
				chatService.getAllConversations(page,size,search)
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
	
	
	@PatchMapping("/{conversationId}/title")
	public ResponseEntity<Void> renameConversation(
			
			@PathVariable UUID conversationId,
			@RequestBody RenameConversationRequest request
			
			){
		
		
		if(request.title()==null || request.title().isBlank() || request.title().length()>255) {
			
			return ResponseEntity.badRequest().build();
			
		}
		
		boolean updated=chatService.renameConversation(conversationId,request.title());
		
		if(!updated) {
			return ResponseEntity.notFound().build();
		}
		
		return ResponseEntity.noContent().build();
		
	
	}
	
	
	
	@PatchMapping("/{conversationId}/archive")
	public ResponseEntity<Void> archiveConversation(@PathVariable UUID conversationId){
		
		boolean archived=chatService.archiveConversation(conversationId);
		
		if(!archived) {
			return ResponseEntity.notFound().build();
		}
		
		return ResponseEntity.noContent().build();
		
	
	}
	
	@PatchMapping("/{conversationId}/restore")
	public ResponseEntity<Void> restoreConversation(@PathVariable UUID conversationId){
		
		
		boolean restored=chatService.restoreConversation(conversationId);
		
		if(!restored) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.noContent().build();
		
		
		
		
		
		
	}
	
	
	

}
