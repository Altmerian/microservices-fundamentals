package com.pshakhlovich.microservices_fundamentals.storage.config;


import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@EnableWebSecurity
public class ResourceServerSecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .mvcMatcher("/storages/**")
                .authorizeRequests()
                .mvcMatchers(HttpMethod.GET, "/storages/**").hasAnyAuthority("SCOPE_storages.read", "SCOPE_storages.write")
                .mvcMatchers(HttpMethod.PUT, "/storages/**").hasAuthority("SCOPE_storages.write")
                .mvcMatchers(HttpMethod.DELETE, "/storages/**").hasAuthority("SCOPE_storages.write")
                .and()
                .oauth2ResourceServer()
                .jwt();
        return http.build();
    }
}
