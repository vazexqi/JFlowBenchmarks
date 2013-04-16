package edu.illinois.jflow.benchmark;

import net.semanticmetadata.lire.DocumentBuilder;
import net.semanticmetadata.lire.DocumentBuilderFactory;
import net.semanticmetadata.lire.imageanalysis.AutoColorCorrelogram;
import net.semanticmetadata.lire.imageanalysis.FCTH;
import net.semanticmetadata.lire.imageanalysis.JpegCoefficientHistogram;
import net.semanticmetadata.lire.imageanalysis.Tamura;
import net.semanticmetadata.lire.impl.GenericDocumentBuilder;
import net.semanticmetadata.lire.impl.GenericFastDocumentBuilder;
import net.semanticmetadata.lire.utils.FileUtils;
import net.semanticmetadata.lire.utils.LuceneUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;

public class LireIndexingExample {

    private static final int NUM_IMAGES= 100;

    static final String IMAGES_DIRECTORY= "./wang-100";

    static final String DATABASE= "./candidate_database";

    private DocumentBuilder JPEGExtractor;

    private DocumentBuilder tamuraExtractor;

    private DocumentBuilder autoColorCorrelogramExtractor;

    private DocumentBuilder FCTHExtractor;

    public LireIndexingExample() {
        this.JPEGExtractor= new GenericDocumentBuilder(JpegCoefficientHistogram.class, DocumentBuilder.FIELD_NAME_JPEGCOEFFS, GenericDocumentBuilder.Mode.Fast);
        this.tamuraExtractor= new GenericFastDocumentBuilder(Tamura.class, DocumentBuilder.FIELD_NAME_TAMURA);
        this.autoColorCorrelogramExtractor= new GenericDocumentBuilder(AutoColorCorrelogram.class, DocumentBuilder.FIELD_NAME_AUTOCOLORCORRELOGRAM, GenericDocumentBuilder.Mode.Fast);
        this.FCTHExtractor= new GenericDocumentBuilder(FCTH.class, DocumentBuilder.FIELD_NAME_FCTH, GenericDocumentBuilder.Mode.Fast);
    }

    DocumentBuilder JPEGCoefficientHistogramExtractor() {
        return JPEGExtractor;
    }

    DocumentBuilder tamuraExtractor() {
        return tamuraExtractor;
    }

    DocumentBuilder autoColorCorrelogramExtractor() {
        return autoColorCorrelogramExtractor;
    }

    DocumentBuilder FCTHExtractor() {
        return FCTHExtractor;
    }

    void serialIndexImages() throws Exception {
        IndexWriter indexWriter= LuceneUtils.createIndexWriter(DATABASE, true);

        for (String imagePath : FileUtils.getAllImages(new File(IMAGES_DIRECTORY), true)) {

            // Begin Stage1
            BufferedImage bufferedImage= ImageIO.read(new FileInputStream(imagePath));
            Document docJPEG= JPEGExtractor.createDocument(bufferedImage, imagePath);
            // End Stage1

            // Begin Stage2
            Document docTamura= tamuraExtractor.createDocument(docJPEG, bufferedImage, imagePath);
            // End Stage2

            // Begin Stage3
            Document docColor= autoColorCorrelogramExtractor.createDocument(docTamura, bufferedImage, imagePath);
            // End Stage3

            // Begin Stage4
            Document docFCTH= FCTHExtractor.createDocument(docColor, bufferedImage, imagePath);
            indexWriter.addDocument(docFCTH);
            // End Stage4
        }

        indexWriter.optimize();
        indexWriter.close();
    }

    void timeIndexImages() throws Exception {
        long start= System.currentTimeMillis();
        serialIndexImages();
        long end= System.currentTimeMillis();

        float timeTaken= end - start;
        System.out.println("Total time taken: " + timeTaken + " ms");
    }

    void verifyDatabase() throws Exception {
        IndexReader indexReader= IndexReader.open(new RAMDirectory(FSDirectory.open(new File(DATABASE))), true);
        int numDocuments= indexReader.maxDoc();

        if (numDocuments != NUM_IMAGES) {
            System.err.println("Number of documents is only " + numDocuments);
            return;
        }

        for (int docIndex= 0; docIndex < numDocuments; docIndex++) {
            Document document= indexReader.document(docIndex);
            Fieldable field1= document.getFieldable(DocumentBuilder.FIELD_NAME_JPEGCOEFFS);
            Fieldable field2= document.getFieldable(DocumentBuilder.FIELD_NAME_TAMURA);
            Fieldable field3= document.getFieldable(DocumentBuilder.FIELD_NAME_AUTOCOLORCORRELOGRAM);
            Fieldable field4= document.getFieldable(DocumentBuilder.FIELD_NAME_FCTH);
            if (field1 == null || field2 == null || field3 == null || field4 == null) {
                Fieldable name= document.getFieldable(DocumentBuilder.FIELD_NAME_IDENTIFIER);
                System.err.println(name + " is missing extractor featured");
            }
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("Running Lire Indexing Example");
        LireIndexingExample lire= new LireIndexingExample();
        lire.timeIndexImages();
    }

}
