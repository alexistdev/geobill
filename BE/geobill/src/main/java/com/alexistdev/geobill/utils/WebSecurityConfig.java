package com.alexistdev.geobill.utils;

import com.alexistdev.geobill.models.entity.Role;
import com.alexistdev.geobill.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    @Autowired
    private UserService userService;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Bean
    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests((authz) -> authz
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/users").hasAuthority(Role.ADMIN.toString())
                        .requestMatchers(HttpMethod.PATCH,
                                "/api/v1/users").hasAuthority(Role.ADMIN.toString())
                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/producttypes").hasAnyAuthority(Role.ADMIN.toString(),Role.USER.toString())
                        .requestMatchers(HttpMethod.POST,
                                "/api/v1/producttypes").hasAuthority(Role.ADMIN.toString())
                        .requestMatchers(HttpMethod.PATCH,
                                "/api/v1/producttypes").hasAuthority(Role.ADMIN.toString())
                        .requestMatchers(HttpMethod.DELETE,
                                "/api/v1/menus").hasAuthority(Role.ADMIN.toString())
                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/products").hasAnyAuthority(Role.ADMIN.toString(),Role.USER.toString())
                        .requestMatchers(HttpMethod.POST,
                                "/api/v1/products").hasAuthority(Role.ADMIN.toString())
                        .requestMatchers(HttpMethod.PATCH,
                                "/api/v1/products").hasAuthority(Role.ADMIN.toString())
                        .requestMatchers(HttpMethod.DELETE,
                                "/api/v1/products").hasAuthority(Role.ADMIN.toString())
                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/search-by-type").hasAnyAuthority(Role.ADMIN.toString(),Role.USER.toString())
                        .requestMatchers(HttpMethod.POST,
                                "/api/v1/hosting").hasAnyAuthority(Role.ADMIN.toString(), Role.USER.toString())
                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/hosting").hasAnyAuthority(Role.ADMIN.toString(), Role.USER.toString())
                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/invoice").hasAnyAuthority(Role.ADMIN.toString(), Role.USER.toString())

                        // Ticketing: milik sendiri boleh diakses semua peran.
                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/tickets/me", "/api/v1/tickets/tabs/me")
                                .hasAnyAuthority(Role.ADMIN.toString(), Role.STAFF.toString(), Role.USER.toString())
                        .requestMatchers(HttpMethod.POST,
                                "/api/v1/tickets/auto-close").hasAuthority(Role.ADMIN.toString())
                        .requestMatchers(HttpMethod.POST,
                                "/api/v1/tickets", "/api/v1/tickets/*/replies")
                                .hasAnyAuthority(Role.ADMIN.toString(), Role.STAFF.toString(), Role.USER.toString())
                        // Detail tiket dibatasi service: klien hanya menemukan tiketnya sendiri.
                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/tickets/{id}", "/api/v1/tickets/number/*")
                                .hasAnyAuthority(Role.ADMIN.toString(), Role.STAFF.toString(), Role.USER.toString())
                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/tickets", "/api/v1/tickets/tabs", "/api/v1/tickets/assigned/me")
                                .hasAnyAuthority(Role.ADMIN.toString(), Role.STAFF.toString())
                        .requestMatchers(HttpMethod.PATCH,
                                "/api/v1/tickets/**").hasAnyAuthority(Role.ADMIN.toString(), Role.STAFF.toString())
                        .requestMatchers(HttpMethod.DELETE,
                                "/api/v1/tickets/**").hasAnyAuthority(Role.ADMIN.toString(), Role.STAFF.toString())

                        // Departemen dan template balasan: dibaca staff, diubah admin.
                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/ticket-departments/active")
                                .hasAnyAuthority(Role.ADMIN.toString(), Role.STAFF.toString(), Role.USER.toString())
                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/ticket-departments/**", "/api/v1/ticket-canned-replies/**")
                                .hasAnyAuthority(Role.ADMIN.toString(), Role.STAFF.toString())
                        .requestMatchers(HttpMethod.POST,
                                "/api/v1/ticket-departments/**", "/api/v1/ticket-canned-replies/**")
                                .hasAuthority(Role.ADMIN.toString())
                        .requestMatchers(HttpMethod.PATCH,
                                "/api/v1/ticket-departments/**", "/api/v1/ticket-canned-replies/**")
                                .hasAuthority(Role.ADMIN.toString())
                        .requestMatchers(HttpMethod.DELETE,
                                "/api/v1/ticket-departments/**", "/api/v1/ticket-canned-replies/**")
                                .hasAuthority(Role.ADMIN.toString())
                        .anyRequest().authenticated()
                )
                .httpBasic(withDefaults())
                .authenticationProvider(this.daoAuthenticationProvider());
        return http.build();
    }

    private DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(bCryptPasswordEncoder);
        provider.setUserDetailsService(userService);
        return provider;
    }
}
