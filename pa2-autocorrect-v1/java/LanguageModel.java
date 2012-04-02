import java.util.List;
// returns score for a sentence. (list of words)
// trains on a List<List<String>> corpus.
public interface LanguageModel {

  // train a language model.
  public void train(HolbrookCorpus corpus);

  // language model score (probability) of a given sentence.
  // usually a log-probability. 
  public double score(List<String> sentence);

}
