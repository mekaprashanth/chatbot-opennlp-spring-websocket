/**
 * 
 */
package com.example.websocketdemo.conversation.engine;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import com.conversationkit.model.IConversationState;

/**
 * @author Prashanth_Meka
 *
 */
public class HttpSessionBackedState implements IConversationState<String, Object>, Map<String, Object> {

	Map<String, Object> sessionAtributes;
	
	private HttpSessionBackedState(Map<String, Object> sessionAtributes) {
		this.sessionAtributes = sessionAtributes;
	}

	@Override
    public int getCurrentNodeId() {
        Integer id = (Integer)this.get("currentNodeId");
        return id;
    }

    @Override
    public void setCurrentNodeId(int currentNodeId) {
    	sessionAtributes.put("currentNodeId", currentNodeId);
    }

    @Override
    public void set(String propertyName, Object value) {
    	sessionAtributes.put(propertyName, value);
    }

    @Override
    public Object get(Object propertyName) {
        return sessionAtributes.get(propertyName);
    }

    @Override
    public String getMostRecentResponse() {
        return (String)sessionAtributes.get("mostRecentResponse");
    }

    @Override
    public void setMostRecentResponse(String currentResponse) {
        if (currentResponse == null) {
        	sessionAtributes.remove("mostRecentResponse");
        } else {
        	sessionAtributes.put("mostRecentResponse", currentResponse);
        }
    }
    
    /**
     * @return the name
     */
    public String getName() {
        return (String)sessionAtributes.get("name");
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
    	sessionAtributes.put("name", name);
    }

    /**
     * @return the number
     */
    public int getNumber() {
        return (Integer)sessionAtributes.get("number");
    }

    /**
     * @param number the number to set
     */
    public void setNumber(int number) {
    	sessionAtributes.put("number", number);
    }
    
    public static <S extends IConversationState<String, Object>> S buildFromAttributes(Map<String, Object> sessionAtributes) {
    	return (S) new HttpSessionBackedState(sessionAtributes);
    }

	@Override
	public int size() {
		return sessionAtributes.size();
	}

	@Override
	public boolean isEmpty() {
		return sessionAtributes.isEmpty();
	}

	@Override
	public boolean containsKey(Object key) {
		return sessionAtributes.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return sessionAtributes.containsValue(value);
	}

	@Override
	public Object put(String key, Object value) {
		return sessionAtributes.put(key, value);
	}

	@Override
	public Object remove(Object key) {
		return sessionAtributes.remove(key);
	}

	@Override
	public void putAll(Map<? extends String, ? extends Object> m) {
		sessionAtributes.putAll(m);
	}

	@Override
	public void clear() {
		sessionAtributes.clear();
	}

	@Override
	public Set<String> keySet() {
		return sessionAtributes.keySet();
	}

	@Override
	public Collection<Object> values() {
		return sessionAtributes.values();
	}

	@Override
	public Set<Entry<String, Object>> entrySet() {
		return sessionAtributes.entrySet();
	}

}
