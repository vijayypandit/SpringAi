package com.spring.ai.first.springai;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.spring.ai.first.springai.service.DataLoader;
import com.spring.ai.first.springai.service.DataTransformer;
import org.springframework.ai.vectorstore.VectorStore;

@SpringBootTest
class SpringaiApplicationTests {

    @Autowired
    private DataLoader dataLoader;
    @Autowired
    private DataTransformer dataTransformer;
    @Autowired
    private VectorStore vectorStore;

    @Test
    void testDataLoader() {

        System.out.println("TEST ------- loading data from json");
        var documents = this.dataLoader.loadDocumentsFromJson();
        System.out.println("size from simple_data ----- => " + documents.size());

        documents.forEach(item -> System.out.println("doc --" + item));

    }

    @Test
    void testPdfLoader() {
        System.out.println("Test Method - loading data from PDF");
        var documents = this.dataLoader.loadDocumentsFromPdf();
        System.out.println("size from PDF ----- => " + documents.size());
        documents.forEach(item -> System.out.println("====================" + item));

        System.out.println("--------------Transformer Testing ---------");
        var transnformedDocuments = this.dataTransformer.transform(documents);
        System.out.println("Transformed documents size : " + transnformedDocuments.size());

        // //going to save in the vector db :
        // this.vectorStore.add(transnformedDocuments);
        // System.out.println("Added to vector store successfully");

    }

}
