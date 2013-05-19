package edu.illinois.jflow.benchmark;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.itadaki.bzip2.BZip2InputStream;
import org.itadaki.bzip2.BZip2OutputStream;


/**
 * Tests the compression/decompression cycle on a file or directory. Compression is performed using
 * a temporary file, and the target file or directory is not altered.
 */
public class RoundTrip {

    /**
     * A FileFilter that returns only ordinary, readable files
     */
    private static final FileFilter fileFilter= new FileFilter() {

        @Override
        public boolean accept(File file) {
            return file.isFile() && file.canRead();
        }

    };


    /**
     * A FileFilter that returns only readable directories
     */
    private static final FileFilter directoryFilter= new FileFilter() {

        @Override
        public boolean accept(File file) {
            return file.isDirectory() && file.canRead();
        }

    };


    /**
     * Recursively enumerates ordinary, readable files
     * 
     * @param base The base file or directory name
     * @return A list of ordinary, readable files
     */
    private static List<File> getFiles(File base) {

        List<File> files= new ArrayList<File>();

        if (base.isFile()) {

            if (base.canRead()) {
                files.add(base);
            }

        } else {

            File[] subDirectories= base.listFiles(directoryFilter);
            for (File subDirectory : subDirectories) {
                files.addAll(getFiles(subDirectory));
            }

            for (File file : base.listFiles(fileFilter)) {
                files.add(file);
            }

        }

        return files;

    }


    /**
     * Compresses and decompresses each of a list of files, using a temporary file to hold the
     * compressed data
     * 
     * @param files The files to compress
     * @throws IOException On any error compressing or decompressing the files
     */
    private static void roundTrip(List<File> files) throws IOException {
        for (File inputFile : files) {
            // Begin Stage1
            File uncompressedOutputFile= File.createTempFile("jflow_in", ".tmp");
            InputStream fileInputStream= new BufferedInputStream(new FileInputStream(inputFile));
            BZip2InputStream compressedInputStream= new BZip2InputStream(fileInputStream, false);
            OutputStream uncompressedFileStream= new BufferedOutputStream(new FileOutputStream(uncompressedOutputFile), 524288);

            byte[] decoded= new byte[524288];
            int bytesRead;
            while ((bytesRead= compressedInputStream.read(decoded)) != -1) {
                uncompressedFileStream.write(decoded, 0, bytesRead);
            }
            uncompressedFileStream.close();
            compressedInputStream.close();
            // End Stage1

            // Do some processing in the middle

            // Begin Stage2
            File compressedOutputFile= File.createTempFile("jflow_out", ".bz2");
            InputStream fileStream= new BufferedInputStream(new FileInputStream(uncompressedOutputFile));
            OutputStream fileOutputStream= new BufferedOutputStream(new FileOutputStream(compressedOutputFile), 524288);
            BZip2OutputStream outputStream= new BZip2OutputStream(fileOutputStream);

            byte[] buffer= new byte[524288];
            int bytesWritten;
            while ((bytesWritten= fileStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesWritten);
            }
            outputStream.close();
            fileStream.close();
            // End Stage2
        }

    }


    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {

        if (args.length == 0) {
            System.err.println(
                    "Tests the compression/decompression cycle on a file or directory. Compression is" +
                            "performed using a temporary file, and the target file or directory is not altered.\n\n" +
                            "Usage:\n  java demo.RoundTrip <file or directory name>\n");
            System.exit(1);
        }

        System.out.println("Finding files...");
        List<File> files= getFiles(new File(args[0]));

        System.out.println("Testing compression/decompression cycle...");
        long sTime= System.currentTimeMillis();
        roundTrip(files);
        System.out.println("Time=" + (System.currentTimeMillis() - sTime));

    }


}
