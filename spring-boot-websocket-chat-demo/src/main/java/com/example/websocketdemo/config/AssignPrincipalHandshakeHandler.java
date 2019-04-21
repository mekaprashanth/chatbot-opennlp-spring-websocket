package com.example.websocketdemo.config;

import java.security.Principal;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.apache.commons.text.CharacterPredicates;
import org.apache.commons.text.RandomStringGenerator;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

/**
 * Assign a random username as principal for each websocket client.
 * This is needed to be able to communicate with a specific client.
 */
public class AssignPrincipalHandshakeHandler extends DefaultHandshakeHandler {
    public static final String ATTR_PRINCIPAL = "__principal__";
    public static final String REQ_SESSION = "__requestSession__";

    @Override
    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler,
                                      Map<String, Object> attributes) {
    	
    	ServletServerHttpRequest request1 = ((ServletServerHttpRequest)request);
    	final HttpSession session = request1.getServletRequest().getSession(true);
//    	final String sessionId = SimpMessageHeaderAccessor.getSessionId(attributes);
//    	System.out.println("sessionId in AssignPrincipalHandshakeHandler "+sessionId);
    	String name;
        if (!attributes.containsKey(REQ_SESSION)) {
            name = session.getId();
            attributes.put(REQ_SESSION,session);
        }
        else {
            name = (String)attributes.get(REQ_SESSION);
        }
        return new Principal() {
            @Override
            public String getName() {
                return name;
            }
        };
    }

    private String generateRandomUsername() {
        RandomStringGenerator randomStringGenerator =
            new RandomStringGenerator.Builder()
                .withinRange('0', 'z')
                .filteredBy(CharacterPredicates.LETTERS, CharacterPredicates.DIGITS)
                .build();
        return randomStringGenerator.generate(32);
    }
}