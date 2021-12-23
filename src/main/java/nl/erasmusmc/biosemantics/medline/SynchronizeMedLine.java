package nl.erasmusmc.biosemantics.medline;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileFilter;
import org.apache.commons.net.ftp.FTPReply;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SynchronizeMedLine {

    private static final String HOST = "ftp.ncbi.nlm.nih.gov";
    public static final String USERNAME = "anonymous";
    public static final String PASSWORD = "secret-password";
    public static final String PUBMED_UPDATE_FILES = "/pubmed/updatefiles/";

    public static void main(String[] args) {
        System.out.println("Starting MedLine downloader");
        boolean useCloud = args.length == 0;

        if (useCloud) {
            System.out.println("Loading file from 'the cloud'");
            Cloud cloud = new Cloud();
            String input = cloud.loadFile();
            String outputDir = "./files";
            doWork(input, outputDir);
            cloud.saveFile(input);
        } else {
            System.out.println("Reading CLI args");
            String input = args[0];
            String outputDir = args[1];
            doWork(input, outputDir);
        }

        System.out.println("Done!");
    }

    private static void doWork(String input, String outputDir) {
        System.out.println("Existing files list: " + input);
        System.out.println("Output dir: " + outputDir);

        Set<String> existingFilenames = getExistingFilenames(input);
        System.out.println(existingFilenames.size() + " files have already been processed");

        FTPClient ftp = getFTP();
        List<FTPFile> availableFiles = getAvailableFiles(ftp);

        availableFiles.stream()
                .filter(FTPFile::isFile)
                .map(FTPFile::getName)
                .filter(file -> !existingFilenames.contains(file))
                .forEach(file -> {
                    downloadFile(outputDir, ftp, file);
                    appendLine(file, input);
                });
    }

    private static void downloadFile(String outputFolder, FTPClient ftp, String file) {
        try {
            System.out.println("Processing new file: " + file);
            OutputStream output = new FileOutputStream(outputFolder + "/" + file);
            ftp.retrieveFile(PUBMED_UPDATE_FILES + file, output);
            output.close();
        } catch (Exception e) {
            fail(e);
        }
    }

    private static List<FTPFile> getAvailableFiles(FTPClient ftp) {
        try {
            FTPFileFilter filter = ftpFile -> ftpFile.getName().endsWith(".xml.gz");
            FTPFile[] ftpFiles = ftp.listFiles(PUBMED_UPDATE_FILES, filter);
            System.out.println(ftpFiles.length + " files are available on the MedLine ftp server");
            if (ftpFiles.length == 0) {
                System.err.println("No files available with MedLine");
                System.exit(1);
            }
            return Arrays.asList(ftpFiles);
        } catch (IOException e) {
            fail(e);
        }
        return Collections.emptyList();
    }

    private static FTPClient getFTP() {
        FTPClient ftp = new FTPClient();
        try {
            ftp.connect(HOST);
            int reply = ftp.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                ftp.disconnect();
            }
            ftp.login(USERNAME, PASSWORD);
            ftp.setFileType(FTP.BINARY_FILE_TYPE);
            ftp.enterLocalPassiveMode();
        } catch (IOException e) {
            fail(e);
        }
        return ftp;
    }

    private static void appendLine(String fileName, String input) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(input, true))) {
            bw.newLine();
            bw.write(fileName);
        } catch (IOException e) {
            fail(e);
        }
    }

    private static Set<String> getExistingFilenames(String input) {
        try (Stream<String> stream = Files.lines(Paths.get(input))) {
            return stream.collect(Collectors.toSet());
        } catch (IOException e) {
            fail(e);
        }
        return Collections.emptySet();
    }

    public static void fail(Exception e) {
        e.printStackTrace();
        System.exit(1);
    }

}