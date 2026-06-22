// Este código es para el SecurityConfig.java de MS-SYNC
package duoc.fs3.ms_sync.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                // 1. FUNDAMENTAL PARA QUE NO DE 403 EN REACT NATIVE (CORS)
                .cors(Customizer.withDefaults()) 
                
                // 2. FUNDAMENTAL PARA QUE NO BLOQUEE LOS POST
                .csrf(csrf -> csrf.disable())
                
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Permitir acceso público a las imágenes (GET)
                        .requestMatchers(HttpMethod.GET, "/api/v1/sync/images/**").permitAll()
                        // 3. AQUÍ PERMITES TUS ENDPOINTS DE SYNC SÓLO CON ESTAR AUTENTICADO
                        .requestMatchers("/api/v1/sync/**").authenticated()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
