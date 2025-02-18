package com.demoproject.configuration;

import com.demoproject.jwt.JwtFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) //
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/register","/login","/changepw","/api/logout","api/user/**","/api/auth/**","/api/**", "/css/**", "/js/**", "/images/**").permitAll() // Cho phép truy cập trang đăng ký
                        .requestMatchers("/account/listOwner").hasAuthority("ADMIN")
                        .requestMatchers(("/account/listStaff")).hasAuthority("OWNER")
                        .requestMatchers("/account/**").hasAnyAuthority("ADMIN", "OWNER")
                        .requestMatchers("/product/**","/customer/**").hasAnyAuthority("STAFF", "OWNER")
                        .anyRequest().authenticated()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login.html")
                        .permitAll()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)

                .csrf(csrf -> csrf.disable()); // Tắt CSRF nếu không cần

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);

        AuthenticationManager authManager = new ProviderManager(List.of(authProvider));

        return authentication -> {
            try {
                String username = authentication.getName();
                String rawPassword = authentication.getCredentials().toString();

                logger.info("🔑 Mật khẩu nhập vào: " + rawPassword);

                Authentication authResult = authManager.authenticate(new UsernamePasswordAuthenticationToken(username, rawPassword));

                logger.info("✅ Xác thực thành công!");
                return authResult;
            } catch (AuthenticationException e) {
                logger.error("❌ Xác thực thất bại: " + e.getMessage());
                throw e;
            }
        };
    }
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOrigin("*"); // ✅ Cho phép tất cả origin
        configuration.addAllowedMethod("*"); // ✅ Cho phép tất cả phương thức HTTP (GET, POST, PUT, DELETE)
        configuration.addAllowedHeader("*"); // ✅ Cho phép tất cả header
        configuration.addExposedHeader("Authorization"); // ✅ Expose Authorization Header

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

}
