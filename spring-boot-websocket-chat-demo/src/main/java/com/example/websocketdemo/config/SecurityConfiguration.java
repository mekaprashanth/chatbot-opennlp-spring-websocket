package com.example.websocketdemo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.header.writers.frameoptions.XFrameOptionsHeaderWriter;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

	@Override
	protected  void configure(HttpSecurity http) throws Exception {
		http
		.csrf().disable()  // Refactor login form

		// See https://jira.springsource.org/browse/SPR-11496
		.headers().addHeaderWriter(
			new XFrameOptionsHeaderWriter(
					XFrameOptionsHeaderWriter.XFrameOptionsMode.SAMEORIGIN)).and()

//		.formLogin()
//			.defaultSuccessUrl("/index.html")
//			.loginPage("/login.html")
//			.failureUrl("/login.html?error")
//			.permitAll()
//			.and()
//		.logout()
//			.logoutSuccessUrl("/login.html?logout")
//			.logoutUrl("/logout.html")
//			.permitAll()
//			.and()
		.authorizeRequests()
			.antMatchers("/**").permitAll()
			.antMatchers("/static/**").permitAll()
			.antMatchers("/webjars/**").permitAll()
			.anyRequest().authenticated()
//			.and()
			;
	}
	
	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth
			.inMemoryAuthentication()
				.withUser("prash").password("prash").roles("USER").and()
				.withUser("meka").password("meka").roles("ADMIN","USER");
	}
}