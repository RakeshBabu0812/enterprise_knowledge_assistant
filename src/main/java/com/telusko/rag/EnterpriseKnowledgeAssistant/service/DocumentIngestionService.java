package com.telusko.rag.EnterpriseKnowledgeAssistant.service;

import java.security.MessageDigest;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.telusko.rag.EnterpriseKnowledgeAssistant.dto.DocumentInfo;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;

import jakarta.annotation.PostConstruct;

import java.util.HexFormat;

@Service
public class DocumentIngestionService {
	
	
	private final VectorStore vectorStore;

	private final JdbcTemplate jdbcTemplate;
	
	private static final Logger log =
	        LoggerFactory.getLogger(DocumentIngestionService.class);
	
	
	private record StoredDocument(UUID documentId, String fileHash) {}
	
	
	public DocumentIngestionService(VectorStore vectorStore,JdbcTemplate jdbcTemplate) {
		this.vectorStore=vectorStore;
		this.jdbcTemplate=jdbcTemplate;
	}
	
	
	
	private static final Pattern SAFE_FILENAME=Pattern
			                                        .compile("^[A-Za-z0-9][A-Za-z0-9._ -]{0,119}\\.pdf$",
			                                        		Pattern.CASE_INSENSITIVE);
	
	
	
	public List<DocumentInfo> getAllDocuments(){
		
		
		return jdbcTemplate.query("""
			       SELECT d.document_id,
			       d.file_name,
				   d.file_hash,
				   d.ingested_at,
				   COUNT(v.id) as total_chunks
				   FROM document_ingestion d
				   LEFT JOIN vector_store v
				   ON  v.metadata->>'document_id'=d.document_id::text
				   GROUP BY 
				   d.document_id,
				   d.file_name,
				   d.file_hash,
				   d.ingested_at
				   ORDER BY d.ingested_at DESC """
				   ,(rs,rowNum)-> new DocumentInfo(
						      rs.getObject("document_id",UUID.class), 
						      rs.getString("file_name"),
						      rs.getString("file_hash"),
						      rs.getTimestamp("ingested_at").toLocalDateTime(),
						      rs.getLong("total_chunks")
						   )
				   
		
				);
		
	}
	
	
	public DocumentInfo getDocument(String fileName) {
		
		
		List<DocumentInfo> documents=jdbcTemplate.query(
				"""
				SELECT d.document_id,
				d.file_name,
				d.file_hash,
				d.ingested_at,
				COUNT(v.id) as total_chunks
				FROM document_ingestion d
				LEFT JOIN vector_store v
				ON v.metadata->>'document_id'=d.document_id::text
				WHERE d.file_name=?
				GROUP BY 
				d.document_id,
				d.file_name,
				d.file_hash,
				d.ingested_at
				"""
					,(rs,rowNum)-> new DocumentInfo(		
						rs.getObject("document_id",UUID.class),
						rs.getString("file_name"),
						rs.getString("file_hash"),
						rs.getTimestamp("ingested_at").toLocalDateTime(),
						rs.getLong("total_chunks")
						),
				
				fileName
				
		);
		
			return documents.isEmpty()?null:documents.get(0);
	
				
				
				
			

		
		
	}
	
	
						
     @Transactional
	public boolean deleteDocument(UUID documentId) {
    	 
    	 List<String> documents=jdbcTemplate.query(
    			   """
    	 		
    	 		SELECT file_name 
    	 		From document_ingestion
    	 		WHERE document_id=?
  
    	 		""",
    	 		  (rs,rowNum)->rs.getString("file_name")
    	 		,documentId) ;
    	 
    	 
    	 if(documents.isEmpty()) {
    		 return false;
    	 }
    	 
    	 vectorStore.delete("document_id=='"+documentId+"'");
    	 
    	 jdbcTemplate.update("DELETE FROM document_ingestion WHERE document_id=?",documentId);
    	 
    	 log.info("Deleted Document: {} ",documentId);
    	 
    	 return true;
		
	
		
	}
			
	 
	
	
	private String calculateHash(Resource resource) throws Exception {
		
		byte[] content;
		
		try(var input=resource.getInputStream()){
			
			content=input.readAllBytes(); 
		}
		
		byte[] hash=MessageDigest.getInstance("SHA-256").digest(content);
	
		return HexFormat.of().formatHex(hash);
		

	}
	
	
	//@PostConstruct
	//public void loadDocument() {
		
//	Resource resource=new ClassPathResource("docs/employee-policy.pdf");
		
//ingestPdf(resource,"employee-policy.pdf");
		
		
//}
	
	
	
    @Transactional
	public UUID ingestPdf(Resource resource,String fileName) {
		
		 

		
		log.info("Starting PDF ingestion...");
		
		/*
		Long existingchunks=jdbcTemplate.queryForObject(
				
				 "SELECT COUNT(*) FROM vector_store " +
					        "WHERE metadata->>'file_name' = ?",
					        Long.class,
					        "employee-policy.pdf"
				
				);
		
		
		
		

				if(existingchunks!=null  && existingchunks>0) {
					log.info("Documents already exist. Skiping ingestion");
					return;
				}
				*/
		
		
		
		
		
		
		try {
			String currentHash=calculateHash(resource);
			
			/* jdbcTemplate.execute("""
				        CREATE TABLE IF NOT EXISTS document_ingestion (
				            document_id UUID Primary Key 
				            file_name VARCHAR(255) UNIQUE,
				            file_hash VARCHAR(64) NOT NULL,
				            ingested_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
				        )
				        """);*/
			 
			 List<StoredDocument> existing=jdbcTemplate.query(
					 "SELECT document_id,file_hash From document_ingestion WHERE file_name=?",
					  (rs,rowNum)-> new StoredDocument(
							    rs.getObject("document_id",UUID.class),
							    rs.getString("file_hash")
							  ),
					  fileName
					 );
			 
			 
			 UUID documentId=existing.isEmpty()?UUID.randomUUID():existing.get(0).documentId();
			 
			 
			 if(!existing.isEmpty() && existing.get(0).fileHash().equals(currentHash)) {
				 
	             log.info("PDF is unchanged,Skipping: {}",fileName);
	             
	             return documentId;
			 }
			 
			 
			 log.info("Reading PDF: {}",fileName);
			 
			 
			 PagePdfDocumentReader reader=new PagePdfDocumentReader(resource);
				
				List<Document> documents=reader.get();
				
				log.info("Loaded {} documents", documents.size());
				
				
				
				TokenTextSplitter splitter=new TokenTextSplitter();
				
				
				List<Document> chunks=splitter.apply(documents);
				
				if(chunks.isEmpty()) {
					
					throw new IllegalStateException("No chunks were generated from the PDF.");
					
					
				}
				
				
				for(int i=0;i<chunks.size();i++) {
					
					Document chunk=chunks.get(i);
					
					chunk.getMetadata().put("file_name", fileName);
					chunk.getMetadata().put("document_id",documentId.toString());
					chunk.getMetadata().put("chunk_index",i);

				}
				
				
				log.info("Generated {} chunks for {}", chunks.size(),fileName);
				
				if(!existing.isEmpty()) {
				
			   vectorStore.delete("document_id =='"+documentId+"'");
			   
		   
			 
			  
			   
				}
				
			
				
				vectorStore.add(chunks);
				
				
				 jdbcTemplate.update("""
		                    INSERT INTO document_ingestion
		                        (document_id,file_name, file_hash, ingested_at)
		                    VALUES (?,?, ?, CURRENT_TIMESTAMP)
		                    ON CONFLICT (file_name)
		                    DO UPDATE SET
		                        file_hash = EXCLUDED.file_hash,
		                        ingested_at = CURRENT_TIMESTAMP
		                    """,
		                    documentId,
		                    fileName,
		                    currentHash
		            );

				
				 log.info("PDF ingestion completed. Document ID: {} ",documentId);
				 
				 
			return documentId;
				
			
		}
		catch(Exception e) {
			
			log.error("PDF ingestion failed for {}",fileName,e);
			
			throw new IllegalStateException("PDF ingestion failed "+fileName,e);
		
		}
		
		
		
		
		
		 
	
		
		
	}
	

	
	
	
}
