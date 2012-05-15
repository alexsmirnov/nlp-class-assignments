import java.io.*;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;


public class IRSystem {
  List<String> titles;
  ArrayList<ArrayList<String>> documents;
  ArrayList<String> vocab;
  HashMap<String, ArrayList<Integer>> invertedIndex;

  // For the text pre-processing.
  PorterStemmer stemmer;

  CounterMap<String, Integer> tfidf;  // word and document index
  Counter<String> docFreq;

  public IRSystem(String dataDir) {
    stemmer = new PorterStemmer();
    readData(dataDir);
  }

  void index() {
    System.out.println("Indexing...");
    /** TODO: Create an inverted index.
     *  Granted this may not be a linked list as in a proper
     *  implementation.
     *  Some helpful instance variables:
     *    * documents
     */


    invertedIndex = new HashMap<String, ArrayList<Integer>>();
    for (String word : vocab) {
      invertedIndex.put(word, new ArrayList<Integer>());
    }

    /* End TODO */
  }


  void computeTFIDF()
  {
    /** TODO: Compute and store TF-IDF values for words and documents.
     *  Recall that you can make use of the instance variables:
     *   * vocab
     *   * documents
     * NOTE that you probably do *not* want to store a value for every
     * word-document pair, but rather just for those pairs where a
     * word actually occurs in the document.
     */
    System.out.println("Computing TF-IDF...");

    tfidf = new CounterMap<String, Integer>();
    for (String word : vocab) {
      for (int d = 0; d < documents.size(); d++) {
        tfidf.setCount(word, new Integer(d), 0.0);
      }
    }

    /* End TODO */
  }


  double getTFIDF(String word, int doc) {
    double tfidf_score = 0.0;
    /** TODO: Return the tf-idf weigthing for the given word (string) and
     * document index.
     */


    /* End TODO */
    return tfidf_score;
  }


  public double getTFIDFUnstemmed(String word, int doc) {
    /**
     * This function gets the TF-IDF of an *unstemmed* word in a document.
     * Stems the word and then calls get_tfidf. You should *not* need to
     * change this interface, but it is necessary for submission.
     */
    word = stemmer.stem(word);
    return getTFIDF(word, doc);
  }


  ArrayList<Integer> getPosting(String word) {
    ArrayList<Integer> posting = new ArrayList<Integer>();
    /** TODO: Return the list of postings for a word. */


    /** End TODO */
    return posting;
  }


  public ArrayList<Integer> getPostingUnstemmed(String word) {
    /**
     * Given a word, this *stems* the word and then calls get_posting on the
     * stemmed word to get its postings list. You should *not* need to change
     * this function. It is needed for submission.
     */
    word = stemmer.stem(word);
    return getPosting(word);
  }


  ArrayList<Integer> booleanRetrieve(ArrayList<String> query)
  {
    /**
     * Given a query in the form of a list of *stemmed* words, this returns
     * the list of documents in which *all* of those words occur (ie an AND
     * query).
     */

    /** TODO: Implement Boolean retrieval. You will want to use your
     * inverted index that you created in index().
     * Right now this just returns all the possible documents!
     * Return an empty list if the query does not return any documents.
     */

    ArrayList<Integer> docs = new ArrayList<Integer>();
    for (int d = 0; d < documents.size(); d++) {
        docs.add(new Integer(d));
    }


    /* End TODO */
    Collections.sort(docs);
    return docs;
  }


  PriorityQueue<Integer> rankRetrieve(ArrayList<String> query) {
    double scores[] = new double[documents.size()];

    /*************************************************************/
    /** TODO: Implement cosine similarity. */
    /*  For now just implements Jaccard similarity. */
    HashSet<String> wordsInQuery = new HashSet<String>();
    wordsInQuery.addAll(query);
    HashSet<String> wordsInDoc;
    HashSet<String> setUnion;
    HashSet<String> setIntersection;
    for (int d = 0; d < documents.size(); d++) {
      wordsInDoc = new HashSet<String>();
      wordsInDoc.addAll(documents.get(d));
      setUnion = new HashSet<String>(wordsInDoc);
      setUnion.addAll(wordsInQuery);
      setIntersection = new HashSet<String>(wordsInQuery);
      setIntersection.retainAll(wordsInDoc);
      scores[d] = ((double) setIntersection.size()) / ((double) setUnion.size());
    }

    /* End TODO */

    PriorityQueue<Integer> pq = new PriorityQueue<Integer>();
    for (int d = 0; d < scores.length; d++) {
      pq.add(new Integer(d), scores[d]);
    }

    PriorityQueue<Integer> topTen = new PriorityQueue<Integer>();
    for (int d = 0; d < 10; d++) {
      double priority = pq.getPriority();
      topTen.add(pq.next(), priority);
    }

    return topTen;
  }


  /** Given a query string, processes the string and returns the list of
   * lowercase, alphanumeric, stemmed words in the string.
   */
  ArrayList<String> processQuery(String queryString)
  {
    // lowercase
    queryString = queryString.toLowerCase();
    ArrayList<String> query = new ArrayList<String>();
    for (String s : queryString.split("\\s+")) {
      // remove non alphanumeric characters
      s = s.replaceAll("[^a-zA-Z0-9]", "");
      // stem s
      s = stemmer.stem(s);
      if (!s.equals(""))
        query.add(s);
    }
    return query;
  }


  /** Given a string, this will process and then return the list of matching
   * documents found by booleanRetrieve()
   */
  ArrayList<Integer> queryRetrieve(String queryString)
  {
    ArrayList<String> query = processQuery(queryString);
    return booleanRetrieve(query);
  }


  /** Given a string, this will process and then return the list of the
   * top matching documents found by rankRetrieve()
   */
  PriorityQueue<Integer> queryRank(String queryString)
  {
    ArrayList<String> query = processQuery(queryString);
    return rankRetrieve(query);
  }


  void getUniqWords() {
    HashSet<String> uniqWords = new HashSet<String>();
    for (ArrayList<String> document : documents) {
      for (String word : document) {
        uniqWords.add(word);
      }
    }
    vocab = new ArrayList<String>(uniqWords);
  }


  ArrayList<String> readRawFile(String title, BufferedReader input, String stemmedDirName)
  {
    ArrayList<String> document = new ArrayList<String>();
    /* Output buffer for stemmed document. */
    String stemmedFile = stemmedDirName + "/" + title + ".txt";
    BufferedWriter output = null;
    try {
      output = new BufferedWriter(new FileWriter(new File(stemmedFile)));
    } catch(IOException e) {
      System.err.println("Error opening stemmed cache file for " + title);
      e.printStackTrace();
      System.exit(1);
    }

    try {
      String line;
      while ((line = input.readLine()) != null) {
        // make sure everything is lowercase
        line = line.toLowerCase();
        // split on whitespace
        ArrayList<String> tmp = new ArrayList<String>();
        boolean emptyLine = true;
        for (String s : line.split("\\s+")) {
          // Remove non alphanumeric characters
          s = s.replaceAll("[^a-zA-Z0-9]", "");
          // Stem word.
          s = stemmer.stem(s);

          if (!s.equals("")) {
            tmp.add(s);

            /* Write to stemmed file. */
            if (!emptyLine) {
              output.write(" ", 0, 1);
            }
            output.write(s, 0, s.length());
            emptyLine = false;
          }
        }

        /* Write new line to stemmed file (if you put anything on the line). */
        if (output != null && !emptyLine) {
          output.newLine();
        }

        document.addAll(tmp);
      }
    } catch(IOException e) {
      System.err.println("Error closing stemmed cache file for " + title);
      e.printStackTrace();
      System.exit(1);
    }

    /* Close the stemmed file. */
    try {
      output.close();
    } catch(IOException e) {
      System.err.println("Error closing stemmed: " + title);
      e.printStackTrace();
      System.exit(1);
    }

    return document;
  }

  ArrayList<String> readStemmedFile(String title, BufferedReader input)
  {
    ArrayList<String> document = new ArrayList<String>();
    try {
      String line;
      while ((line = input.readLine()) != null) {
        // make sure everything is lowercase
        line = line.toLowerCase();
        // split on whitespace
        ArrayList<String> tmp = new ArrayList<String>();
        for (String s : line.split("\\s+")) {
          tmp.add(s);
        }
        document.addAll(tmp);
      }
    } catch(IOException e) {
      System.err.println("Error processing document: " + title);
      e.printStackTrace();
      System.exit(1);
    }
    return document;
  }


  void readStemmedData(String dirName) {
    System.out.println("Already stemmed!");
    titles = new ArrayList<String>();
    documents = new ArrayList<ArrayList<String>>();
    ArrayList<File> files = new ArrayList<File>();
    for (File f : new File(dirName).listFiles()) {
      if (f.getName().endsWith(".txt") && !f.getName().startsWith("."))
        files.add(f);
    }
    if (files.size() != 60) {
      System.err.println("There are not 60 documents in ../data/RiderHaggard/stemmed\n"
          + "Remove ../data/RiderHaggard/stemmed directory and re-run.");
      System.exit(1);
    }
    for (File f : files) {
      try {
        BufferedReader input = new BufferedReader(new FileReader(f));
        String title = f.getName().replaceAll("\\.txt", "");
        titles.add(title);
        documents.add(readStemmedFile(title, input));
      } catch(IOException e) {
        e.printStackTrace();
        System.exit(1);
      }
    }
  }

  void readRawData(String dirName) {
    System.out.println("Stemming Documents...");
    String rawDirName = dirName + "/raw";
    String stemmedDirName = dirName + "/stemmed";
    new File(stemmedDirName).mkdir();
    titles = new ArrayList<String>();
    documents = new ArrayList<ArrayList<String>>();
    int i = 1;

    for (File f : new File(rawDirName).listFiles()) {
      if (f.getName().endsWith(".txt") && !f.getName().startsWith(".")) {
        System.out.println("Filename: " + f.getName());
        try {
          BufferedReader input = new BufferedReader(new FileReader(f));
          String title = f.getName().replaceAll(" \\d+\\.txt", "");
          titles.add(title);
          System.out.println("    Doc " + i + ": " + title);
          documents.add(readRawFile(title, input, stemmedDirName));
        } catch(IOException e) {
          e.printStackTrace();
          System.exit(1);
        }
        i++;
      }
    }
  }

  void readData(String dirName) {
    System.out.println("Reading in documents...");
    String[] subdirs = new File(dirName).list();
    boolean haveStemmed = false;
    for (String subdir : subdirs) {
      if (subdir.equals("stemmed")) {
        haveStemmed = true;
        break;
      }
    }
    if (haveStemmed) {
      readStemmedData(dirName + "/stemmed");
    } else {
      readRawData(dirName);
    }

    ArrayList<String> titlesSorted = new ArrayList<String>(titles);
    Collections.sort(titlesSorted);
    ArrayList<ArrayList<String>> documentsSorted = new ArrayList<ArrayList<String>>();
    for (int i = 0; i < titles.size(); i++) {
      String title = titlesSorted.get(i);
      documentsSorted.add(documents.get(titles.indexOf(title)));
    }

    documents = documentsSorted;
    titles = titlesSorted;

    getUniqWords();
  }


  String getTitle(int titleIndex) {
    return titles.get(titleIndex);
  }


  public static void runTests(IRSystem irSys) {
    ArrayList<String> questions = null;
    ArrayList<String> solutions = null;

    try {
      BufferedReader input = new BufferedReader(new FileReader(
            new File("../data/queries.txt")));
      questions = new ArrayList<String>();
      String line;
      while ((line = input.readLine()) != null) {
        questions.add(line);
      }
    } catch (IOException e) {
      System.err.println("Error reading ../data/queries.txt: "
          + e.getMessage());
    }
    try {
      BufferedReader input = new BufferedReader(new FileReader(
            new File("../data/solutions_java.txt")));
      solutions = new ArrayList<String>();
      String line;
      while ((line = input.readLine()) != null) {
        solutions.add(line);
      }
    } catch (IOException e) {
      System.err.println("Error reading ../data/solutions_java.txt: "
          + e.getMessage());
    }

    double epsilon = 1E-4;
    int numTests = solutions.size();
    for (int part = 0; part < numTests; part++) {

      int numCorrect = 0;
      int numTotal = 0;

      String problem = questions.get(part);
      String soln = solutions.get(part);

      if (part == 0) {    // Inverted Index test
        System.out.println("Inverted Index Test");

        String[] words = problem.split(", ");
        String[] golds = soln.split("; ");

        for (int i = 0; i < words.length; i++) {
          numTotal++;
          String word = words[i];
          HashSet<Integer> guess = new HashSet<Integer>(irSys.getPostingUnstemmed(word));
          String[] goldList = golds[i].split(", ");
          HashSet<Integer> goldSet = new HashSet<Integer>();
          for (String s : goldList) {
            goldSet.add(new Integer(s));
          }
          if (guess.equals(goldSet)) {
            numCorrect++;
          }
        }
      } else if (part == 1) {   // Boolean retrieval test
        System.out.println("Boolean Retrieval Test");

        String[] queries = problem.split(", ");
        String[] golds = soln.split("; ");
        for (int i = 0; i < queries.length; i++) {
          numTotal++;
          String query = queries[i];
          HashSet<Integer> guess = new HashSet<Integer>(irSys.queryRetrieve(query));
          String[] goldList = golds[i].split(", ");
          HashSet<Integer> goldSet = new HashSet<Integer>();
          for (String s : goldList) {
            goldSet.add(new Integer(s));
          }
          if (guess.equals(goldSet)) {
            numCorrect++;
          }
        }

      } else if (part == 2) {   // TF-IDF test
        System.out.println("TF-IDF Test");

        String[] queries = problem.split("; ");
        String[] golds = soln.split(", ");
        for (int i = 0; i < queries.length; i++) {
          numTotal++;

          String[] query = queries[i].split(", ");
          double guess = irSys.getTFIDFUnstemmed(query[0],
              new Integer(query[1]).intValue());

          double gold = new Double(golds[i]).doubleValue();
          if (guess >= gold - epsilon && guess <= gold + epsilon) {
            numCorrect++;
          }
        }
      } else if (part == 3) {
        System.out.println("Cosine Similarity Test");

        String[] queries = problem.split(", ");
        String[] golds = soln.split("; ");
        for (int i = 0; i < queries.length; i++) {
          numTotal++;

          PriorityQueue<Integer> guess = irSys.queryRank(queries[i]);
          double score = guess.getPriority();
          Integer docId = guess.next();

          String[] topGold = golds[i].split(", ");
          Integer topGoldId = new Integer(topGold[0]);
          double topScore = new Double(topGold[1]).doubleValue();
          
          if (docId.intValue() == topGoldId.intValue() &&
              score >= topScore - epsilon && score <= topScore + epsilon) {
            numCorrect++;
          }
        }
      }

      String feedback = numCorrect + "/" + numTotal + " Correct. " +
        "Accuracy: " + (double)numCorrect / numTotal;
      int points = 0;
      if (numCorrect == numTotal) points = 3;
      else if (numCorrect > 0.75 * numTotal) points = 2;
      else if (numCorrect > 0) points = 1;
      else points = 0;

      System.out.println("    Score: " + points + " Feedback: " + feedback);
    }

  }


  public static void main(String[] args) {
    String dataDir;

    IRSystem irSys = new IRSystem("../data/RiderHaggard");
    irSys.index();
    irSys.computeTFIDF();

    if (args.length == 0) {
      runTests(irSys);
    } else {
      String query = "";
      boolean haveAdded = false;
      for (String s : args) {
        if (haveAdded) {
          query += " ";
        }
        query += s;
        haveAdded = true;
      }
      PriorityQueue<Integer> results = irSys.queryRank(query);
      System.out.println("Best matching documents to '" + query + "':");
      int numResults = results.size();
      for (int i = 0; i < numResults; i++) {
        double score = results.getPriority();
        String title = irSys.getTitle(results.next().intValue());
        System.out.println(title + ": " + score);
      }
    }
  }
}
