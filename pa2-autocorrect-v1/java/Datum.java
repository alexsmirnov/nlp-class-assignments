// holds a word and an alternative.
// anything else?
public class Datum {
  protected String word; // correct word
  protected String error; // spelling error

  public Datum() {
    word = "";
    error = "";
  }
  
  public Datum(String w, String e) {
    word = w;
    error = e;
  }

  // returns a new datum with error = "";
  public Datum fixError() {
    return new Datum(word, "");
  }

  // returns true of this Datum has a spelling error
  public boolean hasError() {
    return !(error == null || error.equals(""));
  }

  public void setWord(String w) {
    word = w;
  }

  public String getWord() {
    return word;
  }
  
  public void setError(String e) {
    error = e;
  }

  public String getError() {
    return error;
  }

  // returns true if the error is within edit distance one and contains no numerics/punctuation
  public boolean isValidTest() {
    if(!hasError()) {
      return false;
    }
    int distance = EditDistance.editDistance(word, error, true);
    if(distance > 1) {
      return false;
    }
    String regex = ".*[^a-zA-Z].*"; 
    if(word.matches(regex) || error.matches(regex)) {
      return false;
    }
    return true;
  }

  public String toString() {
    return String.format("word: \"%s\" error: \"%s\" distance: %d valid: %b", word, error, EditDistance.editDistance(word, error, true), isValidTest());
  }
}
