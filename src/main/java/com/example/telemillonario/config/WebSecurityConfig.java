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

        http.formLogin()
                .loginPage("/login")
                .loginProcessingUrl("/processLogin")
                .defaultSuccessUrl("/redirectByRole",true);

        http.logout()
                .logoutSuccessUrl("/")
                .deleteCookies("JSESSIONID")
                .invalidateHttpSession(true);

        http.authorizeRequests()
                .antMatchers("/admin/**").hasAuthority("Administrador")
                .antMatchers("/operador/**").hasAuthority("Operador")
                .antMatchers("/calificarObra", "/historial").access("hasAuthority('Usuario')|| hasAuthority('ROLE_USER')")
                .antMatchers("/perfil/**").hasAnyAuthority("Operador","Usuario","ROLE_USER")
                .antMatchers("/","/cartelera/*","/sedes/*","/actores/*","/directores/*").access("hasAuthority('Usuario') || isAnonymous() || hasAuthority('ROLE_USER')")
                .anyRequest().permitAll().and()
                .oauth2Login()
                .loginPage("/login")
                .defaultSuccessUrl("/loginByGoogle",true);
    }

    @Autowired
    DataSource dataSource;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.jdbcAuthentication()
                .dataSource(dataSource)
                .passwordEncoder(new BCryptPasswordEncoder())
                .usersByUsernameQuery("SELECT correo, contrasenia,estado FROM persona WHERE correo = ?")
                .authoritiesByUsernameQuery("SELECT persona.correo,rol.nombre FROM persona INNER JOIN rol ON ( persona.idrol = rol.id ) WHERE persona.correo = ? and persona.estado = 1");
    }
}