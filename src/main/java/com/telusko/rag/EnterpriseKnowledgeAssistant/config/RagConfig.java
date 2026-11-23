package com.telusko.rag.EnterpriseKnowledgeAssistant.config;

import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RagConfig {
	
	
	
	@Bean
	public Advisor ragAdvisor(VectorStore vectorStore) {
		
		
		VectorStoreDocumentRetriever documentRetriever=VectorStoreDocumentRetriever.builder()
				                                             .vectorStore(vectorStore)
				                                            .similarityThreshold(0.50)
				                                             .topK(5)
				                                             .build();
				
		return RetrievalAugmentationAdvisor.builder().documentRetriever(documentRetriever).build();
		
		
		
		
		
	}
	

}
