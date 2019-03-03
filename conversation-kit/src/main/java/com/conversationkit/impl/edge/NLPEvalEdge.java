/*
 * The MIT License
 *
 * Copyright 2016 Synclab Consulting LLC.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.conversationkit.impl.edge;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.stream.Collectors;

import com.conversationkit.model.IConversationNode;
import com.conversationkit.model.IConversationState;
import com.conversationkit.nlp.LocationNameFinder;

import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.Span;


public class NLPEvalEdge<S extends IConversationState<String, Object>> extends ConversationEdge<S> {

	protected final String[] stateKeys;
	protected final Object[] stateValues;
	protected final LocationNameFinder nameFinder;
	protected TokenizerME tokenizer;

	
	public NLPEvalEdge(String matchRegex, String[] stateKeys, Object[] stateValues, int flags,
			IConversationNode<S> endNode) {
		super(endNode);
		this.stateKeys = stateKeys;
		this.nameFinder = LocationNameFinder.getInstance();
		this.stateValues = stateValues;
		this.initializeTokenizer();

	}

	public void initializeTokenizer() {
		InputStream inputStreamTokenizer;
		URL fileUrl = this.getClass().getResource("/models/en-token.bin");
		try {
			inputStreamTokenizer = fileUrl.openStream();
			TokenizerModel tokenModel = new TokenizerModel(inputStreamTokenizer);
			inputStreamTokenizer.close();
			tokenizer = new TokenizerME(tokenModel);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	public NLPEvalEdge(String matchRegex, String[] stateKeys, Object[] stateValues, IConversationNode<S> endNode) {
		super(endNode);
		this.stateKeys = stateKeys;
		this.nameFinder = LocationNameFinder.getInstance();
		this.stateValues = stateValues;
		this.initializeTokenizer();
	}

	
	public NLPEvalEdge(String matchRegex, String[] stateKeys, IConversationNode<S> endNode) {
		super(endNode);
		this.stateKeys = stateKeys;
		this.nameFinder = LocationNameFinder.getInstance();
		this.stateValues = null;
		this.initializeTokenizer();
	}

	
	public NLPEvalEdge(String matchRegex, IConversationNode<S> endNode) {
		super(endNode);
		this.stateKeys = null;
		this.stateValues = null;
		this.nameFinder = LocationNameFinder.getInstance();
		this.initializeTokenizer();
	}

	@Override
	public boolean isMatchForState(S state) {

		if (state.getMostRecentResponse() != null) {
			for (NameFinderME nameFinderME : nameFinder.getNameFinderMEs()) {
				String[] tokens = tokenizer.tokenize(state.getMostRecentResponse());
				Span[] spansArr = nameFinderME.find(tokens);
				String[] strArr = Arrays.stream(spansArr).map(span -> span.getType()).sorted()
						.collect(Collectors.toList()).toArray(new String[1]);
				Arrays.sort(this.stateKeys);
				System.out.println("strArr: "+Arrays.toString(strArr)+" stateKeys: "+Arrays.toString(stateKeys));
				if (Arrays.equals(strArr, this.stateKeys)) {
					return true;
				}
			}
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onMatch(S state) {
		if (stateValues != null && stateValues.length == stateKeys.length) {
			for (int i = 0; i < stateKeys.length; i++) {
				state.set(stateKeys[i], stateValues[i]);
			}
		} else {
			for (NameFinderME nameFinderME : nameFinder.getNameFinderMEs()) {
				String[] tokens = tokenizer.tokenize(state.getMostRecentResponse());
				Span[] spans = nameFinderME.find(tokens);
				String[] names = Span.spansToStrings(spans, tokens);
				for (int i = 0; i < spans.length; i++) {
					state.set(spans[i].getType(),names[i]);
				}
			}
		}
	}

	public String toString() {
		return "NLPEvalEdge {" + nameFinder + '}';
	}

}
