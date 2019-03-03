/**
 * 
 */
package com.conversationkit.impl;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Formatter;
import java.util.Scanner;
import java.util.logging.Logger;

import com.conversationkit.builder.JsonGraphBuilder;
import com.conversationkit.builder.TestCaseUserState;
import com.conversationkit.model.IConversationSnippet;
import com.conversationkit.model.IConversationSnippetButton;
import com.conversationkit.model.SnippetContentType;
import com.conversationkit.model.SnippetType;
import com.conversationkit.model.UnexpectedResponseException;
import com.conversationkit.model.UnmatchedResponseException;

/**
 * @author Prashanth_Meka
 *
 */
public class TicketBookingTest {

    private static final Logger logger = Logger.getLogger(TicketBookingTest.class.getName());
    
    Scanner scanner = new Scanner(System.in);
    
    public void endConversation() {
    	scanner.close();
    }
    
    @SuppressWarnings("rawtypes")
	public void startConversation() throws IOException {
        logger.info("** Initializing Templated Regex / JavaScript Conversation for testing");
        JsonGraphBuilder<TestCaseUserState> builder = new JsonGraphBuilder<>();
        Reader reader = new InputStreamReader(TicketBookingTest.class.getResourceAsStream("/ticket-booking-nlp.json"));
        DirectedConversationEngine<TestCaseUserState> tree = builder.readJsonGraph(reader);
    	
        TestCaseUserState state = new TestCaseUserState();
        state.setCurrentNodeId(1);
        
        StringBuilder convo = new StringBuilder();
        Formatter formatter = new Formatter(convo);
        convo.append("\n");
        
        Iterable<IConversationSnippet<TestCaseUserState>> nodes = tree.startConversationFromState(state);        
        for (IConversationSnippet node : nodes) {
            OutputUtil.formatSnippet(formatter, node, state);
        }
        System.out.println(formatter.toString());
        String userResponse = null;
        while(scanner.hasNextLine()) {
        	userResponse = scanner.nextLine();
        	if("Quit".equalsIgnoreCase(userResponse) || "Exit".equalsIgnoreCase(userResponse)) {
        		break;
        	}

        	try {
                tree.updateStateWithResponse(state, userResponse);
            } catch (UnmatchedResponseException | UnexpectedResponseException e) {
            	OutputUtil.formatSnippet(formatter, new IConversationSnippet<TestCaseUserState>(){

                    public String renderContent(TestCaseUserState state) {
                    	convo.setLength(0);
                        return "I'm sorry, I didn't understand your response '"+state.getMostRecentResponse()+"'.";
                    }

                    public SnippetType getType() {
                        return SnippetType.STATEMENT;
                    }

                    public Iterable<String> getSuggestedResponses(TestCaseUserState state) {
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
            }
            System.out.println("Current State "+ state);
        	nodes = tree.startConversationFromState(state);        
            for (IConversationSnippet node : nodes) {
                OutputUtil.formatSnippet(formatter, node, state);
            }        	
        	System.out.println(formatter.toString());
        	convo.setLength(0);
        }

    }

    /**
	 * @param args
     * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		TicketBookingTest test = new TicketBookingTest();
		test.startConversation();
		test.endConversation();

	}

}
