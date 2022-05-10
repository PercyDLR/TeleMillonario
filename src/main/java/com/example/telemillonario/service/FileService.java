package com.example.telemillonario.service;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobItem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
@Service
public class FileService {

    @Value("${spring.cloud.azure.storage.blob.connection-string}")
    private String azureConnectionString;

    @Value("${spring.cloud.azure.storage.blob.container-name}")
    private String containerName;

    private BlobContainerClient containerClient(){
        BlobServiceClient serviceClient = new BlobServiceClientBuilder().connectionString(azureConnectionString).buildClient();
        BlobContainerClient containerClient = serviceClient.getBlobContainerClient(containerName);
        return containerClient;
    }

    public List<String> listarArchivos(){
        BlobContainerClient container = containerClient();
        List<String> archivos = new ArrayList<>();
        for(BlobItem blobItem : container.listBlobs()){
            archivos.add(blobItem.getName());
        }
        return archivos;
    }

    public boolean subirArchivo(MultipartFile file){
        try{
            BlobContainerClient container = containerClient();
            BlobClient blob = container.getBlobClient(file.getOriginalFilename());
            blob.upload(file.getInputStream(),file.getSize(),true);
            //System.out.println("Se ha subido el archivo de manera exitosa ");
            return true;
        }catch (Exception e){
            //System.out.println("Ha ocurrido un error en la subida del archivo");
            return false;
        }
    }

    public String obtenerUrl(String filename){
        try{
            BlobContainerClient container = containerClient();
            BlobClient blob = container.getBlobClient(filename);
            System.out.println(blob.getBlobUrl());
            return blob.getBlobUrl();
        }catch (Exception e){
            return "Not founded";
        }
    }

    public boolean eliminarArchivo(String filename){
        try{
            BlobContainerClient container = containerClient();
            BlobClient blob = container.getBlobClient(filename);
            if(blob.exists()){
                blob.delete();
                return true;
            }else{
                return false;
            }
        }catch (Exception e){
            return false;
        }
    }

    public ByteArrayOutputStream descargarArchivo(String filename){
        BlobContainerClient container = containerClient();
        BlobClient blob = container.getBlobClient(filename);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        if(blob.exists()){
            blob.download(os);
        }
        return os;
    }

    public MultipartFile formatearArchivo(MultipartFile file,String nombreBase) throws IOException {
        Random random = new Random();
        String capitalCaseLetters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lowerCaseLetters = "abcdefghijklmnopqrstuvwxyz";
        String numbers = "1234567890";
        int seed = this.listarArchivos().size();
        String nombre = nombreBase+"_"+numbers.charAt(random.nextInt(seed))+
                lowerCaseLetters.charAt(random.nextInt(seed))+
                capitalCaseLetters.charAt(random.nextInt(seed))+
                file.getOriginalFilename().split("\\.")[0].charAt(random.nextInt(seed))+
                "."+
                file.getOriginalFilename().split("\\.")[1];
        MultipartFile output = new MockMultipartFile(nombre,nombre,file.getContentType(),file.getBytes());
        return output;
    }

}
