package com.example.Gericare.config;

import com.example.Gericare.Security.UsuarioDetallesServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // --- BEANS ESENCIALES ---

    // 1. Definir el encriptador de contraseñas.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 2. Definir el SecurityFilterChain (las reglas de acceso a las URLs).
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/login", "/registro", "/css/**", "/js/**").permitAll()
                        .requestMatchers("/usuarios/**", "/pacientes/**", "/asignaciones/**").hasRole("Administrador")
                        .requestMatchers("/cuidador/**").hasRole("Cuidador")
                        .requestMatchers("/familiar/**").hasRole("Familiar")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/", true) // Redirigir a la página de inicio
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                );
        return http.build();
    }

    // --- CONFIGURACIÓN DEL AUTHENTICATION MANAGER ---

    // 3. Crear el "gestor de autenticación" que une el encriptador y el servicio de usuarios.
    // Esta es la forma moderna y segura de evitar dependencias circulares.
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http, UsuarioDetallesServiceImpl userDetailsService) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder =
                http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder
                .userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder());
        return authenticationManagerBuilder.build();
    }
}