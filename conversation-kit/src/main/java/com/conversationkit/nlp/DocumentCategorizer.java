/**
 * 
 */
package com.conversationkit.nlp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import opennlp.tools.doccat.DoccatFactory;
import opennlp.tools.doccat.DoccatModel;
import opennlp.tools.doccat.DocumentCategorizerME;
import opennlp.tools.doccat.DocumentSample;
import opennlp.tools.util.MarkableFileInputStreamFactory;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.ObjectStreamUtils;
import opennlp.tools.util.PlainTextByLineStream;
import opennlp.tools.util.TrainingParameters;

/**
 * @author Prashanth_Meka
 *
 */
public class DocumentCategorizer {
	
	DoccatModel doccatModel = null;
	DocumentCategorizerME categorizer = null;
	public DocumentCategorizer() {
		try {
			this.categorizeDocuments();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void categorizeDocuments() throws FileNotFoundException, IOException {
		File trainingDirectory = new File("intent/models/train");
        if (!trainingDirectory.isDirectory()) {
            throw new IllegalArgumentException("TrainingDirectory is not a directory: " + trainingDirectory.getAbsolutePath());
        }

        List<ObjectStream<DocumentSample>> categoryStreams = new ArrayList<ObjectStream<DocumentSample>>();
        for (File trainingFile : trainingDirectory.listFiles()) {
            String intent = trainingFile.getName().replaceFirst("[.][^.]+$", "");
            ObjectStream<String> lineStream = new PlainTextByLineStream(new MarkableFileInputStreamFactory(trainingFile), "UTF-8");
            ObjectStream<DocumentSample> documentSampleStream = new IntentDocumentSampleStream(intent, lineStream);
            categoryStreams.add(documentSampleStream);
        }

        ObjectStream<DocumentSample> combinedDocumentSampleStream = ObjectStreamUtils.concatenateObjectStream(categoryStreams);

        TrainingParameters trainingParams = new TrainingParameters();
        trainingParams.put(TrainingParameters.ITERATIONS_PARAM, 10);
        trainingParams.put(TrainingParameters.CUTOFF_PARAM, 0);

        doccatModel = DocumentCategorizerME.train("en", combinedDocumentSampleStream, trainingParams, new DoccatFactory());
        combinedDocumentSampleStream.close();
        categorizer = new DocumentCategorizerME(doccatModel);
	}

}
