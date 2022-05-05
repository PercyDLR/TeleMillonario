package com.example.telemillonario.config;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import javax.sql.DataSource;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http.formLogin().loginPage("/login").loginProcessingUrl("/processLogin").defaultSuccessUrl("/redirectByRole",true);

        http.logout().logoutSuccessUrl("/paginaPrincipal").deleteCookies("JSESSIONID").invalidateHttpSession(true);


       /* http.authorizeRequests()
                .antMatchers()
                .
        */
    }

    @Autowired
    DataSource dataSource;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {

        auth.jdbcAuthentication()
                .dataSource(dataSource)
                .passwordEncoder(new BCryptPasswordEncoder())
                .usersByUsernameQuery("SELECT correo, contrasenia,estado FROM persona WHERE correo = ?")
                .authoritiesByUsernameQuery("SELECT correo,nombres FROM persona INNER JOIN rol ON ( persona.idrol = rol.id ) WHERE correo = ? and estado = 1");
    }
}
















