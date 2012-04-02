import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;

public class HolbrookCorpus {

  List<Sentence> data;

  public HolbrookCorpus() { 
    data = new ArrayList<Sentence>();
  }

  public HolbrookCorpus(String fileName) {
    data = readHolbrook(fileName);
  }

  public void slurpString(String contents) {
    String[] lines = contents.split("\n");
    List<Sentence> corpus = new ArrayList<Sentence>();
    for(String line : lines) {
      Sentence sentence = processLine(line);
      if(sentence != null) {
        corpus.add(sentence); 
      }
    }
    this.data = corpus;
  }


  List<Sentence> readHolbrook(String fileName) {
    List<Sentence> corpus = new ArrayList<Sentence>();
    try {
      BufferedReader br = new BufferedReader(new FileReader(fileName));
      String line;
      while((line = br.readLine()) != null) {
        Sentence sentence = processLine(line);
        if(sentence != null) {
          corpus.add(sentence);
        }
      }
    } catch (IOException e) {
      System.err.println(String.format("[HolbrookReader.readHolbrook] Error reading corpus %s: %s", 
          fileName, e.getMessage()));
      e.printStackTrace();
      return null;
    }
    return corpus;  
  }

  // add <s> and </s> markers
  Sentence processLine(String line) {
    Sentence sentence = new Sentence();
    sentence.add(new Datum("<s>", "")); // start of sentence
    // strip whitespace, lowercase, drop punctuation.
    line = line.toLowerCase().trim().replace("\"", "").replace(",","").replace(".","").replace("'","").replace("!","").replace(":","").replace(";","");
    if(line.isEmpty()) {
      return null;
    }
    String [] tokens = line.split(" ");
    int i = 0;
    while(i < tokens.length) {
      String token = tokens[i];
      if(token.equals("<err")) {
        // slice out error substring, process, update i.
        int errorEnd = findNext(tokens, "</err>", i);
        if(errorEnd == -1) {
          System.err.println("Misformatted error string: " + line);
          return null;
        }
        String[] errorTokens = Arrays.copyOfRange(tokens, i, errorEnd + 1);

        Datum datum = processError(errorTokens);
        sentence.add(datum);
        i = errorEnd + 1; // update index accordingly.
      } else {
        Datum datum = new Datum();
        datum.setWord(new String(token)); 
        sentence.add(datum);
        i++;
      } 
    }
    sentence.add(new Datum("</s>","")); // end of sentence
    return sentence;
  }

  // Finds the next occurence of query in tokens, starting from index start. -1 if missing.
  int findNext(String[] tokens, String query, int start) {
    for(int i = start; i < tokens.length; i++) {
      if(tokens[i].equals(query)) 
        return i;
    }
    return -1;
  }

  // process an error block of the form <err targ=w1 w2 w3> e1 e2 e3 </err>
  Datum processError(String[] tokens) {
    Datum datum = new Datum();

    StringBuffer correctWords = new StringBuffer(tokens[1].split("=")[1]);
    int end = 0; // the end of targ string
    while(end < tokens.length) {
      if(tokens[end].endsWith(">"))
        break;
      end++;
    }
    for(int i = 2; i < end; i++) {
      correctWords.append(" ").append(tokens[i]);
    }
    correctWords.deleteCharAt(correctWords.length() - 1);
    datum.setWord(correctWords.toString());
    
    StringBuffer errorWords = new StringBuffer(tokens[end + 1]);
    for(int i = end + 2; i < tokens.length - 1; i++) {
      errorWords.append(" ");
      errorWords.append(tokens[i]);
    }
    datum.setError(errorWords.toString());
    return datum;
  }

  public List<Sentence> getData() {
    return data;
  }


  // returns Sentences with just one error.
  public List<Sentence> generateTestCases() {
    List<Sentence> testCases = new ArrayList<Sentence>();
    for(Sentence sentence : data) {
      Sentence cleanSentence = sentence.cleanSentence();
      for(int i = 0; i < sentence.size(); i++) {
        Datum datum_i = sentence.get(i);
        if(datum_i.hasError() && datum_i.isValidTest()) {
          Sentence testSentence = new Sentence(cleanSentence);
          testSentence.set(i, datum_i); // add the error back.
          testCases.add(testSentence);
        }
      }
    }
    return testCases;
  }

  // prints out corpus, split by lines, no <s> or punctuation, spaces between each token.
  // used to get clean test data to correct.
  public String testToString(boolean isTrain) {
    StringBuilder sb = new StringBuilder();
    List<Sentence> cases = generateTestCases(); 
    for(Sentence sentence : cases) {
      List<String> tokens;
      if(isTrain) { // use errors
        tokens = sentence.getErrorSentence();
      } else { // use corrected
        tokens = sentence.getCorrectSentence();
      }
      for(int i = 1; i < tokens.size() - 1; i++) {
        String token = tokens.get(i);
        if(token == null || token.equals("")) {
          continue;
        }
        if(i != 1) {
          sb.append(" ");
        }
        sb.append(token);
      }
      sb.append("\n");
    } 
    return sb.toString();
  }

  // generates test sentences, then gets clean sentence for each one, JSONs it.
  public String generateJSON(boolean isTrain) { 
    StringBuilder sb = new StringBuilder();
    sb.append("[");
    List<Sentence> cases = generateTestCases();
    for(int i = 0; i < cases.size(); i++) {
      Sentence sentence = cases.get(i);
      if(i != 0) {
        sb.append(",");
      }
      sb.append("[");
      List<String> tokens;
      if(isTrain) {
        tokens = sentence.getErrorSentence();
      } else {
        tokens = sentence.getCorrectSentence();
      }
      for(int j = 0; j < tokens.size(); j++) {
        String token = tokens.get(j);
        if(token == null || token.equals("")) {
          continue;
        } 
        if(j != 0) {
          sb.append(",");
        }
        sb.append(String.format("\"%s\"", token));
      }
      sb.append("]"); // end sentence
    }
    sb.append("]"); // end corpus
    return sb.toString();
  }
}
