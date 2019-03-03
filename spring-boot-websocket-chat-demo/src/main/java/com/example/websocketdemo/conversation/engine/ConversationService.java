/**
 * 
 */
package com.example.websocketdemo.conversation.engine;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Formatter;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.conversationkit.builder.JsonGraphBuilder;
import com.conversationkit.builder.JsonGraphBuilderV1;
import com.conversationkit.builder.TestCaseUserState;
import com.conversationkit.impl.DirectedConversationEngine;
import com.conversationkit.impl.OutputUtil;
import com.conversationkit.model.IConversationSnippet;
import com.conversationkit.model.IConversationSnippetButton;
import com.conversationkit.model.IConversationState;
import com.conversationkit.model.SnippetContentType;
import com.conversationkit.model.SnippetType;
import com.conversationkit.model.UnexpectedResponseException;
import com.conversationkit.model.UnmatchedResponseException;
import com.example.websocketdemo.model.ChatMessage;

/**
 * @author Prashanth_Meka
 *
 */

@Service
public class ConversationService<S extends IConversationState<String, Object>> {

	DirectedConversationEngine<S> engine;

	public ConversationService() {
		JsonGraphBuilder<S> builder = new JsonGraphBuilderV1<>();
		Reader reader = new InputStreamReader(
				ConversationService.class.getResourceAsStream("/conversation/config/ticket-booking-nlp.json"));
		try {
			engine = builder.readJsonGraph(reader);
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	public void createConversation(ChatMessage chatMessage, ChatMessage serverMessage,
			Map<String, Object> sessionAtributes) {

		S state = HttpSessionBackedState.buildFromAttributes(sessionAtributes);
		state.setCurrentNodeId(1);

		Iterable<IConversationSnippet<S>> nodes = engine.startConversationFromState(state);
		StringBuilder convo = new StringBuilder();
		Formatter formatter = new Formatter(convo);

		convo.append("\n");
		for (IConversationSnippet<S> node : nodes) {
			OutputUtil.formatSnippetPlain(formatter, node, state);
			convo.append("\n");
		}
		System.out.println("formatter.toString() " + formatter.toString());
		serverMessage.setContent(formatter.toString());
	}

	public void continueConversation(ChatMessage chatMessage, ChatMessage serverMessage,
			Map<String, Object> sessionAtributes) {

		S state = HttpSessionBackedState.buildFromAttributes(sessionAtributes);

		StringBuilder convo = new StringBuilder();
		Formatter formatter = new Formatter(convo);

		convo.append("\n");
		try {
			engine.updateStateWithResponse(state, chatMessage.getContent());
		} catch (UnmatchedResponseException e) {
			OutputUtil.formatSnippetPlain(formatter, new IConversationSnippet<HttpSessionBackedState>() {

				public String renderContent(HttpSessionBackedState state) {
					return "I'm sorry, I didn't understand your response '" + state.getMostRecentResponse() + "'.";
				}

				public SnippetType getType() {
					return SnippetType.STATEMENT;
				}

				public Iterable<String> getSuggestedResponses(HttpSessionBackedState state) {
					return null;
				}

				public SnippetContentType getContentType() {
					return SnippetContentType.TEXT;
				}

				@Override
				public Iterable<IConversationSnippetButton> getButtons() {
					return null;
				}

			}, state);
		} catch (UnexpectedResponseException e) {
			formatter.close();
			throw new RuntimeException(e.getMessage());
		} finally {
		}

		Iterable<IConversationSnippet<S>> nodes = engine.startConversationFromState(state);
		for (IConversationSnippet<S> node : nodes) {
			OutputUtil.formatSnippetPlain(formatter, node, state);
			convo.append("\n");
		}
		System.out.println("formatter.toString() " + formatter.toString());
		serverMessage.setContent(formatter.toString());
		formatter.close();
	}

}
