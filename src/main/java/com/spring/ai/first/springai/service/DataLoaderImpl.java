package com.spring.ai.first.springai.service;

import java.util.List;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.JsonReader;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service
public class DataLoaderImpl implements DataLoader {

    @Value("classpath:simple-data.json")
    private Resource jsonResource;

    @Value("classpath:cricket_rules.pdf")
    private Resource pdfResource;

    @Override
    public List<Document> loadDocumentsFromJson() {

        System.out.println("loading data from json");
        var jsonReader = new JsonReader(jsonResource);
        return jsonReader.read();
    }

    @Override
    public List<Document> loadDocumentsFromPdf() {

        var pdfreader = new PagePdfDocumentReader(pdfResource,
                PdfDocumentReaderConfig.builder()
                        .withPageTopMargin(0)
                        .withPageExtractedTextFormatter(ExtractedTextFormatter.builder()
                                .withNumberOfTopTextLinesToDelete(1)
                                .build())
                        .build());

        return pdfreader.read();
    }

}
