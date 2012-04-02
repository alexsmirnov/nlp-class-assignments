import java.util.List;

public class StupidBackoffLanguageModel implements LanguageModel {

  /** Initialize your data structures in the constructor. */
  public StupidBackoffLanguageModel(HolbrookCorpus corpus) {
    train(corpus);
  }

  /** Takes a corpus and trains your language model. 
    * Compute any counts or other corpus statistics in this function.
    */
  public void train(HolbrookCorpus corpus) {

  }

  /** Takes a list of strings as argument and returns the log-probability of the 
    * sentence using your language model. Use whatever data you computed in train() here.
    */
  public double score(List<String> sentence) {
    return 0.0;
  }
}
