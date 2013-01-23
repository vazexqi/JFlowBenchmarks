package edu.illinois.jflow.benchmark;

import net.semanticmetadata.lire.DocumentBuilder;
import net.semanticmetadata.lire.DocumentBuilderFactory;
import net.semanticmetadata.lire.utils.FileUtils;
import net.semanticmetadata.lire.utils.LuceneUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

public class Benchmark100 {

    private static final String IMAGESPATH= "./wang-100";

    private static final String INDEXPATH_SERIAL= "./wang-benchmark-index-serial";

    DocumentBuilder createDocumentBuilder() {
        return DocumentBuilderFactory.getExplodedFullDocumentBuilder();
    }

    void execute() throws IOException, InterruptedException {
        DocumentBuilder builder= createDocumentBuilder();
        IndexWriter indexWriter= LuceneUtils.createIndexWriter(INDEXPATH_SERIAL, true);
        ArrayList<String> images= FileUtils.getAllImages(new File(IMAGESPATH), true);

        long start= System.currentTimeMillis();
        for (String file : images) {
            FileInputStream inputStream= new FileInputStream(file);
            try {
                Document doc= builder.createDocument(inputStream, file);
                indexWriter.addDocument(doc);
            } catch (Exception e) {
                System.err.println("Error indexing file: " + file + "(" + e.getMessage() + ")");
            } finally {
                inputStream.close();
            }
        }

        long end= System.currentTimeMillis();

        float timeTaken= (end - start);
        System.out.print(timeTaken + "\t");

        indexWriter.optimize();
        indexWriter.close();
    }

    void cleanUp() {
        delete(new File(INDEXPATH_SERIAL));
    }

    private void delete(File file) {
        if (file.isDirectory()) {
            for (String path : file.list()) {
                delete(new File(file, path));
            }
        }
        file.delete();
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("Benchmarking Serial Benchmark with Wang100");
        System.out.println("==========================================");

        int REPETITIONS= 1;

        for (int iter= 1; iter <= REPETITIONS; iter++) {
            Benchmark100 benchmark= new Benchmark100();
            benchmark.execute();
            benchmark.cleanUp();
        }
        System.out.println("");
    }
}
