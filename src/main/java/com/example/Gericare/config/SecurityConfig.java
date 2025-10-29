package com.example.Gericare.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
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

                        // historias clinicas
                        // Ver HC: Admin y Cuidador
                        .requestMatchers(HttpMethod.GET, "/historias-clinicas/paciente/**").hasAnyRole("Administrador", "Cuidador")
                        // Editar HC: Solo Admin
                        .requestMatchers(HttpMethod.GET, "/historias-clinicas/editar/paciente/**").hasRole("Administrador")
                        .requestMatchers(HttpMethod.POST, "/historias-clinicas/editar/**").hasRole("Administrador")
                        // Solicitudes (Familiar)
                        .requestMatchers("/solicitudes/mis-solicitudes", "/solicitudes/nueva").hasRole("Familiar")
                        .requestMatchers(HttpMethod.POST, "/solicitudes/crear").hasRole("Familiar")
                        // Solicitudes (Admin)
                        .requestMatchers("/solicitudes/admin/**").hasRole("Administrador")
                        // Solicitudes (Eliminar controlado en Controller Service)
                        .requestMatchers(HttpMethod.POST, "/solicitudes/eliminar/**").hasAnyRole("Familiar", "Administrador")

                        // Tratamientos (Admin)
                        .requestMatchers("/tratamientos/admin/**", "/tratamientos/nuevo").hasRole("Administrador")
                        .requestMatchers(HttpMethod.POST, "/tratamientos/crear", "/tratamientos/eliminar/**").hasRole("Administrador")
                        // Tratamientos (Cuidador)
                        .requestMatchers("/tratamientos/mis-tratamientos/**").hasRole("Cuidador")
                        .requestMatchers(HttpMethod.POST, "/tratamientos/completar/**").hasRole("Cuidador")
                        // Tratamientos (Familiar solo lectura)
                        .requestMatchers("/tratamientos/paciente/**").hasRole("Familiar")
                        // Tratamientos (Editar controlado en Controller Service)
                        .requestMatchers("/tratamientos/editar/**").hasAnyRole("Administrador", "Cuidador") // GET para mostrar form
                        .requestMatchers(HttpMethod.POST, "/tratamientos/actualizar/**").hasAnyRole("Administrador", "Cuidador") // POST para guardar

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
                //Historia Clinica


        // Devolver el filtro de seguridad construido
        return http.build();
    }
}