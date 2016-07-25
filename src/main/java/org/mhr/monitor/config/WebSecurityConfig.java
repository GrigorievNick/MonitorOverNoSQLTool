package org.mhr.monitor.config;


import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.header.writers.frameoptions.XFrameOptionsHeaderWriter;

/**
 * Customizes Spring Security configuration.
 */
@Configuration
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .headers()
            .addHeaderWriter(
            new XFrameOptionsHeaderWriter(
                XFrameOptionsHeaderWriter.XFrameOptionsMode.SAMEORIGIN)).and()

            .formLogin()
            .defaultSuccessUrl("/index.html")
            .loginPage("/login.html")
            .failureUrl("/login.html?error")
            .permitAll()
            .and()
            .logout()
            .logoutSuccessUrl("/login.html?logout")
            .logoutUrl("/logout.html")
            .permitAll()
            .and()
            .authorizeRequests()
            .antMatchers("/websocket/**").permitAll()
            .antMatchers("/websocket/").permitAll()
            .antMatchers("/static/**").permitAll()
            .antMatchers("/webjars/**").permitAll()
            .anyRequest().authenticated()
            .and();
    }


    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth
            .inMemoryAuthentication()
            .withUser("mhr").password("mhr").roles("USER").and()
            .withUser("admin").password("admin").roles("ADMIN", "USER");
    }
}
