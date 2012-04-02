import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SpellCorrect {
  EditModel editModel;
  LanguageModel languageModel;

  public SpellCorrect() {
    HolbrookCorpus corpus = new HolbrookCorpus("../data/holbrook-tagged-train.dat");
    editModel = new EditModel("../data/count_1edit.txt", corpus);
    languageModel = null;
  }

  public SpellCorrect(LanguageModel lm, HolbrookCorpus corpus) {
    editModel = new EditModel("../data/count_1edit.txt", corpus);
    languageModel = lm;
  }

  /** corrects a whole corpus, returns a JSON representation of the output. */
  String correctCorpus(HolbrookCorpus corpus, int partId) {
    StringBuilder sb = new StringBuilder();
    sb.append(String.format("[[%d]", partId));
    List<Sentence> data = corpus.getData();
    for(int i = 0; i < data.size(); i++) {
      if(i != 0) {
        sb.append(",");
      }
      sb.append("[");
      Sentence sentence = data.get(i);
      List<String> uncorrected = sentence.getErrorSentence();
      List<String> corrected = correctSentence(uncorrected);
      for(int j = 0; j < corrected.size(); j++) {
        String str = corrected.get(j);
        if(j != 0) {
          sb.append(",");
        }
        sb.append(String.format("\"%s\"", str));
      }
      sb.append("]"); // end of sentence
    }
    sb.append("]"); // end of corpus
    return sb.toString();
  }

  List<String> correctSentence(List<String> sentence) {
    if(sentence.isEmpty()) {
      return new ArrayList<String>();
    }
    int argmax_i = 0;
    String argmax_w = sentence.get(0);
    double max = Double.NEGATIVE_INFINITY;
    double maxlm = Double.NEGATIVE_INFINITY;
    double maxedit = Double.NEGATIVE_INFINITY;

    // skip first and last tokens.
    for(int i = 1; i < sentence.size() - 1; i++) {
      String word = sentence.get(i);
      Map<String, Double> editProbs = editModel.editProbabilities(word);
      for(String alternative : editProbs.keySet()) {
        // skip non-edits:
        if(alternative.equals(word)) 
          continue;
        sentence.set(i, alternative);
        double lmscore = languageModel.score(sentence);
        double editscore = Math.log(editProbs.get(alternative));
        double score = lmscore + editscore;
        if(score >= max) {
          max = score;
          maxlm = lmscore;
          maxedit = editscore;
          argmax_i = i;
          argmax_w = alternative;
        }
      }
      sentence.set(i, word); // restore sentence to original state
    }
    List<String> argmax = new ArrayList<String>(sentence);
    argmax.set(argmax_i, argmax_w);
    return argmax;
  }


  public SpellingResult evaluate(HolbrookCorpus corpus) {
    int numCorrect = 0;
    int numTotal = 0;
    List<Sentence> testData = corpus.generateTestCases();
    for(Sentence sentence : testData) {
      if(sentence.isEmpty()) 
        continue;
      List<String> errorSentence = sentence.getErrorSentence(); // with misspelling
      List<String> correct = sentence.getCorrectSentence(); // without misspelling


      List<String> hypothesis = correctSentence(errorSentence); // hypothesis


      if(sentence.isCorrection(hypothesis)) {
        numCorrect++;
      } 
      numTotal++;
    }
    return new SpellingResult(numCorrect, numTotal);
  }


  public static void eval() {
    String trainPath = "../data/holbrook-tagged-train.dat";
    HolbrookCorpus trainingCorpus = new HolbrookCorpus(trainPath);

    String devPath = "../data/holbrook-tagged-dev.dat";
    HolbrookCorpus devCorpus = new HolbrookCorpus(devPath);

    System.out.println("Uniform Language Model: ");
    UniformLanguageModel uniformLM = new UniformLanguageModel(trainingCorpus);
    SpellCorrect uniformSpell = new SpellCorrect(uniformLM, trainingCorpus); 
    SpellingResult uniformOutcome = uniformSpell.evaluate(devCorpus);
    System.out.println(uniformOutcome.toString());

    System.out.println("Laplace Unigram Language Model: ");
    LaplaceUnigramLanguageModel laplaceUnigramLM = new LaplaceUnigramLanguageModel(trainingCorpus);
    SpellCorrect laplaceUnigramSpell = new SpellCorrect(laplaceUnigramLM, trainingCorpus);
    SpellingResult laplaceUnigramOutcome = laplaceUnigramSpell.evaluate(devCorpus);
    System.out.println(laplaceUnigramOutcome.toString());

    System.out.println("Laplace Bigram Language Model: ");
    LaplaceBigramLanguageModel laplaceBigramLM = new LaplaceBigramLanguageModel(trainingCorpus);
    SpellCorrect laplaceBigramSpell = new SpellCorrect(laplaceBigramLM, trainingCorpus);
    SpellingResult laplaceBigramOutcome = laplaceBigramSpell.evaluate(devCorpus);
    System.out.println(laplaceBigramOutcome.toString());

    System.out.println("Stupid Backoff Language Model: ");
    StupidBackoffLanguageModel sbLM = new StupidBackoffLanguageModel(trainingCorpus);
    SpellCorrect sbSpell = new SpellCorrect(sbLM, trainingCorpus);
    SpellingResult sbOutcome = sbSpell.evaluate(devCorpus);
    System.out.println(sbOutcome.toString());

    System.out.println("Custom Language Model: ");
    CustomLanguageModel customLM = new CustomLanguageModel(trainingCorpus);
    SpellCorrect customSpell = new SpellCorrect(customLM, trainingCorpus);
    SpellingResult customOutcome = customSpell.evaluate(devCorpus);
    System.out.println(customOutcome.toString());
  }

  public static void main(String[] args) {
    SpellCorrect.eval();
  }
}
