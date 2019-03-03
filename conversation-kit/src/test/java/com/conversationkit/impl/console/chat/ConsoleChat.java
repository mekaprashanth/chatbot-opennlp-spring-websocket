package com.conversationkit.impl.console.chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.conversationkit.builder.JsonGraphBuilder;
import com.conversationkit.impl.DirectedConversationEngine;
import com.conversationkit.impl.MapBackedState;
import com.conversationkit.model.IConversationSnippet;
import com.conversationkit.model.IConversationSnippetButton;
import com.conversationkit.model.IConversationState;
import com.conversationkit.model.SnippetContentType;
import com.conversationkit.model.SnippetType;
import com.conversationkit.model.UnexpectedResponseException;
import com.conversationkit.model.UnmatchedResponseException;

public class ConsoleChat {

    private static class BasicUnmatchedSnippet<S extends IConversationState<String, Object>> implements IConversationSnippet<S> {

        @Override
        public String renderContent(S state) {
            return "I'm sorry, I didn't understand your response '" + state.getMostRecentResponse() + "'.";
        }

        @Override
        public SnippetType getType() {
            return SnippetType.STATEMENT;
        }

        @Override
        public Iterable<String> getSuggestedResponses(S state) {
            return null;
        }

        @Override
        public SnippetContentType getContentType() {
            return SnippetContentType.TEXT;
        }

		@Override
		public Iterable<IConversationSnippetButton> getButtons() {
			// TODO Auto-generated method stub
			return null;
		}

    }

    public static void main(String[] args) {

        Logger logbackRoot = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
        logbackRoot.setLevel(Level.INFO);
        
        System.setProperty("java.util.logging.SimpleFormatter.format","  %4$s: %5$s%6$s%n");


        BufferedReader br = null;

        BasicUnmatchedSnippet<MapBackedState> unmatchedSnippet = new BasicUnmatchedSnippet();

        try {

            JsonGraphBuilder<MapBackedState> builder = new JsonGraphBuilder();
            Reader reader = new InputStreamReader(ConsoleChat.class.getResourceAsStream("/conversation.json"));
            DirectedConversationEngine<MapBackedState> tree = builder.readJsonGraph(reader);
            MapBackedState state = new MapBackedState();
            String[] possibleSymptoms = {"Sneezing", "Itchy Eyes", "Runny Nose"};
            state.set("name", "Daniel");
            state.set("possibleSymptoms", possibleSymptoms);
            state.setCurrentNodeId(1);

            br = new BufferedReader(new InputStreamReader(System.in));

            while (true) {
                Iterable<IConversationSnippet<MapBackedState>> nodes = tree.startConversationFromState(state);

                for (IConversationSnippet node : nodes) {
                    System.out.println("$ " + node.renderContent(state));
                    if ((node.getType() == SnippetType.QUESTION) && (node.getSuggestedResponses(state) != null)) {
                        System.out.println("$ " + "[ " + String.join(" | ", node.getSuggestedResponses(state)) + " ]");
                    }

                }

                System.out.print("> ");
                String response = br.readLine();
                try {
                    tree.updateStateWithResponse(state, response);
                } catch (UnmatchedResponseException | UnexpectedResponseException e) {
                    if ("quit".equals(response)) {
                        System.exit(0);
                    } else {
                        System.out.println("$ " + unmatchedSnippet.renderContent(state));
                    }
                }

            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }
}