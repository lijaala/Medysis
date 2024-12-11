package Medysis.Project;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // Password encoder bean
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Disabling CSRF
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/register","/login", "/css/**", "/js/**", "/image/**","/api/**","/home").permitAll()  // Allow public access
                        .anyRequest().authenticated()  // Protect other endpoints
                )
                .formLogin(form -> form
                        .loginPage("/login")  // Custom login page
                        .permitAll()  // Allow everyone to access the login page
                        .defaultSuccessUrl("/home", true)  // Redirect after successful login
                        .failureUrl("/login?error=true")
                )
                .logout(logout -> logout.permitAll());  // Allow everyone to access logout

        return http.build();
    }
}
