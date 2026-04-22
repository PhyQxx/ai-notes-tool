package com.ainotes.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Security Configuration
 *
 * @author AI Notes Team
 * @since 1.0.0
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final XssFilter xssFilter;
    private final RateLimitFilter rateLimitFilter;
    private final CsrfFilter csrfFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(request -> {
                    var config = new org.springframework.web.cors.CorsConfiguration();
                    config.setAllowedOriginPatterns(java.util.List.of("*"));
                    config.setAllowedMethods(java.util.List.of("GET","POST","PUT","DELETE","OPTIONS"));
                    config.setAllowedHeaders(java.util.List.of("*"));
                    config.setAllowCredentials(true);
                    config.setExposedHeaders(java.util.List.of("X-New-Token"));
                    return config;
                }))
                .authorizeHttpRequests(authorize -> authorize
                        // 放行认证相关接口
                        .requestMatchers("/auth/**").permitAll()
                        // 放行Swagger文档
                        .requestMatchers("/doc.html", "/swagger-resources/**", "/webjars/**", "/v3/api-docs/**", "/swagger-ui/**").permitAll()
                        // 放行静态资源
                        .requestMatchers("/static/**", "/uploads/**").permitAll()
                        // 放行WebSocket
                        .requestMatchers("/ws/**").permitAll()
                        // 放行公开分享
                        .requestMatchers("/share/**").permitAll()
                        // 放行CSRF Token获取
                        .requestMatchers("/csrf/**").permitAll()
                        // 放行AI供应商公开接口（无需登录即可查看可用模型）
                        .requestMatchers("/ai/providers").permitAll()
                        .requestMatchers("/ai/models").permitAll()
                        .requestMatchers("/ai/config/test").permitAll()
                        // 放行CORS预检
                        .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()
                        // 其他请求需要认证
                        .anyRequest().authenticated()
                )
                // 添加JWT过滤器
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                // 添加安全过滤器
                .addFilterBefore(xssFilter, JwtAuthenticationFilter.class)
                .addFilterBefore(rateLimitFilter, XssFilter.class)
                .addFilterBefore(csrfFilter, RateLimitFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
