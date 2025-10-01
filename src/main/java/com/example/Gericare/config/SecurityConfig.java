// edbudy/gericarejava/GericareJava-77ecd4243c43f842b16ab915993a9b87d440c2ea/src/main/java/com/example/Gericare/config/SecurityConfig.java
package com.example.Gericare.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        // Permisos públicos
                        .requestMatchers("/", "/login", "/registro", "/css/**", "/js/**", "/images/**")
                        .permitAll()

                        // Reglas cuidador
                        .requestMatchers(HttpMethod.POST, "/actividades/completar/**").hasRole("Cuidador")

                        // Ver lista actividad
                        .requestMatchers("/actividades/actividades-pacientes").hasRole("Cuidador")

                        // Reglas admin
                        .requestMatchers("/usuarios/**", "/pacientes/**", "/actividades/**")
                        .hasRole("Administrador")

                        // Permisos para las otras vistas de cuidador y familiar
                        .requestMatchers("/cuidador/**").hasRole("Cuidador")
                        .requestMatchers("/familiar/**").hasRole("Familiar")

                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/dashboard", true)
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll());

        return http.build();
    }
}