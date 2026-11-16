package com.askroot.config;

import com.askroot.security.*;
import org.springframework.context.annotation.*;
import org.springframework.security.authentication.*;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.*;
import java.util.List;

@Configuration @EnableMethodSecurity
public class SecurityConfig {

    @Bean PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(12); }

    @Bean CorsConfigurationSource corsSource(AskRootProperties p) {
        var c = new CorsConfiguration();
        c.setAllowedOrigins(List.of(p.getCors().getOrigins().split(",")));
        c.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
        c.setAllowedHeaders(List.of("Authorization","Content-Type","X-Requested-With"));
        c.setAllowCredentials(true);
        var s = new UrlBasedCorsConfigurationSource();
        s.registerCorsConfiguration("/api/**", c);
        return s;
    }

    @Bean SecurityFilterChain chain(HttpSecurity http, JwtAuthFilter jwt, RateLimitFilter rl,
                                    DeviceKeyFilter dk, SecurityConfigDeps deps) throws Exception {
        return http
            .csrf(c -> c.disable())                       // stateless + Bearer tokens
            .cors(c -> c.configurationSource(deps.cors()))
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(a -> a
                .requestMatchers("/api/auth/**", "/actuator/health").permitAll()
                .requestMatchers("/api/agent/**").hasRole("AGENT")
                .requestMatchers("/api/**").authenticated()
                .anyRequest().denyAll())
            .addFilterBefore(rl, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(dk, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(jwt, UsernamePasswordAuthenticationFilter.class)
            .headers(h -> h
                .contentSecurityPolicy(c -> c.policyDirectives("default-src 'self'"))
                .frameOptions(f -> f.deny()))
            .build();
    }
}
