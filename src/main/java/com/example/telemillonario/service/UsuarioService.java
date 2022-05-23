package com.example.telemillonario.service;


import com.example.telemillonario.entity.Persona;
import com.example.telemillonario.repository.PersonaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@Service
@Transactional
public class UsuarioService {

    @Autowired
    PersonaRepository personaRepository;


    public void updateResetPassword(String token,String correo){

        Persona persona = personaRepository.findByCorreo(correo);

        persona.setPasswordToken(token);
        personaRepository.save(persona);

    }


    public void updatePassword(Persona persona,String newPassword){
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encoddedPassword = passwordEncoder.encode(newPassword);

        persona.setContrasenia(encoddedPassword);
        persona.setPasswordToken("");

        personaRepository.save(persona);
    }



}
