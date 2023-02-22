package com.project.stress_traffic_system.config;

import com.project.stress_traffic_system.jwt.JwtAuthFilter;
import com.project.stress_traffic_system.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;


@Configuration
@RequiredArgsConstructor
@EnableWebSecurity // 스프링 Security 지원을 가능하게 함
public class WebSecurityConfig {

    private static final List<String> permitOrigin =
            List.of("http://localhost:3000","http://localhost:8080","http://sts-system.vercel.app","http://sts-system-front-product.vercel.app"
            ,"https://sts-system.vercel.app", "https://sts-system-front-product.vercel.app");

    /**
     * BCrypt로 패스워드 인코딩 수행<br>
     * PasswordEncoder가 불려지면 이제 BCryptPasswordEncoder가 반환됨<br>
     * @return BCryptPasswordEncoder()
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        //BCrypt는 강력한 해시 메커니즘 중 하나.
        //BCrypt를 사용하는 이유가 SHA방식이 보안에 결함에 있는 것은 아니고,
        //BCrypt는 Blowfish라는 것을 통해 구현해서 연산 속도를 늦춰 Brute-force 공격에 대비하는데 효과적이기 때문

        //Scrypt라는 brute force 공격에 대해 더 강력함을 가지는 메커니즘도 있지만 이건 많은 메모리와 cpu를 사용함
        //보안 시스템을 구현하는데 많은 비용을 투자할 수 있다면 Scrypt를 사용하면 됨
        return new BCryptPasswordEncoder();
    }

    private final JwtUtil jwtUtil;


    //h2 console 사용을 위해 h2관련해서 보안 검사를 건너뛰도록 함
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring()

//                .requestMatchers(PathRequest.toH2Console())
                .requestMatchers(PathRequest.toStaticResources().atCommonLocations());
    }

    private static final String[] PERMIT_URL_ARRAY = {
            /* swagger v2 */
            "/v2/api-docs",
            "/swagger-resources",
            "/swagger-resources/**",
            "/configuration/ui",
            "/configuration/security",
            "/swagger-ui.html",
            "/webjars/**",
            /* swagger v3 */
            "/v3/api-docs/**",
            "/swagger-ui/**"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        //CSRF(Cross-Site Request Forgery) 보호를 비활성화
        http.csrf().disable();

        // 기본 설정인 Session 방식은 사용하지 않고 JWT 방식을 사용하기 위한 설정
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        //cors 설정
        http.cors().configurationSource(configurationSource());

        //다양한 엔드포인트 패턴에 대한 요청을 승인
        http.authorizeRequests()
                //추후 api들에 맞춰 permit 여부 결정
                .antMatchers("/api/members/**").permitAll()
                .antMatchers("/api/products/**").permitAll()
                .antMatchers("/signup").permitAll()
                .antMatchers("/login").permitAll()
                .antMatchers("/main").permitAll()

                //swagger 관련해서 인증 통과
                .antMatchers(PERMIT_URL_ARRAY).permitAll()

                //다른 것들은 전부 인증을 받아야 한다.
                .anyRequest().authenticated()
                // JWT 인증/인가를 사용하기 위한 설정
                // addFilterBefore -> 1번째 인자값의 필터가 2번째 인자값의 필터를 수행하기 전에 실행됨.
                // 즉, Jwt필터를 이용해 일단 토큰이 제대로 되어 있는 지부터 확인한다.
                .and().addFilterBefore(new JwtAuthFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class);

        //http 보안 설정을 위 내용으로 해서 반환
        return http.build();
    }

    @Bean
    public CorsConfigurationSource configurationSource() {
        UrlBasedCorsConfigurationSource corsConfigurationSource = new UrlBasedCorsConfigurationSource();

        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(permitOrigin);
        config.addAllowedMethod("*");

        config.addAllowedHeader("Authorization");
        config.addAllowedHeader("Content-Type");

        config.addExposedHeader("Authorization");
        config.addExposedHeader("Content-Type");
        config.setAllowCredentials(true);

        corsConfigurationSource.registerCorsConfiguration("/**", config);

        return corsConfigurationSource;
    }
}
