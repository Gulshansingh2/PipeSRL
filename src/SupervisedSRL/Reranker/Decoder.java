package SupervisedSRL.Reranker;

import SentenceStruct.Argument;
import SentenceStruct.PA;
import SentenceStruct.Sentence;
import SupervisedSRL.Strcutures.IndexMap;
import SupervisedSRL.Strcutures.Pair;
import SupervisedSRL.Strcutures.Prediction;
import SupervisedSRL.Strcutures.Prediction4Reranker;
import ml.AveragedPerceptron;
import ml.RerankerAveragedPerceptron;
import util.IO;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

/**
 * Created by Maryam Aminian on 8/25/16.
 */
public class Decoder {
    AveragedPerceptron piClassifier;
    AveragedPerceptron aiClasssifier;
    AveragedPerceptron acClasssifier;
    RerankerAveragedPerceptron reranker;
    IndexMap indexMap;
    HashMap<Object, Integer>[] rerankerFeatureMap;

    public Decoder(AveragedPerceptron piClassifier,AveragedPerceptron aiClassifier, AveragedPerceptron acClassifier, RerankerAveragedPerceptron reranker,
                   IndexMap indexMap, HashMap<Object, Integer>[] featureMap) {
        this.piClassifier = piClassifier;
        this.aiClasssifier = aiClassifier;
        this.acClasssifier = acClassifier;
        this.reranker = reranker;
        this.indexMap = indexMap;
        this.rerankerFeatureMap = featureMap;
    }

    private int predict(RerankerPool pool) throws Exception {
        return reranker.argmax(pool, true);
    }

    public void decode(ArrayList<String> testSentences, int numOfPIFeatures, int numOfPDFeatures, int numOfAIFeatures, int numOfACFeatures, int numOfGlobalFeatures,
                       int aiMaxBeamSize, int acMaxBeamSize, String outputFile, double aiCoefficient, String pdModelDir, boolean usePI) throws Exception {

        SupervisedSRL.Decoder decoder = new SupervisedSRL.Decoder(this.piClassifier, this.aiClasssifier, this.acClasssifier);
        BufferedWriter outputWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8"));

        for (int senIdx = 0; senIdx < testSentences.size(); senIdx++) {
            if (senIdx%1000 ==0)
                System.out.println(senIdx+"/"+ testSentences.size());

            Sentence testSentence = new Sentence(testSentences.get(senIdx), indexMap);
            HashMap<Integer, HashMap<Integer, Integer>> goldMap = getGoldArgLabelMap(testSentence, acClasssifier.getReverseLabelMap());
            ArrayList<String> sentenceToWriteOutputFile = IO.getSentenceForOutput(testSentences.get(senIdx));
            TreeMap<Integer, Prediction> prediction4ThisSentence = new TreeMap<Integer, Prediction>();

            TreeMap<Integer, Prediction4Reranker> predictedAIACCandidates4thisSen =
                    (TreeMap<Integer, Prediction4Reranker>) decoder.predict(testSentence, indexMap, aiMaxBeamSize, acMaxBeamSize,
                            numOfPIFeatures, numOfPDFeatures, numOfAIFeatures, numOfACFeatures, true, aiCoefficient, pdModelDir, usePI);

            //creating the pool and making prediction
            prediction4ThisSentence = obtainRerankerPrediction4Sentence(numOfAIFeatures, numOfACFeatures, numOfGlobalFeatures, testSentence, goldMap, predictedAIACCandidates4thisSen);
            outputWriter.write(IO.generateCompleteOutputSentenceInCoNLLFormat(sentenceToWriteOutputFile, prediction4ThisSentence, acClasssifier.getLabelMap()));
        }
        outputWriter.flush();
        outputWriter.close();
    }


    private TreeMap<Integer, Prediction> obtainRerankerPrediction4Sentence(int numOfAIFeatures, int numOfACFeatures,
                                                                           int numOfGlobalFeatures, Sentence testSentence,
                                                                           HashMap<Integer, HashMap<Integer, Integer>> goldMap,
                                                                           TreeMap<Integer, Prediction4Reranker> predictedAIACCandidates4thisSen) throws Exception {
        TreeMap<Integer, Prediction> predictions4ThisSentence = new TreeMap<Integer, Prediction>();

        for (int pIdx : predictedAIACCandidates4thisSen.keySet()) {
            RerankerPool rerankerPool = new RerankerPool();
            String pLabel = predictedAIACCandidates4thisSen.get(pIdx).getPredicateLabel();
            HashMap<Integer, Pair<Integer, Integer>> acCandidateIndexInfo = new HashMap<Integer, Pair<Integer, Integer>>();

            ArrayList<Pair<Double, ArrayList<Integer>>> aiCandidates = predictedAIACCandidates4thisSen.get(pIdx).getAiCandidates();
            ArrayList<ArrayList<Pair<Double, ArrayList<Integer>>>> acCandidates = predictedAIACCandidates4thisSen.get(pIdx).getAcCandidates();
            int acCandidateIndex = -1;

            for (int i = 0; i < aiCandidates.size(); i++) {
                Pair<Double, ArrayList<Integer>> aiCandid = aiCandidates.get(i);
                ArrayList<Pair<Double, ArrayList<Integer>>> acCandids4thisAiCandid = acCandidates.get(i);

                for (int j = 0; j < acCandids4thisAiCandid.size(); j++) {
                    acCandidateIndex++;
                    acCandidateIndexInfo.put(acCandidateIndex, new Pair<Integer, Integer>(i, j));
                    Pair<Double, ArrayList<Integer>> acCandid = acCandids4thisAiCandid.get(j);
                    rerankerPool.addInstance(new RerankerInstanceItem(RerankerInstanceGenerator.extractFinalRerankerFeatures(pIdx, pLabel, testSentence, aiCandid, acCandid,
                            numOfAIFeatures, numOfACFeatures,numOfGlobalFeatures,  indexMap, acClasssifier.getLabelMap(), acClasssifier.getReverseLabelMap(), rerankerFeatureMap), "0"), false);
                }
            }

            int bestCandidateIndex = predict(rerankerPool);
            int bestAICandidIndex = acCandidateIndexInfo.get(bestCandidateIndex).first;
            int bestACCandidIndex = acCandidateIndexInfo.get(bestCandidateIndex).second;

            predictions4ThisSentence.put(pIdx,
                    new Prediction(pLabel, aiCandidates.get(bestAICandidIndex).second,
                            acCandidates.get(bestAICandidIndex).get(bestACCandidIndex).second));
        }
        return predictions4ThisSentence;
    }


    private HashMap<Integer, HashMap<Integer, Integer>> getGoldArgLabelMap(Sentence sentence,
                                                                           HashMap<String, Integer> reverseLabelMap) {
        HashMap<Integer, HashMap<Integer, Integer>> goldArgLabelMap = new HashMap<Integer, HashMap<Integer, Integer>>();
        ArrayList<PA> goldPAs = sentence.getPredicateArguments().getPredicateArgumentsAsArray();
        for (PA pa : goldPAs) {
            int goldPIdx = pa.getPredicate().getIndex();
            ArrayList<Argument> goldArgs = pa.getArguments();
            HashMap<Integer, Integer> goldArgMap = new HashMap<Integer, Integer>();
            for (Argument arg : goldArgs)
                goldArgMap.put(arg.getIndex(), reverseLabelMap.get(arg.getType()));
            goldArgLabelMap.put(goldPIdx, goldArgMap);
        }
        return goldArgLabelMap;
    }
}
