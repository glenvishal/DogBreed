package com.dog.breed;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.dog.entity.Breed;
import com.dog.repository.BreedRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import sun.java2d.pipe.SpanShapeRenderer;

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value="/breed", produces = MediaType.APPLICATION_JSON_VALUE)
public class DogBreedController {
    private String EXTERNAL_API;
    private RestTemplate restTemplate;
    private AmazonS3 s3Client;
    private String bucketName;
    private String downloadDir;
    private BreedRepository breedRepository;
    private String REGION;

    @Autowired
    public DogBreedController(@Value("${app.external.url}") String EXTERNAL_API, RestTemplate restTemplate,
                              AmazonS3 s3Client,
                              @Value("${app.region}") String REGION,
                              @Value("${app.bucket-name}") String bucketName,
                              @Value("${app.download-dir}") String downloadDir,
                              BreedRepository breedRepository) {
        this.EXTERNAL_API = EXTERNAL_API;
        this.restTemplate = restTemplate;
        this.s3Client = s3Client;
        this.bucketName = bucketName;
        this.downloadDir = downloadDir;
        this.breedRepository = breedRepository;
        this.REGION = REGION;
    }

    @RequestMapping(value = "/generateRecord", method = RequestMethod.GET)
    public ResponseEntity<?> generateRecord() throws IOException {

        ResponseEntity<String> response
                = restTemplate.getForEntity(EXTERNAL_API, String.class);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(response.getBody());
        JsonNode message = root.path("message");

        String dogName = message.toString().split("/")[4];
        String fileName = message.toString().split("/")[5];

        if(downLoadPic(message.textValue(), fileName)){
            uploadToBucket(dogName, fileName);
        }

        storeBreedInDb(dogName, getDate(), REGION);

        return new ResponseEntity<Void>(HttpStatus.CREATED);
    }

    @RequestMapping(value = "/getRecord/{id}", method = RequestMethod.GET)
    public ResponseEntity<Breed> getRecord(@PathVariable String id) throws IOException {

        Optional<Breed> breed = breedRepository.findById(Long.valueOf(id));

        return new ResponseEntity<Breed>(breed.get(), HttpStatus.OK);
    }

    @RequestMapping(value = "/deleteRecord/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<?> deleteRecord(@PathVariable String id) throws IOException {

        Optional<Breed> breed = breedRepository.findById(Long.valueOf(id));

        s3Client.deleteObject(bucketName,breed.get().getName());
        breedRepository.deleteById(Long.valueOf(id));

        return new ResponseEntity<Void>(HttpStatus.OK);
    }

    @RequestMapping(value = "/searchBreed/{name}", method = RequestMethod.GET)
    public ResponseEntity<List<Breed>> searchBreed(@PathVariable String name) throws IOException {

        List<Breed> breed = breedRepository.findByName(name);

        return new ResponseEntity<>(breed, HttpStatus.OK);
    }

    @RequestMapping(value = "/searchAllBreeds", method = RequestMethod.GET)
    public ResponseEntity<List<String>> searchBreed() throws IOException {

        List<Breed> breed = breedRepository.findAll();

        List<String> response = breed.stream().map(Breed::getName).collect(Collectors.toList());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private String uploadToBucket(String dogName, String fileName) {

        PutObjectResult result = s3Client.putObject(bucketName, dogName, new File(downloadDir+fileName));
        System.out.println(result.getETag());
        return result.getETag();

    }

    private boolean downLoadPic(String url, String fileName) throws  IOException{

        try (BufferedInputStream in = new BufferedInputStream(new URL(url).openStream());
             FileOutputStream fileOutputStream = new FileOutputStream(downloadDir+fileName)) {
            byte dataBuffer[] = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                fileOutputStream.write(dataBuffer, 0, bytesRead);
            }
            return true;
        } catch (IOException e) {
            throw e;
        }
    }

    private void storeBreedInDb(String dogName, String date, String location){
        Breed breed = new Breed();
        breed.setName(dogName);
        breed.setDate(date);
        breed.setLocation(location);
        breedRepository.save(breed);
    }

    private String getDate(){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return dateFormat.format(new Date());
    }


}
