import java.util.*;
import java.io.*;


/** Do not modify this class
 *  The submit script does not use this class 
 *  It directly calls the methods of FeatureFactory and MEMM classes.
 */
public class NER {
    
    public static void main(String[] args) throws IOException {
	if (args.length < 2) {
	    System.out.println("USAGE: java -cp classes NER ../data/train ../data/dev");
	    return;
	}	    

	String print = "";
	if (args.length > 2 && args[2].equals("-print")) {
	    print = "-print";
	}

	FeatureFactory ff = new FeatureFactory();
	// read the train and test data
	List<Datum> trainData = ff.readData(args[0]);
	List<Datum> testData = ff.readData(args[1]);

	// add the features
	List<Datum> trainDataWithFeatures = ff.setFeaturesTrain(trainData);
	List<Datum> testDataWithFeatures = ff.setFeaturesTest(testData);

	// write the data with the features into JSON files
	ff.writeData(trainDataWithFeatures, "trainWithFeatures");
	ff.writeData(testDataWithFeatures, "testWithFeatures");

	// run MEMM
        ProcessBuilder pb =
	    new ProcessBuilder("java", "-cp", "classes", "-Xmx1G", "MEMM", "trainWithFeatures.json", "testWithFeatures.json", print);
        pb.redirectErrorStream(true);
        Process proc = pb.start();

	BufferedReader br = new BufferedReader(new InputStreamReader(proc.getInputStream()));
	String line = br.readLine();
	while (line != null) {
	    System.out.println(line);
	    line = br.readLine();
	}
	
    }
}