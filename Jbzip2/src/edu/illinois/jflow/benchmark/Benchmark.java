package edu.illinois.jflow.benchmark;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

import org.itadaki.bzip2.BZip2OutputStream;

public class Benchmark {

    public static void main(String[] args) throws Exception {
        final File dir= new File("dir");
        dir.mkdirs();
        final File inputFile= new File("inputs/media.dat");
        final File outputFile= new File(dir, "media.compressed.bz2");

        InputStream fileInputStream= new BufferedInputStream(new FileInputStream(inputFile));
        BufferedOutputStream bufferedOutputStream= new BufferedOutputStream(new FileOutputStream(outputFile));
        BZip2OutputStream compressorStream= new BZip2OutputStream(bufferedOutputStream);

        // Compression - need to use buffered streams or the results will be too I/O intensive
        long startCompressed= System.currentTimeMillis();

        byte[] buffer= new byte[524288]; //TODO: What is this magic number (also from /Jbzip2/src/demo/Compress.java)
        int bytesRead;
        while ((bytesRead= fileInputStream.read(buffer)) != -1) {
            compressorStream.write(buffer, 0, bytesRead);
        }

        fileInputStream.close();
        compressorStream.close();

        long stopCompressed= System.currentTimeMillis();
        System.out.println((stopCompressed - startCompressed) + "ms");
    }
}
