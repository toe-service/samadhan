package com.samadhan.config;

import com.samadhan.security.JwtAccessDeniedHandler;
import com.samadhan.security.JwtAuthenticationEntryPoint;
import com.samadhan.security.JwtAuthenticationFilter;
import com.samadhan.security.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Autowired
    private JwtAccessDeniedHandler jwtAccessDeniedHandler;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(customUserDetailsService)
                .passwordEncoder(passwordEncoder());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .cors()
                .and()
                .csrf()
                .disable()
                .exceptionHandling()
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                .accessDeniedHandler(jwtAccessDeniedHandler)
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                // Public endpoints
                .antMatchers("/gaadi-dikhao/status").permitAll()
                .antMatchers("/gaadi-dikhao/login").permitAll()
                .antMatchers("/gaadi-dikhao/verify-token").permitAll()
                .antMatchers(HttpMethod.POST, "/v1/send-otp").permitAll()
                .antMatchers(HttpMethod.POST, "/v1/user-otp-verify").permitAll()
                .antMatchers(HttpMethod.POST, "/v1/user-register").permitAll()
                .antMatchers(HttpMethod.POST, "/v1/transfervendor-login").permitAll()
                .antMatchers(HttpMethod.POST, "/v1/role-login").permitAll()
                .antMatchers(HttpMethod.GET, "/location/search").permitAll()
                .antMatchers(HttpMethod.GET, "/location/source/search").permitAll()
                .antMatchers(HttpMethod.GET, "/location/latlong").permitAll()
                .antMatchers(HttpMethod.GET, "/pay/bookVehicleCostList").permitAll()
                .antMatchers(HttpMethod.GET, "/pay/rideCostCalculation").permitAll()
                .antMatchers(HttpMethod.POST, "/transfer/requestRideTransfer").permitAll()
               // .antMatchers(HttpMethod.GET, "/transfer/rideCostCalculation").permitAll()
                .antMatchers("/health").permitAll()
                .antMatchers("/swagger-ui/**").permitAll()
                .antMatchers("/v3/api-docs/**").permitAll()
                // Protected endpoints
                .antMatchers("/v1/**").authenticated()
                .antMatchers("/ride/**").authenticated()
                .antMatchers("/vehicle/**").authenticated()
                .anyRequest().authenticated()
                .and()
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
    }
}
