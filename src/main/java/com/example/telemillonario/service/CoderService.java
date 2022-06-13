package com.example.telemillonario.service;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;

@Component
@Service
public class CoderService {

    public String codificar(String value){
        Encoder encoder = Base64.getEncoder();
        return encoder.encodeToString(value.getBytes());
    }

    public String decodificar(String value){
        Decoder decoder = Base64.getDecoder();
        return new String(decoder.decode(value));
    }
}
