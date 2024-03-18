package com.yi.spring.config;

import com.yi.spring.OAuth2.OAuth2MemberService;
import jakarta.servlet.DispatcherType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@PropertySource("classpath:application-oauth.properties")
public class SpringSecurityConfig {
    @Autowired
    OAuth2MemberService memberService;
    @Autowired
    MyAuthenticationFailureHandler myAuthenticationFailureHandler;
    @Autowired
    CustomAccessDeniedHandler customAccessDeniedHandler;
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf().disable().cors().disable()
                .authorizeHttpRequests(request->request
                        .dispatcherTypeMatchers(DispatcherType.FORWARD).permitAll()
                        .requestMatchers("/status","/images/**","/css/**","/js/**").permitAll()
                        .requestMatchers( "/", "/home", "/homeSlide","/signUp","/join").permitAll()
                        .requestMatchers("/user/**").authenticated()
//                        .requestMatchers("/user/**").hasAnyAuthority("USER", "OWNER", "ADMIN", "OAUTH2_USER", "oauth2_user", "SCOPE_openid", "kakao_account")
                        .requestMatchers("/QA/**").authenticated()
//                        .requestMatchers("/QA/**").hasAnyRole("USER", "OWNER", "ADMIN")
                        .requestMatchers("/owner/**").hasAnyRole("OWNER", "ADMIN")
                        .requestMatchers("/manager/**").hasRole("ADMIN")
//                        .anyRequest().authenticated()
                        .anyRequest().permitAll()
                ).formLogin(login->login
                        .loginPage("/login")
                        .loginProcessingUrl("/login-process")
                        .usernameParameter("userId")
                        .passwordParameter("userPassword")
                        .failureHandler( myAuthenticationFailureHandler )
                        .defaultSuccessUrl("/home", false)
                        .permitAll()
                ).oauth2Login( configurer-> configurer
                        .loginPage("/login")
                          .userInfoEndpoint(config -> config.userService(memberService))
                        .redirectionEndpoint(Customizer.withDefaults())
                )
                .logout((logout)->logout
                    .logoutSuccessUrl("/"))
                .exceptionHandling().accessDeniedPage("/err/user/forbidden")
                .accessDeniedHandler( customAccessDeniedHandler )
        ;

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
//        return new RawPasswordEncoder();
    }
}
