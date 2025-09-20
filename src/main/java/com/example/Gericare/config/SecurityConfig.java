package com.example.Gericare.config;

import com.example.Gericare.service.UsuarioService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration // Le dice a Spring que esta es una clase de configuración
@EnableWebSecurity // Habilita la seguridad web de Spring
public class SecurityConfig {

    // 1. CREAMOS EL BEAN DEL CODIFICADOR DE CONTRASEÑAS
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 2. CREAMOS EL "PROVEEDOR DE AUTENTICACIÓN"
    // Este componente le dice a Spring Security cómo obtener los detalles del usuario
    // y cómo verificar la contraseña.
    @Bean
    public DaoAuthenticationProvider authenticationProvider(UsuarioService usuarioService) {
        DaoAuthenticationProvider auth = new DaoAuthenticationProvider();
        auth.setUserDetailsService(usuarioService); // Le decimos que use nuestro UsuarioService para buscar usuarios
        auth.setPasswordEncoder(passwordEncoder()); // Le decimos que use BCrypt para las contraseñas
        return auth;
    }

    // 3. CONFIGURAMOS LAS REGLAS DE SEGURIDAD HTTP
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        // Permitimos el acceso público a estas rutas (CSS, JS, imágenes, etc.)
                        .requestMatchers("/js/**", "/css/**", "/img/**").permitAll()
                        // Permitimos el acceso a la página de registro
                        .requestMatchers("/registro").permitAll()
                        // Cualquier otra petición debe ser autenticada
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        // La URL de nuestra página de login personalizada
                        .loginPage("/login")
                        // Permitimos que todos accedan a la página de login
                        .permitAll()
                )
                .logout(logout -> logout
                        // Habilitamos el logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                        // A dónde redirigir después del logout
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                );

        return http.build();
    }
}