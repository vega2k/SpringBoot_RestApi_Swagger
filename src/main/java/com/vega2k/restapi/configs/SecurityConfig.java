package com.vega2k.restapi.configs;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.InMemoryTokenStore;

import com.vega2k.restapi.accounts.AccountService;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
	@Autowired
	AccountService accountService;
	
	@Autowired
	PasswordEncoder passwordEncoder;
	
	@Bean
	public TokenStore tokenStore() {
		return new InMemoryTokenStore();
	}

	@Bean
	@Override
	public AuthenticationManager authenticationManagerBean() throws Exception {
		return super.authenticationManagerBean();
	}

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(accountService)
			.passwordEncoder(passwordEncoder);
	}

//	@Override
//	public void configure(WebSecurity web) throws Exception {
//		web.ignoring().mvcMatchers("/h2-console/**");
//		web.ignoring().requestMatchers(PathRequest.toStaticResources().atCommonLocations());
//	}

	/*
	 * WebSecurity에 비해서 서버가 너무 많은 일을 합니다. 
	 */
//	@Override
//	protected void configure(HttpSecurity http) throws Exception {
//		http.authorizeRequests()
//			.mvcMatchers("index.html").anonymous()
//			.requestMatchers(PathRequest.toStaticResources().atCommonLocations()).anonymous();		
//	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.anonymous()
				.and()
			.formLogin()
				.and()
			.authorizeRequests()
				//.mvcMatchers(HttpMethod.GET, "/api/**").anonymous()
				//브라우저에서 post로 요청 보내는게 어려우니까 우선 get에 인증을 걸어서 로그인이 되는지 테스트한다.
				.mvcMatchers(HttpMethod.GET, "/api/**").authenticated()
				.anyRequest().authenticated();		
	}
	
	
	
	
	
	
}
