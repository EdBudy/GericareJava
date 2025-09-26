package com.example.Gericare.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // Definir el encriptador de contraseñas que usará toda la aplicación.
    // BCrypt es el estándar de la industria.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        // Permisos públicos
                        // Permitir el acceso sin necesidad de login a estas URLs.
                        // Página de inicio, login y archivos de estilo (CSS, JS).
                        .requestMatchers("/", "/login", "/registro", "/css/**", "/js/**")
                        .permitAll()

                        // Permisos admin
                        // Solo 'Administrador' podrá acceder a estas URLs.
                        // Protege todas las operaciones de creación y gestión de usuarios,
                        // pacientes y asignaciones.
                        .requestMatchers("/usuarios/**", "/pacientes/**", "/asignaciones/**")
                        .hasAuthority("Administrador")

                        // Permisos cuidador
                        // Solo 'Cuidador' podrá acceder a estas URLs.
                        .requestMatchers("/cuidador/**").hasAuthority("Cuidador")

                        // Permisos familiar
                        // Solo 'Familiar' podrá acceder a estas URLs.
                        .requestMatchers("/familiar/**").hasAuthority("Familiar")

                        /*
                         * Cualquier otra petición (URL) que no coincida con las reglas
                         * anteriores,
                         * pues tiene que ser de un usuario que haya iniciado sesión (esté
                         * autenticado).
                         */
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        // Indicar cuál es la página de login personalizada.
                        .loginPage("/login")
                        // Definir la página a la que se redirige al usuario tras un login
                        // exitoso.
                        .defaultSuccessUrl("/dashboard", true)
                        .permitAll())
                .logout(logout -> logout
                        // Definir URL que procesará el cierre de sesión.
                        .logoutUrl("/logout")
                        // A dónde redirigir al usuario después de cerrar sesión.
                        .logoutSuccessUrl("/login?logout")
                        .permitAll());

        return http.build();
    }
}