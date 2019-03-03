/**
 * 
 */
package com.conversationkit.nlp;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import opennlp.tools.ml.naivebayes.NaiveBayesTrainer;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.NameSample;
import opennlp.tools.namefind.NameSampleDataStream;
import opennlp.tools.namefind.TokenNameFinderFactory;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.MarkableFileInputStreamFactory;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.ObjectStreamUtils;
import opennlp.tools.util.PlainTextByLineStream;
import opennlp.tools.util.Span;
import opennlp.tools.util.TrainingParameters;

/**
 * @author Prashanth_Meka
 *
 */
public class LocationNameFinder {
	
	TokenNameFinderModel model = null;
	
	private static LocationNameFinder _instance = new LocationNameFinder();
	
	List<TokenNameFinderModel> tokenNameFinderModels = new ArrayList<TokenNameFinderModel>();

	private LocationNameFinder () {
		try {
			this.initializeLocationNameFinder();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
    
    public void initializeLocationNameFinder() throws IOException {
    	URL fileUrl = this.getClass().getResource("/intent/models/train");
    	File trainingDirectory = new File(fileUrl.getFile());
        if (!trainingDirectory.isDirectory()) {
            throw new IllegalArgumentException("TrainingDirectory is not a directory: " + trainingDirectory.getAbsolutePath());
        }

        List<ObjectStream<NameSample>> nameStreams = new ArrayList<ObjectStream<NameSample>>();
        for (File trainingFile : trainingDirectory.listFiles()) {
            ObjectStream<String> lineStream = new PlainTextByLineStream(new MarkableFileInputStreamFactory(trainingFile), "UTF-8");
            ObjectStream<NameSample> nameSampleStream = new NameSampleDataStream(lineStream);
            nameStreams.add(nameSampleStream);
        }
        ObjectStream<NameSample> combinedNameSampleStream = ObjectStreamUtils.concatenateObjectStream(nameStreams);

        TrainingParameters trainingParams = new TrainingParameters();
        trainingParams.put(TrainingParameters.ITERATIONS_PARAM, 100);
        trainingParams.put(TrainingParameters.CUTOFF_PARAM, 0);
        trainingParams.put(TrainingParameters.ALGORITHM_PARAM, NaiveBayesTrainer.NAIVE_BAYES_VALUE);


        TokenNameFinderModel tokenNameFinderModel = NameFinderME.train("en", null, combinedNameSampleStream, trainingParams, new TokenNameFinderFactory());
        combinedNameSampleStream.close();
        tokenNameFinderModels.add(tokenNameFinderModel);
    
    }
    
    public static LocationNameFinder getInstance() {
    	return _instance;
    }
    
    public NameFinderME[] getNameFinderMEs() {
    	NameFinderME[] nameFinderMEs = new NameFinderME[tokenNameFinderModels.size()];
        for (int i = 0; i < tokenNameFinderModels.size(); i++) {
            nameFinderMEs[i] = new NameFinderME(tokenNameFinderModels.get(i));
        }
    	return nameFinderMEs;
    }
    
    public static void main(String[] args) throws IOException {
    	LocationNameFinder instance = LocationNameFinder.getInstance();
    
    	InputStream inputStreamTokenizer = new FileInputStream("models/en-token.bin");
        TokenizerModel tokenModel = new TokenizerModel(inputStreamTokenizer);
        inputStreamTokenizer.close();
        TokenizerME tokenizer = new TokenizerME(tokenModel);
        
        String[] tokens = tokenizer.tokenize("please book tickets to texas on 4th Oct");
//        for(int i=0; i< tokens.length; i++) {
//        	tokens[i] = tokens[i].substring(0, 1).toUpperCase() + tokens[i].substring(1);
//        }
        for (NameFinderME nameFinderME : instance.getNameFinderMEs()) {
            Span[] spans = nameFinderME.find(tokens);
            String[] names = Span.spansToStrings(spans, tokens);
            for (int i = 0; i < spans.length; i++) {
                System.out.print(spans[i].getType() + ": '" + names[i] + "' ");
            }
        }
        for (NameFinderME nameFinderME : instance.getNameFinderMEs()) {
        	nameFinderME.clearAdaptiveData();
        }
	}
}
