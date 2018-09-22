package com.demo.imageresizer;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.apache.http.client.methods.HttpRequestBase;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.util.IOUtils;

/**
 * A simple test harness for locally invoking your Lambda function handler.
 */
@RunWith(MockitoJUnitRunner.class)
public class LambdaFunctionHandlerTest {

    private static final String TEST_IMAGE_FILENAME = "HappyFace.jpg";
	private final String CONTENT_TYPE = "image/jpeg";
    private S3Event event;

    @Mock
    private AmazonS3 s3Client;
    @Mock
    private S3Object s3Object;
    
    @Mock
    private HttpRequestBase httpRequestBaseMock;

    @Captor
    private ArgumentCaptor<GetObjectRequest> getObjectRequest;
	
    @Captor
    private ArgumentCaptor<PutObjectRequest> putObjectRequestCaptor;
    

    @Before
    public void setUp() throws IOException {
        event = TestUtils.parse("/s3-event.put.json", S3Event.class);
        
        InputStream is = IOUtils.class.getResourceAsStream("/HappyFace.jpg");
        S3ObjectInputStream s = new S3ObjectInputStream(is, httpRequestBaseMock);
        
        // TODO: customize your mock logic for s3 client
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType(CONTENT_TYPE);
        when(s3Object.getObjectMetadata()).thenReturn(objectMetadata);
        when(s3Client.getObject(getObjectRequest.capture())).thenReturn(s3Object);
        when(s3Object.getObjectContent()).thenReturn(s);
        when(s3Object.getKey()).thenReturn(TEST_IMAGE_FILENAME);
        
    }

    private Context createContext() {
        TestContext ctx = new TestContext();

        // TODO: customize your context here if needed.
        ctx.setFunctionName("Your Function Name");

        return ctx;
    }

    @Test
    public void testLambdaFunctionHandler() throws IOException {
        LambdaFunctionHandler handler = new LambdaFunctionHandler(s3Client);
        Context ctx = createContext();

        String output = handler.handleRequest(event, ctx);
        verify(s3Client).putObject(putObjectRequestCaptor.capture());
        PutObjectRequest putObject = putObjectRequestCaptor.getValue();
        File imageFile = putObject.getFile();
        BufferedImage resizedImage = ImageIO.read(imageFile);
        
        Assert.assertEquals(100,resizedImage.getHeight());
        Assert.assertEquals(100, resizedImage.getWidth());
        
        
        Assert.assertEquals("satyadestinationimage", putObject.getBucketName());
        Assert.assertEquals(TEST_IMAGE_FILENAME, putObject.getKey());
        
        // TODO: validate output here if needed.
        Assert.assertEquals(CONTENT_TYPE, output);
    }
}
