package com.example.websocketdemo.controller;

import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.conversationkit.model.IConversationState;
import com.example.websocketdemo.config.AssignPrincipalHandshakeHandler;
import com.example.websocketdemo.conversation.engine.ConversationService;
import com.example.websocketdemo.model.ChatMessage;
import com.example.websocketdemo.model.ChatMessage.MessageType;

/**
 */
@Controller
public class ChatController<S extends IConversationState<String, Object>> {

	@Autowired
	private SimpMessagingTemplate messagingTemplate;
	
	@Autowired
	private ConversationService<S> conversationService;

	@MessageMapping("/chat.sendMessage")
	@SendTo("/topic/public")
	public ChatMessage sendMessage(@Payload ChatMessage chatMessage) {
		return chatMessage;
	}

	@MessageMapping("/chat.addUser")
	public void addUser(@Payload ChatMessage chatMessage, 
			SimpMessageHeaderAccessor headerAccessor) {
		ChatMessage serverMessage = new ChatMessage();
		serverMessage.setSender("Travel Bot");
		serverMessage.setType(MessageType.JOIN);
		System.out.println("sessionId "+headerAccessor.getSessionId());
		HttpSession session = (HttpSession) headerAccessor.getSessionAttributes().get(AssignPrincipalHandshakeHandler.REQ_SESSION);
		String principalName = session.getId();
		messagingTemplate.convertAndSendToUser(principalName, "/queue/reply", chatMessage);
		messagingTemplate.convertAndSendToUser(principalName, "/queue/reply", serverMessage);
		induceDelay();
		conversationService.createConversation(chatMessage, serverMessage, headerAccessor.getSessionAttributes());
		serverMessage.setType(MessageType.CHAT);
		dumpHeaderContent(headerAccessor);
		messagingTemplate.convertAndSendToUser(principalName, "/queue/reply", serverMessage);
	}

	private void induceDelay() {
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private void dumpHeaderContent(SimpMessageHeaderAccessor headerAccessor) {
		System.out.println("MessageHeader Content");
		headerAccessor.getMessageHeaders()
		.forEach((k,v) -> System.out.println("Key: "+k+" value: "+v));
		
		System.out.println("SessionAttributes Content");
		headerAccessor.getSessionAttributes()
		.forEach((k,v) -> System.out.println("Key: "+k+" value: "+v));

		HttpSession session = (HttpSession) headerAccessor.getSessionAttributes().get(AssignPrincipalHandshakeHandler.REQ_SESSION);
		System.out.println("Session Id "+session.getId());
	}

	@MessageMapping("/chat.toUser")
	public void sendToUser(@Payload ChatMessage chatMessage,
			SimpMessageHeaderAccessor headerAccessor) {
		System.out.println("Received new Message "+chatMessage.getContent()+" from User "+chatMessage.getSender());
		ChatMessage serverMessage = new ChatMessage();
		serverMessage.setSender("Travel Bot");
		serverMessage.setType(MessageType.REPLY);
		serverMessage.setContent(chatMessage.getContent());
		final Map<String, Object> sessionAttributes = headerAccessor.getSessionAttributes();
		HttpSession session = (HttpSession) sessionAttributes.get(AssignPrincipalHandshakeHandler.REQ_SESSION);
		String principalName = session.getId();
		messagingTemplate.convertAndSendToUser(principalName, "/queue/reply", chatMessage);
		induceDelay();
		conversationService.continueConversation(chatMessage, serverMessage, sessionAttributes);
		dumpHeaderContent(headerAccessor);
		messagingTemplate.convertAndSendToUser(principalName, "/queue/reply", serverMessage);
	}

}
