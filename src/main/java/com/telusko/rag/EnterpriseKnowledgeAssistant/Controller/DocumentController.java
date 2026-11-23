package com.telusko.rag.EnterpriseKnowledgeAssistant.Controller;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.multipart.MultipartFile;

import com.telusko.rag.EnterpriseKnowledgeAssistant.dto.DocumentInfo;
import com.telusko.rag.EnterpriseKnowledgeAssistant.service.DocumentIngestionService;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {
	
	
	
	
	private static final long MAX_FILE_SIZE=10*1024*1024;
	
	private static final Pattern SAFE_FILENAME=
			                                  Pattern.compile(
			                                		  "^[A-Za-z0-9][A-Za-z0-9._ -]{0,119}\\.pdf$",
			                                		  Pattern.CASE_INSENSITIVE
			                                		 );
	
	private final DocumentIngestionService ingestionService;
	
	
	public DocumentController(DocumentIngestionService ingestionService) {
		
		this.ingestionService=ingestionService;
		
		
	}
	
	
	@PostMapping(value="/upload",consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
	
	public ResponseEntity<Map<String,String>> uploadPdf(@RequestParam("file") MultipartFile file){

		if(file.isEmpty()) {
			
			return error(400,"Please select a non-empty PDF.");
			
		}
		
		if(file.getSize() > MAX_FILE_SIZE) {
			
			return error(413,"PDF must not exceed 10 MB.");
			
		}
		
		
		String fileName=file.getOriginalFilename();
		
		if(fileName==null ||!SAFE_FILENAME.matcher(fileName).matches()) {
			
			
			return error(400, "Invalid filename. Use letters, numbers, spaces, "
					+ "hyphens, underscores, and a .pdf extension.");
		
		}
		
		try(InputStream input=file.getInputStream()){
			
			byte[] header=input.readNBytes(5);
			
			if(! new String(header,
					java.nio.charset.StandardCharsets.US_ASCII).equals("%PDF-") ) {
				
				
				return error(400,"The uploaded Pdf is not a valid Pdf");
				
			}
			
	
			
		}
		catch(Exception e) {
			
			return error(400,"Unable to read the uploaded file");

			
		}
		
		try {
		      
			ingestionService.ingestPdf(file.getResource(), fileName);
			
			return ResponseEntity.ok(Map.of("message","Pdf processed successfully.",
					                        "fileName",fileName
					
					));
			
		
		}
		catch(Exception e) {
			
			return error(500,"Failed to process the PDF.");
		
		}
		
		
		
		
		
		
	}
	
	private ResponseEntity<Map<String,String>> error(int status,String message){
		
		
		return ResponseEntity.status(status).body(Map.of("error",message));
		
		
	}
	
	
	@GetMapping
	public ResponseEntity<List<DocumentInfo>> getAllDocuments(){
		
		
		return ResponseEntity.ok(
				ingestionService.getAllDocuments()
				);
		
		
	}
	
	
	@GetMapping("/{fileName}")
	public ResponseEntity<?> getDocument(@PathVariable String fileName){
		
		
		DocumentInfo document=ingestionService.getDocument(fileName);
		
		if(document==null) {
			return ResponseEntity.notFound().build();
		}
		
		return ResponseEntity.ok(document);

		
		
		
		
		
	}
	
	
	
	
	
	
	@DeleteMapping("/{documentId}")
	public ResponseEntity<Map<String,String>> deleteDocument(@PathVariable UUID documentId){
		
		try {
			
			boolean deleted=ingestionService.deleteDocument(documentId);
			
			if(!deleted) {
				
				return error(404,"Document not found: " + documentId);
			
			}
			
			return ResponseEntity.ok(Map.of("message","Document deleted successfully.",
					                   "documentId",documentId.toString()
					                   
					
					));
			
			
			
			
		}
		catch(IllegalArgumentException e) {
			
			return error(400,e.getMessage());
		}
		
		catch(Exception e) {
			
			return ResponseEntity.internalServerError().body(
					
					Map.of("error","Failed to delete Document.")
				
					);
					
			
			
		}
		
		
		
		
		
		
		
		
		
	}
	
	
	
	
	
	
	
	
	
	

}
