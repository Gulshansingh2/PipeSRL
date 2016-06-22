package SupervisedSRL.Features;
/**
 * Created by Maryam Aminian on 5/17/16.
 */

import Sentence.Sentence;
import Sentence.Predicate;
import Sentence.Argument;
import SupervisedSRL.Strcutures.IndexMap;
import apple.laf.JRSUIUtils;
import util.StringUtils;

import java.util.HashMap;
import java.util.TreeSet;
import java.util.Set;

public class FeatureExtractor {

    // todo Object[]
    public static Object[] extractFeatures(Predicate p, int aIdx, Sentence sentence, String state, int length,
                                           IndexMap indexMap) {

        // todo object; int; respectively
        Object[] features = new String[length];
        int[] sentenceDepLabels = sentence.getDepLabels();
        int[] sentenceDepHeads = sentence.getDepHeads();
        int[] sentenceWords = sentence.getWords();
        int[] sentencePOSTags = sentence.getPosTags();
        int[] sentenceLemmas = sentence.getLemmas();
        TreeSet<Integer>[] sentenceReverseDepHeads = sentence.getReverseDepHeads();

        //predicate features
        int pIdx = p.getIndex();
        int pw = sentenceWords[pIdx];
        int ppos = sentencePOSTags[pIdx];
        int plem = sentenceLemmas[pIdx];
        int pdeprel = sentenceDepLabels[pIdx];
        String psense = p.getLabel();
        int pprw = sentenceWords[sentenceDepHeads[pIdx]];
        int pprpos = sentencePOSTags[sentenceDepHeads[pIdx]];
        String pdepsubcat = getDepSubCat(pIdx, sentenceReverseDepHeads, sentenceDepLabels);
        String pchilddepset = getChildSet(pIdx, sentenceReverseDepHeads, sentenceDepLabels);
        String pchildposset = getChildSet(pIdx, sentenceReverseDepHeads, sentencePOSTags);
        String pchildwset = getChildSet(pIdx, sentenceReverseDepHeads, sentenceWords);

       // String voice = sentence.getVoice(pIdx);

        if (state.equals("AI") || state.equals("AC")) {
            int leftMostDependentIndex = getLeftMostDependentIndex(aIdx, sentenceReverseDepHeads);
            int rightMostDependentIndex = getRightMostDependentIndex(aIdx, sentenceReverseDepHeads);
            int lefSiblingIndex = getLeftSiblingIndex(aIdx, sentenceReverseDepHeads);
            int rightSiblingIndex = getRightSiblingIndex(aIdx, sentenceReverseDepHeads);

            //argument features
            int aw = sentenceWords[aIdx];
            int apos = sentencePOSTags[aIdx];
            int adeprel = sentenceDepLabels[aIdx];

            //predicate-argument features
            String deprelpath = StringUtils.convertPathArrayIntoString(sentence.getDepPath(pIdx, aIdx));
            String pospath = StringUtils.convertPathArrayIntoString(sentence.getPOSPath(pIdx, aIdx));
            int position = (pIdx < aIdx) ? 1:0;
            int leftw = (leftMostDependentIndex != -1) ? sentenceWords[leftMostDependentIndex] : indexMap.getNullIdx();
            int leftpos = (leftMostDependentIndex != -1) ? sentencePOSTags[leftMostDependentIndex] : indexMap.getNullIdx();
            int rightw = (rightMostDependentIndex != -1) ? sentenceWords[rightMostDependentIndex] : indexMap.getNullIdx();
            int rightpos = (rightMostDependentIndex != -1) ? sentencePOSTags[rightMostDependentIndex] : indexMap.getNullIdx();
            int rightsiblingw = (rightSiblingIndex != -1) ? sentenceWords[rightSiblingIndex] : indexMap.getNullIdx();
            int rightsiblingpos = (rightSiblingIndex != -1) ? sentencePOSTags[rightSiblingIndex] : indexMap.getNullIdx();
            int leftsiblingw = (lefSiblingIndex != -1) ? sentenceWords[lefSiblingIndex] : indexMap.getNullIdx();
            int leftsiblingpos = (lefSiblingIndex != -1) ? sentencePOSTags[lefSiblingIndex] : indexMap.getNullIdx();

            //build feature vector for argument identification module
            int index = 0;
            features[index++] = pw;
            features[index++] = ppos;
            features[index++] = plem;
            features[index++] = pdeprel;
            features[index++] = psense;
            features[index++] = pprw;
            features[index++] = pprpos;
            features[index++] = pdepsubcat;
            features[index++] = pchilddepset;
            features[index++] = pchildposset;
            features[index++] = pchildwset;

            features[index++] = aw;
            features[index++] = apos;
            features[index++] = adeprel;
            features[index++] = deprelpath;
            features[index++] = pospath;
            features[index++] = position;
            features[index++] = leftw;
            features[index++] = leftpos;
            features[index++] = rightw;
            features[index++] = rightpos;
            features[index++] = leftsiblingw;
            features[index++] = leftsiblingpos;
            features[index++] = rightsiblingw;
            features[index++] = rightsiblingpos;

            //predicate-argument conjoined features
            // pw + argument features

            // todo 20 bits for words, 10 bits for pos, 10 bits for dependencies
            // todo e.g. (pw<<20) | aw ==> 20+20> 32 ==> long
            long pw_aw = (pw<<20) | aw;
            features[index++] = pw_aw;
            // todo (pw<<10) | pos ==> 10+20<32 ==> int
            int pw_apos = (pw<<10) | apos ;
            features[index++] = pw_apos;
            int pw_adeprel = (pw<<10) | adeprel;
            features[index++] = pw_adeprel;
            StringBuilder pw_deprelpath= new StringBuilder();
            pw_deprelpath.append(pw);
            pw_deprelpath.append(" ");
            pw_deprelpath.append(deprelpath);
            features[index++] = pw_deprelpath.toString();
            StringBuilder pw_pospath= new StringBuilder();
            pw_deprelpath.append(pw);
            pw_deprelpath.append(" ");
            pw_deprelpath.append(pospath);
            features[index++] = pw_pospath.toString();
            int pw_position = (pw>>8) | position;
            features[index++] = pw_position;
            long pw_leftw = (pw>>20) | leftw;
            features[index++] = pw_leftw;
            int pw_leftpos = (pw>>10) | leftpos;
            features[index++] = pw_leftpos;
            long pw_rightw = (pw>>20) | rightw;
            features[index++] = pw_rightw;
            int pw_rightpos = (pw>>10) | rightpos;
            features[index++] = pw_rightpos;
            long pw_leftsiblingw = (pw>>20) | leftsiblingw;
            features[index++] = pw_leftsiblingw;
            int pw_leftsiblingpos= (pw>>10) | leftsiblingpos;
            features[index++] = pw_leftsiblingpos;
            long pw_rightsiblingw = (pw<<20) | rightsiblingw;
            features[index++] = pw_rightsiblingw;
            int pw_rightsiblingpos = (pw<<10) | rightsiblingpos;
            features[index++] = pw_rightsiblingpos;

            //ppos + argument features
            int aw_ppos = (aw<<10) | ppos;
            features[index++] = aw_ppos;
            int ppos_apos = (ppos<<10) | apos;
            features[index++] = ppos_apos;
            int ppos_adeprel = (ppos<<10) | adeprel;
            features[index++] = ppos_adeprel;
            StringBuilder ppos_deprelpath = new StringBuilder();
            ppos_deprelpath.append(ppos);
            ppos_deprelpath.append(" ");
            ppos_deprelpath.append(deprelpath);
            features[index++] = ppos_deprelpath;
            StringBuilder ppos_pospath = new StringBuilder();
            ppos_pospath.append(ppos);
            ppos_pospath.append(" ");
            ppos_pospath.append(pospath);
            features[index++] = ppos_pospath;
            int ppos_position = (ppos<<1) | position;
            features[index++] = ppos_position;
            int leftw_ppos = (leftw<<10) | ppos;
            features[index++] = leftw_ppos;
            int ppos_leftpos = (ppos<<10) | leftpos;
            features[index++] = ppos_leftpos;
            int rightw_ppos = (rightw<<10) | ppos;
            features[index++] = rightw_ppos;
            int ppos_rightpos = (ppos<<10) | rightpos;
            features[index++] = ppos_rightpos;
            int leftsiblingw_ppos = (leftsiblingw<<10) | ppos;
            features[index++] = leftsiblingw_ppos;
            int ppos_leftsiblingpos = (ppos<<10) | leftsiblingpos;
            features[index++] = ppos_leftsiblingpos;
            int rightsiblingw_ppos = (rightsiblingw<<10) | ppos;
            features[index++] = rightsiblingw_ppos;
            int ppos_rightsiblingpos = (ppos<<10) | rightsiblingpos;
            features[index++] = ppos_rightsiblingpos;


            //pdeprel + argument features
            int aw_pdeprel = (aw<<10) | pdeprel;
            features[index++] = aw_pdeprel;
            int pdeprel_apos = (pdeprel<<10) | apos;
            features[index++] = pdeprel_apos;
            int pdeprel_adeprel = (pdeprel<<10) | adeprel;
            features[index++] = pdeprel_adeprel;
            StringBuilder pdeprel_deprelpath = new StringBuilder();
            pdeprel_deprelpath.append(pdeprel);
            pdeprel_deprelpath.append(" ");
            pdeprel_deprelpath.append(deprelpath);
            features[index++] = pdeprel_deprelpath;
            StringBuilder pdeprel_pospath= new StringBuilder();
            pdeprel_pospath.append(pdeprel);
            pdeprel_pospath.append(" ");
            pdeprel_pospath.append(pospath);
            features[index++] = pdeprel_pospath;
            int pdeprel_position = (pdeprel<<1) | position;
            features[index++] = pdeprel_position;
            int leftw_pdeprel = (leftw<<10) | pdeprel;
            features[index++] = leftw_pdeprel;
            int pdeprel_leftpos = (pdeprel<<10) | leftpos;
            features[index++] = pdeprel_leftpos;
            int rightw_pdeprel = (rightw<<10) | pdeprel;
            features[index++] = rightw_pdeprel;
            int pdeprel_rightpos = (pdeprel<<10) | rightpos;
            features[index++] = pdeprel_rightpos;
            int leftsiblingw_pdeprel = (leftsiblingw<<10) |pdeprel;
            features[index++] = leftsiblingw_pdeprel;
            int pdeprel_leftsiblingpos = (pdeprel<<10) | leftsiblingpos;
            features[index++] = pdeprel_leftsiblingpos;
            int rightsiblingw_pdeprel = (rightsiblingw<<10) | pdeprel;
            features[index++] = rightsiblingw_pdeprel;
            int pdeprel_rightsiblingpos = (pdeprel<<10) | rightsiblingpos;
            features[index++] = pdeprel_rightsiblingpos;


            //plem + argument features
            long aw_plem = (aw<<20) | plem;
            features[index++] = aw_plem;
            int plem_apos = (plem<<10) | apos;
            features[index++] = plem_apos;
            int plem_adeprel = (plem<<10) | adeprel;
            features[index++] = plem_adeprel;
            StringBuilder plem_deprelpath = new StringBuilder();
            pdeprel_deprelpath.append(plem);
            pdeprel_deprelpath.append(" ");
            pdeprel_deprelpath.append(deprelpath);
            features[index++] = plem_deprelpath;
            StringBuilder plem_pospath= new StringBuilder();
            pdeprel_pospath.append(plem);
            pdeprel_pospath.append(" ");
            pdeprel_pospath.append(pospath);
            features[index++] = plem_pospath;
            int plem_position = (plem<<1) | position;
            features[index++] = plem_position;
            long leftw_plem = (leftw<<20) | plem;
            features[index++] = leftw_plem;
            int plem_leftpos = (plem<<10) | leftpos;
            features[index++] = plem_leftpos;
            long rightw_plem = (rightw<<20) | plem;
            features[index++] = rightw_plem;
            int plem_rightpos = (plem<<10) | rightpos;
            features[index++] = plem_rightpos;
            long leftsiblingw_plem = (leftsiblingw<<20) |plem;
            features[index++] = leftsiblingw_plem;
            int plem_leftsiblingpos = (plem<<10) | leftsiblingpos;
            features[index++] = plem_leftsiblingpos;
            long rightsiblingw_plem = (rightsiblingw<<20) | plem;
            features[index++] = rightsiblingw_plem;
            int plem_rightsiblingpos = (plem<<10) | rightsiblingpos;
            features[index++] = plem_rightsiblingpos;

            /*
            //psense + argument features
            long aw_psense = (aw<<20) | psense;
            features[index++] = aw_psense;
            int psense_apos = (psense<<10) | apos;
            features[index++] = psense_apos;
            int psense_adeprel = (psense<<10) | adeprel;
            features[index++] = psense_adeprel;
            StringBuilder psense_deprelpath = new StringBuilder();
            pdeprel_deprelpath.append(psense);
            pdeprel_deprelpath.append(" ");
            pdeprel_deprelpath.append(deprelpath);
            features[index++] = psense_deprelpath;
            StringBuilder psense_pospath= new StringBuilder();
            pdeprel_pospath.append(psense);
            pdeprel_pospath.append(" ");
            pdeprel_pospath.append(pospath);
            features[index++] = psense_pospath;
            int psense_position = (psense<<1) | position;
            features[index++] = psense_position;
            long leftw_psense = (leftw<<20) | psense;
            features[index++] = leftw_psense;
            int psense_leftpos = (psense<<10) | leftpos;
            features[index++] = psense_leftpos;
            long rightw_psense = (rightw<<20) | psense;
            features[index++] = rightw_psense;
            int psense_rightpos = (psense<<10) | rightpos;
            features[index++] = psense_rightpos;
            long leftsiblingw_psense = (leftsiblingw<<20) |psense;
            features[index++] = leftsiblingw_psense;
            int psense_leftsiblingpos = (psense<<10) | leftsiblingpos;
            features[index++] = psense_leftsiblingpos;
            long rightsiblingw_psense = (rightsiblingw<<20) | psense;
            features[index++] = rightsiblingw_psense;
            int psense_rightsiblingpos = (psense<<10) | rightsiblingpos;
            features[index++] = psense_rightsiblingpos;
            */

            //psense + argument features
            StringBuilder psense_aw = new StringBuilder();
            psense_aw.append(psense);
            psense_aw.append(" ");
            psense_aw.append(aw);
            features[index++] = psense_aw;
            StringBuilder psense_apos = new StringBuilder();
            psense_apos.append(psense);
            psense_apos.append(" ");
            psense_apos.append(apos);
            features[index++] = psense_apos;
            StringBuilder psense_adeprel = new StringBuilder();
            psense_adeprel.append(psense);
            psense_adeprel.append(" ");
            psense_adeprel.append(adeprel);
            features[index++] = psense_adeprel;
            StringBuilder psense_deprelpath = new StringBuilder();
            psense_deprelpath.append(psense);
            psense_deprelpath.append(" ");
            psense_deprelpath.append(deprelpath);
            features[index++] = psense_deprelpath;
            StringBuilder psense_pospath = new StringBuilder();
            psense_pospath.append(psense);
            psense_pospath.append(" ");
            psense_pospath.append(pospath);
            features[index++] = psense_pospath;
            StringBuilder psense_position = new StringBuilder();
            psense_position.append(psense);
            psense_position.append(" ");
            psense_position.append(position);
            features[index++] = psense_position;
            StringBuilder psense_leftw = new StringBuilder();
            psense_leftw.append(psense);
            psense_leftw.append(" ");
            psense_leftw.append(leftw);
            features[index++] = psense_leftw;
            StringBuilder psense_leftpos = new StringBuilder();
            psense_leftpos.append(psense);
            psense_leftpos.append(" ");
            psense_leftpos.append(leftpos);
            features[index++] = psense_leftpos;
            StringBuilder psense_rightw = new StringBuilder();
            psense_rightw.append(psense);
            psense_rightw.append(" ");
            psense_rightw.append(rightw);
            features[index++] = psense_rightw;
            StringBuilder psense_rightpos = new StringBuilder();
            psense_rightpos.append(psense);
            psense_rightpos.append(" ");
            psense_rightpos.append(rightpos);
            features[index++] = psense_rightpos;
            StringBuilder psense_leftsiblingw = new StringBuilder();
            psense_leftsiblingw.append(psense);
            psense_leftsiblingw.append(" ");
            psense_leftsiblingw.append(leftsiblingw);
            features[index++] = psense_leftsiblingw;
            StringBuilder psense_leftsiblingpos = new StringBuilder();
            psense_leftsiblingpos.append(psense);
            psense_leftsiblingpos.append(" ");
            psense_leftsiblingpos.append(leftsiblingpos);
            features[index++] = psense_leftsiblingpos;
            StringBuilder psense_rightsiblingw = new StringBuilder();
            psense_rightsiblingw.append(psense);
            psense_rightsiblingw.append(" ");
            psense_rightsiblingw.append(rightsiblingw);
            features[index++] = psense_rightsiblingw;
            StringBuilder psense_rightsiblingpos = new StringBuilder();
            psense_rightsiblingpos.append(psense);
            psense_rightsiblingpos.append(" ");
            psense_rightsiblingpos.append(rightsiblingpos);
            features[index++] = psense_rightsiblingpos;


            //pprw  + argument features
            long aw_pprw = (aw<<20) | pprw;
            features[index++] = aw_pprw;
            int pprw_apos = (pprw<<10) | apos;
            features[index++] = pprw_apos;
            int pprw_adeprel = (pprw<<10) | adeprel;
            features[index++] = pprw_adeprel;
            StringBuilder pprw_deprelpath = new StringBuilder();
            pdeprel_deprelpath.append(pprw);
            pdeprel_deprelpath.append(" ");
            pdeprel_deprelpath.append(deprelpath);
            features[index++] = pprw_deprelpath;
            StringBuilder pprw_pospath= new StringBuilder();
            pdeprel_pospath.append(pprw);
            pdeprel_pospath.append(" ");
            pdeprel_pospath.append(pospath);
            features[index++] = pprw_pospath;
            int pprw_position = (pprw<<1) | position;
            features[index++] = pprw_position;
            long leftw_pprw = (leftw<<20) | pprw;
            features[index++] = leftw_pprw;
            int pprw_leftpos = (pprw<<10) | leftpos;
            features[index++] = pprw_leftpos;
            long rightw_pprw = (rightw<<20) | pprw;
            features[index++] = rightw_pprw;
            int pprw_rightpos = (pprw<<10) | rightpos;
            features[index++] = pprw_rightpos;
            long leftsiblingw_pprw = (leftsiblingw<<20) |pprw;
            features[index++] = leftsiblingw_pprw;
            int pprw_leftsiblingpos = (pprw<<10) | leftsiblingpos;
            features[index++] = pprw_leftsiblingpos;
            long rightsiblingw_pprw = (rightsiblingw<<20) | pprw;
            features[index++] = rightsiblingw_pprw;
            int pprw_rightsiblingpos = (pprw<<10) | rightsiblingpos;
            features[index++] = pprw_rightsiblingpos;


            //pdeprel + argument features
            int aw_pprpos = (aw<<10) | pprpos;
            features[index++] = aw_pprpos;
            int pprpos_apos = (pprpos<<10) | apos;
            features[index++] = pprpos_apos;
            int pprpos_adeprel = (pprpos<<10) | adeprel;
            features[index++] = pprpos_adeprel;
            StringBuilder pprpos_deprelpath = new StringBuilder();
            pdeprel_deprelpath.append(pprpos);
            pdeprel_deprelpath.append(" ");
            pdeprel_deprelpath.append(deprelpath);
            features[index++] = pprpos_deprelpath;
            StringBuilder pprpos_pospath= new StringBuilder();
            pdeprel_pospath.append(pprpos);
            pdeprel_pospath.append(" ");
            pdeprel_pospath.append(pospath);
            features[index++] = pprpos_pospath;
            int pprpos_position = (pprpos<<1) | position;
            features[index++] = pprpos_position;
            int leftw_pprpos = (leftw<<10) | pprpos;
            features[index++] = leftw_pprpos;
            int pprpos_leftpos = (pprpos<<10) | leftpos;
            features[index++] = pprpos_leftpos;
            int rightw_pprpos = (rightw<<10) | pprpos;
            features[index++] = rightw_pprpos;
            int pprpos_rightpos = (pprpos<<10) | rightpos;
            features[index++] = pprpos_rightpos;
            int leftsiblingw_pprpos = (leftsiblingw<<10) |pprpos;
            features[index++] = leftsiblingw_pprpos;
            int pprpos_leftsiblingpos = (pprpos<<10) | leftsiblingpos;
            features[index++] = pprpos_leftsiblingpos;
            int rightsiblingw_pprpos = (rightsiblingw<<10) | pprpos;
            features[index++] = rightsiblingw_pprpos;
            int pprpos_rightsiblingpos = (pprpos<<10) | rightsiblingpos;
            features[index++] = pprpos_rightsiblingpos;

            //pchilddepset + argument features
            StringBuilder pchilddepset_aw = new StringBuilder();
            pchilddepset_aw.append(pchilddepset);
            pchilddepset_aw.append(" ");
            pchilddepset_aw.append(aw);
            features[index++] = pchilddepset_aw;
            StringBuilder pchilddepset_apos = new StringBuilder();
            pchilddepset_apos.append(pchilddepset);
            pchilddepset_apos.append(" ");
            pchilddepset_apos.append(apos);
            features[index++] = pchilddepset_apos;
            StringBuilder pchilddepset_adeprel = new StringBuilder();
            pchilddepset_adeprel.append(pchilddepset);
            pchilddepset_adeprel.append(" ");
            pchilddepset_adeprel.append(adeprel);
            features[index++] = pchilddepset_adeprel;
            StringBuilder pchilddepset_deprelpath = new StringBuilder();
            pchilddepset_deprelpath.append(pchilddepset);
            pchilddepset_deprelpath.append(" ");
            pchilddepset_deprelpath.append(deprelpath);
            features[index++] = pchilddepset_deprelpath;
            StringBuilder pchilddepset_pospath = new StringBuilder();
            pchilddepset_pospath.append(pchilddepset);
            pchilddepset_pospath.append(" ");
            pchilddepset_pospath.append(pospath);
            features[index++] = pchilddepset_pospath;
            StringBuilder pchilddepset_position = new StringBuilder();
            pchilddepset_position.append(pchilddepset);
            pchilddepset_position.append(" ");
            pchilddepset_position.append(position);
            features[index++] = pchilddepset_position;
            StringBuilder pchilddepset_leftw = new StringBuilder();
            pchilddepset_leftw.append(pchilddepset);
            pchilddepset_leftw.append(" ");
            pchilddepset_leftw.append(leftw);
            features[index++] = pchilddepset_leftw;
            StringBuilder pchilddepset_leftpos = new StringBuilder();
            pchilddepset_leftpos.append(pchilddepset);
            pchilddepset_leftpos.append(" ");
            pchilddepset_leftpos.append(leftpos);
            features[index++] = pchilddepset_leftpos;
            StringBuilder pchilddepset_rightw = new StringBuilder();
            pchilddepset_rightw.append(pchilddepset);
            pchilddepset_rightw.append(" ");
            pchilddepset_rightw.append(rightw);
            features[index++] = pchilddepset_rightw;
            StringBuilder pchilddepset_rightpos = new StringBuilder();
            pchilddepset_rightpos.append(pchilddepset);
            pchilddepset_rightpos.append(" ");
            pchilddepset_rightpos.append(rightpos);
            features[index++] = pchilddepset_rightpos;
            StringBuilder pchilddepset_leftsiblingw = new StringBuilder();
            pchilddepset_leftsiblingw.append(pchilddepset);
            pchilddepset_leftsiblingw.append(" ");
            pchilddepset_leftsiblingw.append(leftsiblingw);
            features[index++] = pchilddepset_leftsiblingw;
            StringBuilder pchilddepset_leftsiblingpos = new StringBuilder();
            pchilddepset_leftsiblingpos.append(pchilddepset);
            pchilddepset_leftsiblingpos.append(" ");
            pchilddepset_leftsiblingpos.append(leftsiblingpos);
            features[index++] = pchilddepset_leftsiblingpos;
            StringBuilder pchilddepset_rightsiblingw = new StringBuilder();
            pchilddepset_rightsiblingw.append(pchilddepset);
            pchilddepset_rightsiblingw.append(" ");
            pchilddepset_rightsiblingw.append(rightsiblingw);
            features[index++] = pchilddepset_rightsiblingw;
            StringBuilder pchilddepset_rightsiblingpos = new StringBuilder();
            pchilddepset_rightsiblingpos.append(pchilddepset);
            pchilddepset_rightsiblingpos.append(" ");
            pchilddepset_rightsiblingpos.append(rightsiblingpos);
            features[index++] = pchilddepset_rightsiblingpos;


            //pdepsubcat + argument features
            StringBuilder pdepsubcat_aw = new StringBuilder();
            pdepsubcat_aw.append(pdepsubcat);
            pdepsubcat_aw.append(" ");
            pdepsubcat_aw.append(aw);
            features[index++] = pdepsubcat_aw;
            StringBuilder pdepsubcat_apos = new StringBuilder();
            pdepsubcat_apos.append(pdepsubcat);
            pdepsubcat_apos.append(" ");
            pdepsubcat_apos.append(apos);
            features[index++] = pdepsubcat_apos;
            StringBuilder pdepsubcat_adeprel = new StringBuilder();
            pdepsubcat_adeprel.append(pdepsubcat);
            pdepsubcat_adeprel.append(" ");
            pdepsubcat_adeprel.append(adeprel);
            features[index++] = pdepsubcat_adeprel;
            StringBuilder pdepsubcat_deprelpath = new StringBuilder();
            pdepsubcat_deprelpath.append(pdepsubcat);
            pdepsubcat_deprelpath.append(" ");
            pdepsubcat_deprelpath.append(deprelpath);
            features[index++] = pdepsubcat_deprelpath;
            StringBuilder pdepsubcat_pospath = new StringBuilder();
            pdepsubcat_pospath.append(pdepsubcat);
            pdepsubcat_pospath.append(" ");
            pdepsubcat_pospath.append(pospath);
            features[index++] = pdepsubcat_pospath;
            StringBuilder pdepsubcat_position = new StringBuilder();
            pdepsubcat_position.append(pdepsubcat);
            pdepsubcat_position.append(" ");
            pdepsubcat_position.append(position);
            features[index++] = pdepsubcat_position;
            StringBuilder pdepsubcat_leftw = new StringBuilder();
            pdepsubcat_leftw.append(pdepsubcat);
            pdepsubcat_leftw.append(" ");
            pdepsubcat_leftw.append(leftw);
            features[index++] = pdepsubcat_leftw;
            StringBuilder pdepsubcat_leftpos = new StringBuilder();
            pdepsubcat_leftpos.append(pdepsubcat);
            pdepsubcat_leftpos.append(" ");
            pdepsubcat_leftpos.append(leftpos);
            features[index++] = pdepsubcat_leftpos;
            StringBuilder pdepsubcat_rightw = new StringBuilder();
            pdepsubcat_rightw.append(pdepsubcat);
            pdepsubcat_rightw.append(" ");
            pdepsubcat_rightw.append(rightw);
            features[index++] = pdepsubcat_rightw;
            StringBuilder pdepsubcat_rightpos = new StringBuilder();
            pdepsubcat_rightpos.append(pdepsubcat);
            pdepsubcat_rightpos.append(" ");
            pdepsubcat_rightpos.append(rightpos);
            features[index++] = pdepsubcat_rightpos;
            StringBuilder pdepsubcat_leftsiblingw = new StringBuilder();
            pdepsubcat_leftsiblingw.append(pdepsubcat);
            pdepsubcat_leftsiblingw.append(" ");
            pdepsubcat_leftsiblingw.append(leftsiblingw);
            features[index++] = pdepsubcat_leftsiblingw;
            StringBuilder pdepsubcat_leftsiblingpos = new StringBuilder();
            pdepsubcat_leftsiblingpos.append(pdepsubcat);
            pdepsubcat_leftsiblingpos.append(" ");
            pdepsubcat_leftsiblingpos.append(leftsiblingpos);
            features[index++] = pdepsubcat_leftsiblingpos;
            StringBuilder pdepsubcat_rightsiblingw = new StringBuilder();
            pdepsubcat_rightsiblingw.append(pdepsubcat);
            pdepsubcat_rightsiblingw.append(" ");
            pdepsubcat_rightsiblingw.append(rightsiblingw);
            features[index++] = pdepsubcat_rightsiblingw;
            StringBuilder pdepsubcat_rightsiblingpos = new StringBuilder();
            pdepsubcat_rightsiblingpos.append(pdepsubcat);
            pdepsubcat_rightsiblingpos.append(" ");
            pdepsubcat_rightsiblingpos.append(rightsiblingpos);
            features[index++] = pdepsubcat_rightsiblingpos;


            //pchildposset + argument features
            StringBuilder pchildposset_aw = new StringBuilder();
            pchildposset_aw.append(pchildposset);
            pchildposset_aw.append(" ");
            pchildposset_aw.append(aw);
            features[index++] = pchildposset_aw;
            StringBuilder pchildposset_apos = new StringBuilder();
            pchildposset_apos.append(pchildposset);
            pchildposset_apos.append(" ");
            pchildposset_apos.append(apos);
            features[index++] = pchildposset_apos;
            StringBuilder pchildposset_adeprel = new StringBuilder();
            pchildposset_adeprel.append(pchildposset);
            pchildposset_adeprel.append(" ");
            pchildposset_adeprel.append(adeprel);
            features[index++] = pchildposset_adeprel;
            StringBuilder pchildposset_deprelpath = new StringBuilder();
            pchildposset_deprelpath.append(pchildposset);
            pchildposset_deprelpath.append(" ");
            pchildposset_deprelpath.append(deprelpath);
            features[index++] = pchildposset_deprelpath;
            StringBuilder pchildposset_pospath = new StringBuilder();
            pchildposset_pospath.append(pchildposset);
            pchildposset_pospath.append(" ");
            pchildposset_pospath.append(pospath);
            features[index++] = pchildposset_pospath;
            StringBuilder pchildposset_position = new StringBuilder();
            pchildposset_position.append(pchildposset);
            pchildposset_position.append(" ");
            pchildposset_position.append(position);
            features[index++] = pchildposset_position;
            StringBuilder pchildposset_leftw = new StringBuilder();
            pchildposset_leftw.append(pchildposset);
            pchildposset_leftw.append(" ");
            pchildposset_leftw.append(leftw);
            features[index++] = pchildposset_leftw;
            StringBuilder pchildposset_leftpos = new StringBuilder();
            pchildposset_leftpos.append(pchildposset);
            pchildposset_leftpos.append(" ");
            pchildposset_leftpos.append(leftpos);
            features[index++] = pchildposset_leftpos;
            StringBuilder pchildposset_rightw = new StringBuilder();
            pchildposset_rightw.append(pchildposset);
            pchildposset_rightw.append(" ");
            pchildposset_rightw.append(rightw);
            features[index++] = pchildposset_rightw;
            StringBuilder pchildposset_rightpos = new StringBuilder();
            pchildposset_rightpos.append(pchildposset);
            pchildposset_rightpos.append(" ");
            pchildposset_rightpos.append(rightpos);
            features[index++] = pchildposset_rightpos;
            StringBuilder pchildposset_leftsiblingw = new StringBuilder();
            pchildposset_leftsiblingw.append(pchildposset);
            pchildposset_leftsiblingw.append(" ");
            pchildposset_leftsiblingw.append(leftsiblingw);
            features[index++] = pchildposset_leftsiblingw;
            StringBuilder pchildposset_leftsiblingpos = new StringBuilder();
            pchildposset_leftsiblingpos.append(pchildposset);
            pchildposset_leftsiblingpos.append(" ");
            pchildposset_leftsiblingpos.append(leftsiblingpos);
            features[index++] = pchildposset_leftsiblingpos;
            StringBuilder pchildposset_rightsiblingw = new StringBuilder();
            pchildposset_rightsiblingw.append(pchildposset);
            pchildposset_rightsiblingw.append(" ");
            pchildposset_rightsiblingw.append(rightsiblingw);
            features[index++] = pchildposset_rightsiblingw;
            StringBuilder pchildposset_rightsiblingpos = new StringBuilder();
            pchildposset_rightsiblingpos.append(pchildposset);
            pchildposset_rightsiblingpos.append(" ");
            pchildposset_rightsiblingpos.append(rightsiblingpos);
            features[index++] = pchildposset_rightsiblingpos;


            //pchildwset + argument features
            StringBuilder pchildwset_aw = new StringBuilder();
            pchildwset_aw.append(pchildwset);
            pchildwset_aw.append(" ");
            pchildwset_aw.append(aw);
            features[index++] = pchildwset_aw;
            StringBuilder pchildwset_apos = new StringBuilder();
            pchildwset_apos.append(pchildwset);
            pchildwset_apos.append(" ");
            pchildwset_apos.append(apos);
            features[index++] = pchildwset_apos;
            StringBuilder pchildwset_adeprel = new StringBuilder();
            pchildwset_adeprel.append(pchildwset);
            pchildwset_adeprel.append(" ");
            pchildwset_adeprel.append(adeprel);
            features[index++] = pchildwset_adeprel;
            StringBuilder pchildwset_deprelpath = new StringBuilder();
            pchildwset_deprelpath.append(pchildwset);
            pchildwset_deprelpath.append(" ");
            pchildwset_deprelpath.append(deprelpath);
            features[index++] = pchildwset_deprelpath;
            StringBuilder pchildwset_pospath = new StringBuilder();
            pchildwset_pospath.append(pchildwset);
            pchildwset_pospath.append(" ");
            pchildwset_pospath.append(pospath);
            features[index++] = pchildwset_pospath;
            StringBuilder pchildwset_position = new StringBuilder();
            pchildwset_position.append(pchildwset);
            pchildwset_position.append(" ");
            pchildwset_position.append(position);
            features[index++] = pchildwset_position;
            StringBuilder pchildwset_leftw = new StringBuilder();
            pchildwset_leftw.append(pchildwset);
            pchildwset_leftw.append(" ");
            pchildwset_leftw.append(leftw);
            features[index++] = pchildwset_leftw;
            StringBuilder pchildwset_leftpos = new StringBuilder();
            pchildwset_leftpos.append(pchildwset);
            pchildwset_leftpos.append(" ");
            pchildwset_leftpos.append(leftpos);
            features[index++] = pchildwset_leftpos;
            StringBuilder pchildwset_rightw = new StringBuilder();
            pchildwset_rightw.append(pchildwset);
            pchildwset_rightw.append(" ");
            pchildwset_rightw.append(rightw);
            features[index++] = pchildwset_rightw;
            StringBuilder pchildwset_rightpos = new StringBuilder();
            pchildwset_rightpos.append(pchildwset);
            pchildwset_rightpos.append(" ");
            pchildwset_rightpos.append(rightpos);
            features[index++] = pchildwset_rightpos;
            StringBuilder pchildwset_leftsiblingw = new StringBuilder();
            pchildwset_leftsiblingw.append(pchildwset);
            pchildwset_leftsiblingw.append(" ");
            pchildwset_leftsiblingw.append(leftsiblingw);
            features[index++] = pchildwset_leftsiblingw;
            StringBuilder pchildwset_leftsiblingpos = new StringBuilder();
            pchildwset_leftsiblingpos.append(pchildwset);
            pchildwset_leftsiblingpos.append(" ");
            pchildwset_leftsiblingpos.append(leftsiblingpos);
            features[index++] = pchildwset_leftsiblingpos;
            StringBuilder pchildwset_rightsiblingw = new StringBuilder();
            pchildwset_rightsiblingw.append(pchildwset);
            pchildwset_rightsiblingw.append(" ");
            pchildwset_rightsiblingw.append(rightsiblingw);
            features[index++] = pchildwset_rightsiblingw;
            StringBuilder pchildwset_rightsiblingpos = new StringBuilder();
            pchildwset_rightsiblingpos.append(pchildwset);
            pchildwset_rightsiblingpos.append(" ");
            pchildwset_rightsiblingpos.append(rightsiblingpos);
            features[index++] = pchildwset_rightsiblingpos;

            //pw + aw + apos
            long plem_aw_apos = (((plem<<20) | aw )<< 10) | apos;
            features[index++] = plem_aw_apos;
            long plem_aw_adeprel = (((plem<<20) | aw )<< 10) | adeprel;
            features[index++] = plem_aw_adeprel;
            int ppos_apos_adeprel = (((ppos<<10) | apos )<< 10) | adeprel;
            features[index++] = ppos_apos_adeprel;
            int pdeprel_apos_adeprel = (((pdeprel<<10) | apos )<< 10) | adeprel;
            features[index++] = pdeprel_apos_adeprel;

            StringBuilder pchilddepset_apos_adeprel= new StringBuilder();
            pchilddepset_apos_adeprel.append(pchilddepset);
            pchilddepset_apos_adeprel.append(" ");
            pchilddepset_apos_adeprel.append(apos);
            pchilddepset_apos_adeprel.append(" ");
            pchilddepset_apos_adeprel.append(adeprel);
            features[index++] = pchilddepset_apos_adeprel;

            StringBuilder pchildposset_apos_adeprel= new StringBuilder();
            pchildposset_apos_adeprel.append(pchildposset);
            pchildposset_apos_adeprel.append(" ");
            pchildposset_apos_adeprel.append(apos);
            pchildposset_apos_adeprel.append(" ");
            pchildposset_apos_adeprel.append(adeprel);
            features[index++] = pchildposset_apos_adeprel;

            StringBuilder pchildwset_apos_adeprel= new StringBuilder();
            pchildwset_apos_adeprel.append(pchildwset);
            pchildwset_apos_adeprel.append(" ");
            pchildwset_apos_adeprel.append(apos);
            pchildwset_apos_adeprel.append(" ");
            pchildwset_apos_adeprel.append(adeprel);
            features[index++] = pchildwset_apos_adeprel;

            StringBuilder pchildwset_aw_adeprel= new StringBuilder();
            pchildwset_aw_adeprel.append(pchildwset);
            pchildwset_aw_adeprel.append(" ");
            pchildwset_aw_adeprel.append(aw);
            pchildwset_aw_adeprel.append(" ");
            pchildwset_aw_adeprel.append(adeprel);
            features[index++] = pchildwset_aw_adeprel;

            long plem_apos_adeprel = (((plem<<10) | apos )<< 10) | adeprel;
            features[index++] = plem_apos_adeprel;
        }

        //build feature vector for predicate disambiguation module
        if (state.equals("PD")) {
            int index = 0;
            features[index++] = pw;
            features[index++] = ppos;
            features[index++] = pdeprel;
        //    for (String feat:pfeats.split("|"))
         //       features[index++] = feat;
            features[index++] = pprw;
            features[index++] = pprpos;
          //  for (String feat:pprfeats.split("|"))
          //      features[index++] = feat;
            features[index++] = pchilddepset;
            features[index++] = pchildposset;
            features[index++] = pchildwset;
        }

        return features;
    }

    //TODO dependency subcat frames should contain core dep labels (not all of them)
    // todo move this as a private (non-static) member to Predicate class (i.e. member depSubCat; private function: getDepSubCat)
    private static String getDepSubCat(int pIdx, TreeSet<Integer>[] sentenceReverseDepHeads,
                                      int[] sentenceDepLabels) {
        StringBuilder subCat = new StringBuilder();
        TreeSet<Integer> subCatElements= new TreeSet<Integer>();
        if (!sentenceReverseDepHeads[pIdx].equals(null)) {
            for (int child : sentenceReverseDepHeads[pIdx])
                subCatElements.add(sentenceDepLabels[child]);
        }

        for (int child: subCatElements) {
            subCat.append(child);
            subCat.append("\t");
        }
        return subCat.toString().trim();
    }

    private static String getChildSet(int pIdx, TreeSet<Integer>[] sentenceReverseDepHeads,
                                     int[] collection) {
        StringBuilder childSet = new StringBuilder();
        TreeSet<Integer> children= new TreeSet<Integer>();
        if (!sentenceReverseDepHeads[pIdx].equals(null)) {
            for (int child : sentenceReverseDepHeads[pIdx])
                children.add(collection[child]);
        }
        for (int child: children) {
            childSet.append(child);
            childSet.append("\t");
        }
        return childSet.toString().trim();
    }

    private static int getLeftMostDependentIndex(int aIdx, TreeSet<Integer>[] sentenceReverseDepHeads) {
        if (!sentenceReverseDepHeads[aIdx].equals(null))
            return sentenceReverseDepHeads[aIdx].last();
        return -1;
    }

    private static int getRightMostDependentIndex(int aIdx, TreeSet<Integer>[] sentenceReverseDepHeads) {
        if (!sentenceReverseDepHeads[aIdx].equals(null))
            return sentenceReverseDepHeads[aIdx].first();
        return -1;
    }

    private static int getLeftSiblingIndex(int aIdx, TreeSet<Integer>[] sentenceReverseDepHeads) {
        if (!sentenceReverseDepHeads[aIdx].equals(null) && sentenceReverseDepHeads[aIdx].higher(aIdx) != null)
            return sentenceReverseDepHeads[aIdx].higher(aIdx);
        return -1;
    }

    private static int getRightSiblingIndex(int aIdx, TreeSet<Integer>[] sentenceReverseDepHeads) {
        if (!sentenceReverseDepHeads[aIdx].equals(null) && sentenceReverseDepHeads[aIdx].lower(aIdx) != null)
            return sentenceReverseDepHeads[aIdx].lower(aIdx);
        return -1;
    }

}
