/**
 * 
 */
package com.conversationkit.builder;

import java.io.IOException;
import java.util.logging.Logger;

import com.conversationkit.impl.edge.AffirmativeEdge;
import com.conversationkit.impl.edge.DialogTreeEdge;
import com.conversationkit.impl.edge.JavaScriptEdge;
import com.conversationkit.impl.edge.NLPEvalEdge;
import com.conversationkit.impl.edge.NegativeEdge;
import com.conversationkit.impl.edge.RegexEdge;
import com.conversationkit.impl.edge.RegexEdgeV2;
import com.conversationkit.impl.edge.StatementEdge;
import com.conversationkit.impl.node.ConversationNodeButton;
import com.conversationkit.impl.node.DialogTreeNode;
import com.conversationkit.impl.node.HiddenNode;
import com.conversationkit.impl.node.JavaScriptNode;
import com.conversationkit.impl.node.ResponseSuggestingNode;
import com.conversationkit.impl.node.StringReplacingNode;
import com.conversationkit.model.IConversationEdge;
import com.conversationkit.model.IConversationNode;
import com.conversationkit.model.IConversationState;
import com.conversationkit.model.SnippetContentType;
import com.conversationkit.model.SnippetType;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;

/**
 * @author Prashanth_Meka
 *
 */
public class JsonGraphBuilderV1<S extends IConversationState<String, Object>> extends JsonGraphBuilder<S> {
    private static final Logger logger = Logger.getLogger(JsonGraphBuilderV1.class.getName());
    
    /**
     * Creates an <code>IConversationNode</code> from JSON. Override this to
     * handle additional node types. The call to
     * <code>super.nodeFromJson()</code> will return null if the node type is
     * not currently handled.
     *
     * @param id the node id
     * @param type the node type
     * @param content the value of the content key
     * @param snippetType question or statement
     * @param contentType content type to render
     * @param metadata the additional metadata in the JSON
     * @return a node or null
     * @throws IOException exception parsing JSON
     */
    @Override
    protected IConversationNode<S> nodeFromJson(Integer id, String type, String content, SnippetType snippetType, SnippetContentType contentType, JsonObject metadata) throws IOException {

        NodeType nodeType;
        try {
            nodeType = NodeType.valueOf(type);
        } catch (Exception e) {
            return null;
        }

        //make the node into something
        IConversationNode<S> conversationNode = null;

        switch (nodeType) {
            case Hidden:
                conversationNode = new HiddenNode(id, snippetType);
                break;
            case DialogTree:
                DialogTreeNode dtNode = new DialogTreeNode(id, snippetType, content);
                conversationNode = dtNode;
                break;
            case StringReplacing:
                StringReplacingNode srNode = new StringReplacingNode(id, snippetType, content);
                for (String suggestion : createSuggestionsFromMetadata(metadata)) {
                    srNode.addSuggestedResponse(suggestion);
                }
                for (ConversationNodeButton button : createButtonsFromMetadata(metadata)) {
                    srNode.addButton(button);
                }
                conversationNode = srNode;
                break;
            case ResponseSuggesting:
                ResponseSuggestingNode rsNode = new ResponseSuggestingNode(id, snippetType, content, contentType);

                for (String suggestion : createSuggestionsFromMetadata(metadata)) {
                    rsNode.addSuggestedResponse(suggestion);
                }
                for (ConversationNodeButton button : createButtonsFromMetadata(metadata)) {
                    rsNode.addButton(button);
                }

                conversationNode = rsNode;
                break;
            case JavaScript:
                JavaScriptNode jsNode = new JavaScriptNode(id, snippetType, content);

                for (String suggestion : createSuggestionsFromMetadata(metadata)) {
                	jsNode.addSuggestedResponse(suggestion);
                }
                for (ConversationNodeButton button : createButtonsFromMetadata(metadata)) {
                	jsNode.addButton(button);
                }

                conversationNode = jsNode;
                break;
            default:
                return null;
        }

        return conversationNode;
    }

    /**
     * Creates an <code>IConversationEdge</code> from JSON. Override this to
     * handle additional edge types. The call to
     * <code>super.edgeFromJson()</code> will return null if the edge type is
     * not currently handled.
     *
     * @param type the edge type
     * @param metadata the additional metadata in the JSON
     * @param target the target node
     * @return an edge or null
     * @throws IOException exception parsing JSON
     */
    @Override
    protected IConversationEdge<S> edgeFromJson(String type, JsonObject metadata, IConversationNode<S> target) throws IOException {

        EdgeType edgeType;
        
        try {
            edgeType = EdgeType.valueOf(type);
        } catch (Exception e) {
            return null;
        }
        String stateKey = null;
        String[] stateKeys = null;
        if ((metadata != null) && (metadata.get("stateKey") != null)) {
        	if (metadata.get("stateKey").isArray()) {
        		JsonArray arrayKeys = metadata.get("stateKey").asArray();
        		stateKeys = new String[arrayKeys.size()];
        		for(int j = 0; j < arrayKeys.size(); j++ ) {
        			stateKeys[j] = arrayKeys.get(j).asString();
        		}
            } else {
            	stateKey = metadata.get("stateKey").asString();
            }
        }
        Object stateValue = null;
        Object[] stateValues = null;
        if ((metadata != null) && metadata.get("stateValue") != null) {

            if (metadata.get("stateValue").isArray()) {
                stateValue = metadata.get("stateValue").asArray();
            } else if (metadata.get("stateValue").isBoolean()) {
                stateValue = metadata.get("stateValue").asBoolean();
            } else if (metadata.get("stateValue").isNumber()) {
                stateValue = metadata.get("stateValue").asInt();
            } else {
                stateValue = metadata.get("stateValue").asString();
            }
        }
        String pattern = null;
        switch (edgeType) {
            case DialogTree:
                if ((metadata == null) || (metadata.get("answer") == null)) {
                    throw new IOException("DialogTreeEdge missing \"answer\" metadata key: " + metadata);
                }
                DialogTreeEdge dte = new DialogTreeEdge(metadata.get("answer").asString(), stateKey, target);
                return dte;
            case JavaScript:

                if ((metadata == null) || (metadata.get("isMatchForState") == null)) {
                    throw new IOException("JavaScriptEdge missing \"isMatchForState\" metadata key: " + metadata);
                }
                String isMatch = metadata.get("isMatchForState").asString();
                if (metadata.get("onMatch") != null) {
                    String onMatch = metadata.get("onMatch").asString();
                    return new JavaScriptEdge(isMatch, onMatch, target);
                } else {
                    return new JavaScriptEdge(isMatch, target);
                }
            case Regex:
                if ((metadata == null) || (metadata.get("pattern") == null)) {
                    throw new IOException("RegexEdge missing \"pattern\" metadata key: " + metadata);
                }
                pattern = metadata.get("pattern").asString();
                if (stateValue != null) {
                    return new RegexEdge(pattern, stateKey, stateValue, target);
                } else {
                    return new RegexEdge(pattern, stateKey, target);
                }
            case RegexV2:
                if ((metadata == null) || (metadata.get("pattern") == null)) {
                    throw new IOException("RegexEdge missing \"pattern\" metadata key: " + metadata);
                }
                pattern = metadata.get("pattern").asString();
                if (stateValue != null) {
                	stateValues = new Object[ ((JsonArray)stateValue).size()];
                	for(int j = 0; j < ((JsonArray)stateValue).size(); j++) {
                		stateValues[j] = ((JsonArray)stateValue).get(j).asObject();
                	}
                    return new RegexEdgeV2(pattern, stateKeys, stateValues, target);
                } else {
                    return new RegexEdgeV2(pattern, stateKeys, target);
                }
            case NLPEval:
                if ((metadata == null) || (metadata.get("stateKey") == null)) {
                    throw new IOException("NLPEvalEdge missing \"stateKey\" metadata key: " + metadata);
                }
                if (stateValue != null) {
                	stateValues = new Object[ ((JsonArray)stateValue).size()];
                	for(int j = 0; j < ((JsonArray)stateValue).size(); j++) {
                		stateValues[j] = ((JsonArray)stateValue).get(j).asObject();
                	}
                    return new NLPEvalEdge(pattern, stateKeys, stateValues, target);
                } else {
                    return new NLPEvalEdge(pattern, stateKeys, target);
                }
            case Affirmative:
                if (stateValue != null) {
                    return new AffirmativeEdge(stateKey, stateValue, target);
                } else {
                    return new AffirmativeEdge(target);
                }
            case Negative:
                if (stateValue != null) {
                    return new NegativeEdge(stateKey, stateValue, target);
                } else {
                    return new NegativeEdge(target);
                }
            case Statement:
                StatementEdge e = new StatementEdge(target);
                return e;
            default:
                return null;
        }
    }

	
}
