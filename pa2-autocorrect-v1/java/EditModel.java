import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

public class EditModel {

  public Map<String, Integer> editCounts;

  public Set<String> vocabulary;

  public EditModel(String fileName, HolbrookCorpus corpus) {
    readEditCounts(fileName);
    setVocabulary(corpus);
  }

  public void setVocabulary(HolbrookCorpus corpus) {
    vocabulary = new HashSet<String>();
    for(Sentence sentence : corpus.getData()) {
      for(Datum datum : sentence) {
        vocabulary.add(datum.getWord());
      }
    }
  }


  public char [] alphabet = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j',
                             'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't',
                             'u', 'v', 'w', 'x', 'y', 'z'};
  
  Map<String, Integer> readEditCounts(String filename) {
    editCounts = new HashMap<String, Integer>();
    try {
      BufferedReader br = new BufferedReader(new FileReader(filename));
      String line;
      while((line = br.readLine()) != null) {
        String [] contents = line.split("\t");
        editCounts.put(contents[0], Integer.parseInt(contents[1]));
      }
    } catch (IOException e) {
      System.err.println("Error reading edit counts: " + e.getMessage());
      e.printStackTrace();
    } 
    return editCounts;
  }

  /** Returns how many times substring s1 is edited as s2. 
    * For example editCount(e,i) counts how many times the correct 'i' is 
    * misspelled as an 'e'.
    */
  Integer editCount(String s1, String s2) {
    Integer count = editCounts.get(s1 + "|" + s2);
    if(count == null) {
      return new Integer(0);
    } else {
      return count;
    }
  }



  /** Computes p(x| word) for x in the dictionary within edit distance 
    * one of word. 
    */
  Map<String, Double> editProbabilities(String word) {
    HashMap<String, Integer> counts = new HashMap<String, Integer>();
    for(int i = 0; i <= word.length(); i++) {
      String a = word.substring(0, i);
      String b = word.substring(i);
      // deletions. 
      if(b.length() > 0) {
        String deleted = a + b.substring(1);
        if(vocabulary.contains(deleted)) {
          String tail = a.length () > 0 ? a.substring(a.length() - 1, a.length()) : "";
          String original = String.format("%s%s", tail, b.substring(0,1));
          String replacement = tail;
          int count = editCount(original, replacement);
          int prevCount = counts.containsKey(deleted) ? counts.get(deleted) : 0;
          counts.put(deleted, count + prevCount);
        }
      }

      // transpositions
      if(b.length() > 1) {
        String transposed = String.format("%s%c%c%s", a, b.charAt(1), b.charAt(0), b.substring(2));
        if(vocabulary.contains(transposed)) {
          String original = String.format("%c%c", b.charAt(0), b.charAt(1));
          String replacement = String.format("%c%c", b.charAt(1), b.charAt(0));
          int count = editCount(original, replacement);
          int prevCount = counts.containsKey(transposed) ? counts.get(transposed) : 0;
          counts.put(transposed, count + prevCount);
        }
      }

      // replaces
      if(b.length() > 0) {
        for(char c : alphabet) {
          String replaced = String.format("%s%c%s", a, c, b.substring(1));
          if(vocabulary.contains(replaced)) {
            String original = b.substring(0,1);
            String replacement = String.format("%c",c);
            int count = editCount(original, replacement);
            int prevCount = counts.containsKey(replaced) ? counts.get(replaced) : 0;
            counts.put(replaced, count + prevCount);
          }
        }
      }
      
      // insertions
      for(char c : alphabet) {
        String inserted = String.format("%s%c%s", a, c, b);
        if(vocabulary.contains(inserted)) {
          String tail = a.length() > 0 ? a.substring(a.length() - 1, a.length()) : ""; 
          String original = tail;
          String replacement = String.format("%s%c", tail, c);
          int count = editCount(original, replacement);
          int prevCount = counts.containsKey(inserted) ? counts.get(inserted) : 0;
          counts.put(inserted, count + prevCount);
        }
      }
    }

    // normalize
    HashMap<String, Double> probabilities = new HashMap<String, Double>();
    double total = 0.0;
    for(String token : counts.keySet()) {
      total += counts.get(token);
    }
    // For p(x|x)
    Integer selfCount = new Integer((int)(9*total));
    if(selfCount == 0) {
      selfCount = 1;
    }
    counts.put(word, selfCount); // p(x|x) = .9 
    total += selfCount;
    for(String token : counts.keySet()) {
      double p = counts.get(token) / total; 
      probabilities.put(token, p);
    }

    return probabilities;
  }

  public static int dameraulevenshtein(String s1, String s2) {
    return EditDistance.editDistance(s1, s2, true);
  }
}
