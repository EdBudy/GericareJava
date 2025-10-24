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
        return new BCryptPasswordEncoder(); // BCryptPasswordEncoder para que las contraseñas en la bd se guarden hasheadas
    }

    @Bean // Spring detecta este objeto y lo usa automáticamente como la configuración de seguridad
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Configuración de autorización de peticiones HTTP
                .authorizeHttpRequests(authorize -> authorize
                        // Permisos públicos (no requieren login)
                        .requestMatchers("/", "/login", "/registro", "/reset-password", "/css/**", "/js/**", "/images/**")
                        .permitAll()

                        // Reglas cuidador
                        .requestMatchers(HttpMethod.POST, "/actividades/completar/**").hasRole("Cuidador")

                        // Ver lista actividad (cuidador)
                        .requestMatchers("/actividades/actividades-pacientes").hasRole("Cuidador")

                        // Reglas admin
                        .requestMatchers("/usuarios/**", "/pacientes/**", "/actividades/**", "/admin/correos/**")
                        .hasRole("Administrador")

                        // Vistas cuidador
                        .requestMatchers("/cuidador/**").hasRole("Cuidador")
                        // Vistas familiar
                        .requestMatchers("/familiar/**").hasRole("Familiar")

                        // Las demás rutas requieren estar autenticado (logueado)
                        .anyRequest().authenticated())
                // Config del login
                .formLogin(form -> form
                        .loginPage("/login") // Se le dice a Spring cuál es la pag de login
                        .defaultSuccessUrl("/dashboard", true) // Redirigir si el login es exitoso
                        .permitAll())
                // Config del logout
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout") // Redirige a login después de cerrar sesión
                        .permitAll());

        // Devolver el filtro de seguridad construido
        return http.build();
    }
}