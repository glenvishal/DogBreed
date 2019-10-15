package com.dog;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class DogBreedApplication {
	@Value("${app.access-key}")
	private String ACCESS_KEY;

	@Value("${app.secret-key}")
	private String SECRET_KEY;

    @Value("${app.region}")
    private String REGION;

	public static void main(String[] args) {
		SpringApplication.run(DogBreedApplication.class, args);
	}

	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}

	@Bean
	public AWSCredentials awsCredentials() {
		return new BasicAWSCredentials(ACCESS_KEY, SECRET_KEY);
	}

	//AmazonS3
    @Bean
    public AmazonS3 amazonS3() {
        AWSCredentials credentials = new BasicAWSCredentials(ACCESS_KEY, SECRET_KEY);
        AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(Regions.fromName(REGION))
                .build();

	    return s3Client;
    }

}
