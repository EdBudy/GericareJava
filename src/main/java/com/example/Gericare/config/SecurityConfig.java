package com.example.Gericare.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        // PERMISOS PÚBLICOS (Todos pueden acceder)
                        .requestMatchers("/", "/login", "/registro", "/css/**", "/js/**").permitAll()

                        // PERMISOS PARA ADMINISTRADOR
                        .requestMatchers("/admin/**", "/pacientes/nuevo", "/usuarios/nuevo-empleado").hasRole("ADMIN")

                        // PERMISOS PARA CUIDADOR
                        .requestMatchers("/cuidador/**").hasRole("CUIDADOR")

                        // PERMISOS PARA FAMILIAR
                        .requestMatchers("/familiar/**").hasRole("FAMILIAR")

                        // CUALQUIER OTRA PETICIÓN REQUIERE AUTENTICACIÓN
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/dashboard", true) // URL a la que ir después de un login exitoso
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                );

        return http.build();
    }
}