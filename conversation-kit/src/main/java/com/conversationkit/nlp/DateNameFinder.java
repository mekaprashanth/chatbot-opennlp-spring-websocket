/**
 * 
 */
package com.conversationkit.nlp;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.Span;

/**
 * @author Prashanth_Meka
 *
 */
public class DateNameFinder {
	
	NameFinderME nameFinder = null;
	
	private static DateNameFinder _instance = new DateNameFinder();
	
	private DateNameFinder () {
		try {
			this.initializeDateNameFinder();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
    
    public void initializeDateNameFinder() throws IOException {
//    	InputStream inputStream = getClass().getResourceAsStream("models/en-ner-date.bin");
    	InputStream inputStream = new FileInputStream("models/en-ner-date.bin");
        TokenNameFinderModel model = new TokenNameFinderModel(inputStream);
        nameFinder = new NameFinderME(model);
    }
    
    public static DateNameFinder getInstance() {
    	return _instance;
    }
    
    public NameFinderME getNameFinderME() {
    	return nameFinder;
    }
    
    public static void main(String[] args) throws IOException {
    	DateNameFinder instance = DateNameFinder.getInstance();
    
//    	InputStream inputStreamTokenizer = DateNameFinder.class.getResourceAsStream("models/en-token.bin");
    	InputStream inputStreamTokenizer = new FileInputStream("models/en-token.bin");
        TokenizerModel tokenModel = new TokenizerModel(inputStreamTokenizer);
        TokenizerME tokenizer = new TokenizerME(tokenModel);
        String[] tokens = tokenizer.tokenize("i would like to travel from chennai to bangalore on 22-02-19");
        Span nameSpans[] = instance.getNameFinderME().find(tokens);
        for(Span span : nameSpans)
            System.out.println("Position - "+ span.toString() + "    LocationName - " + tokens[span.getStart()]);
    
	}
}
