package com.example.websocketdemo.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Created by rajeevkumarsingh on 24/07/17.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfigNoSession implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
        .setHandshakeHandler(new AssignPrincipalHandshakeHandler())
//        .addInterceptors(new HttpHandshakeInterceptor())
        
        .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes("/app");
        registry.enableSimpleBroker("/topic", "/queue");    //Enables a simple in-memory broker
        registry.setUserDestinationPrefix("/user");
    }
    
    @Override
   	public void configureClientInboundChannel(ChannelRegistration registration) {
   		registration.interceptors(new ChannelInterceptor() {

   			@Override
   			public Message<?> preSend(Message<?> message, MessageChannel channel) {

   				StompHeaderAccessor accessor =
   						MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

   				if (StompCommand.CONNECT.equals(accessor.getCommand())) {
   					String user = accessor.getFirstNativeHeader("user");
   					if (!StringUtils.isEmpty(user)) {
   						List<GrantedAuthority> authorities = new ArrayList<>();
   						authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
   						Authentication auth = new UsernamePasswordAuthenticationToken(user, user, authorities);
   						SecurityContextHolder.getContext().setAuthentication(auth);
   						accessor.setUser(auth);
   					}
   				}

   				return message;
   			}
   		});
   	}
}
