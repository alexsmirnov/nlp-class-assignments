
// contains numCorrect, numTotal, getAccuracy()
public class SpellingResult {

  private int numCorrect;
  private int numTotal;

  public SpellingResult() {
    numCorrect = 0;
    numTotal = 0;
  }

  public SpellingResult(int correct, int total) {
    numCorrect = correct;
    numTotal = total;
  }

  public double getAccuracy() {
    return ((double) numCorrect) / numTotal;
  }

  public String toString() {
    return String.format("correct: %d total: %d accuracy: %f", numCorrect, numTotal, getAccuracy());
  }

  public int getNumCorrect() {
    return numCorrect;
  }
  
  public void setNumCorrect(int correct) {
    numCorrect = correct;
  }

  public int getNumTotal() {
    return numTotal;
  }

  public void setNumTotal(int total) {
    numTotal = total;
  }
}
