package com.example.Gericare.Security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
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
            // 1. Deshabilitar CSRF para que los formularios de reset no reboten (opcional pero recomendado en desarrollo)
            .csrf(csrf -> csrf.disable())
            
            // 2. CONFIGURACIÓN DE RUTAS PÚBLICAS (Aquí es donde ocurre la magia)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/forgot-password/**", "/reset-password/**").permitAll() // LIBERA EL RESET
                .requestMatchers("/css/**", "/js/**", "/images/**", "/vendor/**").permitAll() // LIBERA ESTÁTICOS
                .requestMatchers("/login", "/register").permitAll()
                .anyRequest().authenticated() // Todo lo demás pide login
            )

            // 3. CONFIGURACIÓN DE LOGIN
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/dashboard", true) // A donde va tras loguearse
                .failureUrl("/login?error=true")
                .permitAll()
            )

            // 4. CONFIGURACIÓN DE LOGOUT
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            );

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}
