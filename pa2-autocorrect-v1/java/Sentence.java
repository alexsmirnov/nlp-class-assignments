import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

// wraps a List of datums.
public class Sentence implements Iterable<Datum> {
  protected ArrayList<Datum> data;

  public Sentence() {
    data = new ArrayList<Datum>();
  }

  // soft copy
  public Sentence(Sentence sentence) {
    data = new ArrayList<Datum>(sentence.data);
  }

  public Datum get(int i) {
    return data.get(i);
  }

  public void set(int i, Datum d) {
    data.set(i, d);
  }

  public int size() {
    return data.size();
  }

  public void add(Datum datum) {
    data.add(datum);
  }

  public boolean isEmpty() {
    return data.isEmpty();
  }

  public Iterator<Datum> iterator() {
    return data.iterator();
  }

  // returns the sentence as written, flattened into list of strings
  public List<String> getErrorSentence() {
    List<String> errorSentence = new ArrayList<String>(size());
    for(Datum datum : data) {
      if(datum.hasError()) {
        errorSentence.add(datum.getError());
      } else {
        errorSentence.add(datum.getWord());
      }
    }
    return errorSentence;
  }

  // returns the index of the first Datum which has an error, -1 otherwise.
  public int getErrorIndex() {
    for(int i = 0; i < data.size(); i++) {
      Datum datum = data.get(i); 
      if(datum.hasError()) {
        return i;
      }
    }
    return -1;
  }

  // returns the corrected sentence
  public List<String> getCorrectSentence() {
    List<String> correctSentence = new ArrayList<String>(size());
    for(Datum datum : data) {
      correctSentence.add(datum.getWord());
    }
    return correctSentence;
  }

  // returns a new sentence with all datum's having error removed.
  public Sentence cleanSentence() {
    Sentence sentence = new Sentence();
    for(Datum datum : data) {
      Datum clean = datum.fixError();
      sentence.add(clean);
    }
    return sentence;
  }

  // checks if a candidate list of strings matches every word with this sentence.
  public boolean isCorrection(List<String> candidate) {
    if(data.size() != candidate.size()) {
      return false; 
    }
    for(int i = 0; i < data.size(); i++) {
      if(!candidate.get(i).equals(data.get(i).getWord())) {
        return false;
      }
    }
    return true;
  }

  public String toString() {
    StringBuffer sb = new StringBuffer();
    for(Datum datum : data) {
      sb.append(datum.toString() + "\n");
    }
    return sb.toString(); 
  }
  
}
