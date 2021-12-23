package nl.erasmusmc.biosemantics.medline;


import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import static nl.erasmusmc.biosemantics.medline.SynchronizeMedLine.fail;

public class Cloud {


    private final String bucket;
    private final String filename;
    private final AmazonS3 s3;

    public Cloud() {
        bucket = System.getenv("S3_BUCKET");
        filename = System.getenv("S3_FILENAME");
        String accessKey = System.getenv("AWS_ID");
        String secretKey = System.getenv("AWS_SECRET");
        BasicAWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);
        s3 = AmazonS3ClientBuilder.standard()
                .withRegion(Regions.EU_WEST_1)
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                .build();
    }

    public String loadFile() {
        S3Object file = s3.getObject(bucket, filename);
        InputStream is = file.getObjectContent();

        try {
            File tmp = File.createTempFile(filename, "");
            Files.copy(is, tmp.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return tmp.getAbsolutePath();
        } catch (IOException e) {
            fail(e);
        }
        return null;
    }

    public void saveFile(String path) {
        System.out.println("Saving file " + path + " to s3 bucket: " + bucket + ", as: " + filename);
        s3.putObject(bucket, filename, new File(path));
    }
}
