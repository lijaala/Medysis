package Medysis.Project;

import Medysis.Project.Service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
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
    public UserDetailsService userDetailsService(CustomUserDetailsService customUserDetailsService) {
        return customUserDetailsService;
    }

    @Bean
    public AuthenticationManager authenticationManager(UserDetailsService userDetailsService) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return new ProviderManager(provider);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/forgot-password","/forgotPassword","/api/auth/reset-password").permitAll()
                        .requestMatchers("/register", "/login", "/css/**", "/js/**", "appointment/availableSlots","/image/**", "api/auth/**","appointment/fetchDoctors").permitAll()
                        .requestMatchers("/home","api/staff/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_DOCTOR", "ROLE_LAB TECHNICIAN")
                        .requestMatchers("api/LabOrder/getByUserId").hasAnyAuthority("ROLE_PATIENTS", "ROLE_DOCTOR", "ROLE_LAB TECHNICIAN")
                        .requestMatchers("appointment/list","appointment/admin/book","appointment/edit","api/user/all").hasAnyAuthority("ROLE_ADMIN", "ROLE_DOCTOR")
                        .requestMatchers("/home","api/admin/**","api/dashboard/admin","/api/user/update/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers("api/LabOrder/orderRequest","api/LabOrder/details/","api/LabOrder/labResults", "api/labTests/availableTests").hasAnyAuthority("ROLE_DOCTOR","ROLE_LAB TECHNICIAN")
                        .requestMatchers("/home","appointment/**","api/dashboard/doctor","api/medicalRecords/saveDiagnosis","api/prescriptions/**","api/staff").hasAuthority("ROLE_DOCTOR")
                        .requestMatchers("api/medicalRecords/getByUserId","api/medicalRecords/updateStatus","api/prescriptions/getByUserId","api/prescriptions/getByAppointmentID").hasAnyAuthority("ROLE_DOCTOR", "ROLE_PATIENTS")

                        .requestMatchers("/home","api/dashboard/lab-tech", "api/LabOrder/**","api/labTests/**").hasAuthority("ROLE_LAB TECHNICIAN")
                        .requestMatchers("api/medicalRecords/history","/userHome").hasAuthority("ROLE_PATIENTS")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .permitAll()
                        .defaultSuccessUrl("/home", true)
                        .failureUrl("/login?error=true")
                )
                .logout(logout -> logout.logoutUrl("/api/auth/logout"))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED) // Use session authentication
                );

        return http.build();
    }

}

