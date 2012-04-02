//package spinneret.util;
/* Levenshtein in Java, originally from Josh Drew's code at
 * http://joshdrew.com/
 *
 * Updates:
 * November 27 2009 - total rewrite. A few hard-to-squash bugs
 *  were made harder by obscure variable names, some of which were
 * transposed in various places by repeated fixes
 * the code is now more or less an exact mirror of the pseudo code
 * at wikipedia:
 *
 * http://en.wikipedia.org/w/index.php?title=Damerau%E2%80%93Levenshtein_distance&oldid=326708351
 * 
 * November 6 2009 damlevlim failing with zero-length arguments - fixed
 * March 29 2009 updated to correct swapped string length assignments
 *   - a single character mid-string deletion would return lim for example:
 *   - damlevlim("speling", "spelling", 3) would return 3 instead of 1
 * April 16 2009 fix for 'limit' handling for invocations like:
 *   - damlevlim('h', 'hello', 2) = 4 - should be 2!
 * July 18 2009 moved to package spinneret.util
 */

public class Levenshtein
{
	public static int lev(String s, String t, int[] workspace)
	{
		int lenS = s.length();
		int lenT = t.length();
		int lenS1 = lenS + 1;
		int lenT1 = lenT + 1;
		if (lenT1 == 1)
			return lenS1 - 1;
		if (lenS1 == 1)
			return lenT1 - 1;
		int[] dl = workspace;
		int dlIndex = 0;
		int sPrevIndex = 0, tPrevIndex = 0, rowBefore = 0, min = 0, cost = 0, tmp = 0;
		int tri = lenS1 + 2;
		// start row with constant
		dlIndex = 0;
		for (tmp = 0; tmp < lenT1; tmp++)
		{
			dl[dlIndex] = tmp;
			dlIndex += lenS1;
		}
		for (int sIndex = 0; sIndex < lenS; sIndex++)
		{
			dlIndex = sIndex + 1;
			dl[dlIndex] = dlIndex; // start column with constant
			for (int tIndex = 0; tIndex < lenT; tIndex++)
			{
				rowBefore = dlIndex;
				dlIndex += lenS1;
				//deletion
				min = dl[rowBefore] + 1;
				// insertion
				tmp = dl[dlIndex - 1] + 1;
				if (tmp < min)
					min = tmp;
				cost = 1;
				if (s.charAt(sIndex) == t.charAt(tIndex))
					cost = 0;
				// substitution
				tmp = dl[rowBefore - 1] + cost;
				if (tmp < min)
					min = tmp;
				dl[dlIndex] = min;
				/** /
				System.out.println("sPrevIndex=" + sPrevIndex + ", tPrevIndex=" + tPrevIndex + ", sIndex=" + sIndex + ", tIndex=" + tIndex);
				System.out.println("'" + s + "', '" + t + "'");
				for (int v = 0; v < lenT1; v++)
				{
					for (int w = 0; w < lenS1; w++)
						System.out.print(dl[v * lenS1 + w] + " ");
					System.out.println();
				}
				/**/
				tPrevIndex = tIndex;
			}
			sPrevIndex = sIndex;
		}
		return dl[dlIndex];
	}
	public static int levlim(String s, String t, int limit, int[] workspace)
	{
		int lenS = s.length();
		int lenT = t.length();
		if (lenS < lenT)
		{
			if (lenT - lenS >= limit)
				return limit;
		}
		else
			if (lenT < lenS)
				if (lenS - lenT >= limit)
					return limit;
		int lenS1 = lenS + 1;
		int lenT1 = lenT + 1;
		if (lenS1 == 1)
			return (lenT < limit)?lenT:limit;
		if (lenT1 == 1)
			return (lenS < limit)?lenS:limit;
		int[] dl = workspace;
		int dlIndex = 0;
		int sPrevIndex = 0, tPrevIndex = 0, rowBefore = 0, min = 0, tmp = 0, best = 0, cost = 0;
		int tri = lenS1 + 2;
		// start row with constant
		dlIndex = 0;
		for (tmp = 0; tmp < lenT1; tmp++)
		{
			dl[dlIndex] = tmp;
			dlIndex += lenS1;
		}
		for (int sIndex = 0; sIndex < lenS; sIndex++)
		{
			dlIndex = sIndex + 1;
			dl[dlIndex] = dlIndex; // start column with constant
			best = limit;
			for (int tIndex = 0; tIndex < lenT; tIndex++)
			{
				rowBefore = dlIndex;
				dlIndex += lenS1;
				//deletion
				min = dl[rowBefore] + 1;
				// insertion
				tmp = dl[dlIndex - 1] + 1;
				if (tmp < min)
					min = tmp;
				cost = 1;
				if (s.charAt(sIndex) == t.charAt(tIndex))
					cost = 0;
				/** /
				System.out.println("sPrevIndex=" + sPrevIndex + ", tPrevIndex=" + tPrevIndex + ", sIndex=" + sIndex + ", tIndex=" + tIndex);
				System.out.println("'" + s + "', '" + t + "'");
				for (int v = 0; v < lenT1; v++)
				{
					for (int w = 0; w < lenS1; w++)
						System.out.print(dl[v * lenS1 + w] + " ");
					System.out.println();
				}
				/**/
				// substitution
				tmp = dl[rowBefore - 1] + cost;
				if (tmp < min)
					min = tmp;
				dl[dlIndex] = min;
				if (min < best)
					best = min;
				tPrevIndex = tIndex;
			}
			if (best >= limit)
				return limit;
			sPrevIndex = sIndex;
		}
		if (dl[dlIndex] >= limit)
			return limit;
		else
			return dl[dlIndex];

	}
	public static int damlev(String s, String t, int[] workspace)
	{
		int lenS = s.length();
		int lenT = t.length();
		int lenS1 = lenS + 1;
		int lenT1 = lenT + 1;
		if (lenT1 == 1)
			return lenS1 - 1;
		if (lenS1 == 1)
			return lenT1 - 1;
		int[] dl = workspace;
		int dlIndex = 0;
		int sPrevIndex = 0, tPrevIndex = 0, rowBefore = 0, min = 0, cost = 0, tmp = 0;
		int tri = lenS1 + 2;
		// start row with constant
		dlIndex = 0;
		for (tmp = 0; tmp < lenT1; tmp++)
		{
			dl[dlIndex] = tmp;
			dlIndex += lenS1;
		}
		for (int sIndex = 0; sIndex < lenS; sIndex++)
		{
			dlIndex = sIndex + 1;
			dl[dlIndex] = dlIndex; // start column with constant
			for (int tIndex = 0; tIndex < lenT; tIndex++)
			{
				rowBefore = dlIndex;
				dlIndex += lenS1;
				//deletion
				min = dl[rowBefore] + 1;
				// insertion
				tmp = dl[dlIndex - 1] + 1;
				if (tmp < min)
					min = tmp;
				cost = 1;
				if (s.charAt(sIndex) == t.charAt(tIndex))
					cost = 0;
				if (sIndex > 0 && tIndex > 0)
				{
					if (s.charAt(sIndex) == t.charAt(tPrevIndex) && s.charAt(sPrevIndex) == t.charAt(tIndex))
					{
						tmp = dl[rowBefore - tri] + cost;
						// transposition
						if (tmp < min)
							min = tmp;
					}
				}
				// substitution
				tmp = dl[rowBefore - 1] + cost;
				if (tmp < min)
					min = tmp;
				dl[dlIndex] = min;
				/** /
				System.out.println("sPrevIndex=" + sPrevIndex + ", tPrevIndex=" + tPrevIndex + ", sIndex=" + sIndex + ", tIndex=" + tIndex);
				System.out.println("'" + s + "', '" + t + "'");
				for (int v = 0; v < lenT1; v++)
				{
					for (int w = 0; w < lenS1; w++)
						System.out.print(dl[v * lenS1 + w] + " ");
					System.out.println();
				}
				/**/
				tPrevIndex = tIndex;
			}
			sPrevIndex = sIndex;
		}
		return dl[dlIndex];
	}
	public static int damlevlim(String s, String t, int limit, int[] workspace)
	{
		int lenS = s.length();
		int lenT = t.length();
		if (lenS < lenT)
		{
			if (lenT - lenS >= limit)
				return limit;
		}
		else
			if (lenT < lenS)
				if (lenS - lenT >= limit)
					return limit;
		int lenS1 = lenS + 1;
		int lenT1 = lenT + 1;
		if (lenS1 == 1)
			return (lenT < limit)?lenT:limit;
		if (lenT1 == 1)
			return (lenS < limit)?lenS:limit;
		int[] dl = workspace;
		int dlIndex = 0;
		int sPrevIndex = 0, tPrevIndex = 0, rowBefore = 0, min = 0, tmp = 0, best = 0, cost = 0;
		int tri = lenS1 + 2;
		// start row with constant
		dlIndex = 0;
		for (tmp = 0; tmp < lenT1; tmp++)
		{
			dl[dlIndex] = tmp;
			dlIndex += lenS1;
		}
		for (int sIndex = 0; sIndex < lenS; sIndex++)
		{
			dlIndex = sIndex + 1;
			dl[dlIndex] = dlIndex; // start column with constant
			best = limit;
			for (int tIndex = 0; tIndex < lenT; tIndex++)
			{
				rowBefore = dlIndex;
				dlIndex += lenS1;
				//deletion
				min = dl[rowBefore] + 1;
				// insertion
				tmp = dl[dlIndex - 1] + 1;
				if (tmp < min)
					min = tmp;
				cost = 1;
				if (s.charAt(sIndex) == t.charAt(tIndex))
					cost = 0;
				if (sIndex > 0 && tIndex > 0)
				{
					if (s.charAt(sIndex) == t.charAt(tPrevIndex) && s.charAt(sPrevIndex) == t.charAt(tIndex))
					{
						tmp = dl[rowBefore - tri] + cost;
						// transposition
						if (tmp < min)
							min = tmp;
					}
				}
				// substitution
				tmp = dl[rowBefore - 1] + cost;
				if (tmp < min)
					min = tmp;
				dl[dlIndex] = min;
				if (min < best)
					best = min;
				/** /
				System.out.println("sPrevIndex=" + sPrevIndex + ", tPrevIndex=" + tPrevIndex + ", sIndex=" + sIndex + ", tIndex=" + tIndex);
				System.out.println("'" + s + "', '" + t + "'");
				for (int v = 0; v < lenT1; v++)
				{
					for (int w = 0; w < lenS1; w++)
						System.out.print(dl[v * lenS1 + w] + " ");
					System.out.println();
				}
				/**/
				tPrevIndex = tIndex;
			}
			if (best >= limit)
				return limit;
			sPrevIndex = sIndex;
		}
		if (dl[dlIndex] >= limit)
			return limit;
		else
			return dl[dlIndex];
	}
	public static int[] getWorkspace(int sl, int tl)
	{
		return new int[(sl + 1) * (tl + 1)];
	}
	private final static int[] ZERO_LENGTH_INT_ARRAY = new int[0];
	public static int lev(String s, String t)
	{
		if (s != null && t != null)
			return lev(s, t, getWorkspace(s.length(), t.length()));
		else
			return lev(s, t, ZERO_LENGTH_INT_ARRAY);
	}
	public static int levlim(String s, String t, int limit)
	{
		if (s != null && t != null)
			return levlim(s, t, limit, getWorkspace(s.length(), t.length()));
		else
			return levlim(s, t, limit, ZERO_LENGTH_INT_ARRAY);
	}
	public static int damlev(String s, String t)
	{
		if (s != null && t != null)
			return damlev(s, t, getWorkspace(s.length(), t.length()));
		else
			return damlev(s, t, ZERO_LENGTH_INT_ARRAY);
	}
	public static int damlevlim(String s, String t, int limit)
	{
		if (s != null && t != null)
			return damlevlim(s, t, limit, getWorkspace(s.length(), t.length()));
		else
			return damlevlim(s, t, limit, ZERO_LENGTH_INT_ARRAY);
	}

	public static void main(String[] args) throws Exception
	{
		System.out.println("Check old bugs:\n");
		System.out.println("Tomas' report: damlevlim('h', 'hello', 2) = 4 (should be 2)");
		System.out.print("damlevlim('h', 'hello', 2) = " + damlevlim("h", "hello", 2));
		System.out.println(" " + ((damlevlim("h", "hello", 2) == 2)?"PASS":"*FAIL*"));
		System.out.print("damlevlim('hello', 'h', 2) = " + damlevlim("hello", "h", 2));
		System.out.println(" " + ((damlevlim("hello", "h", 2) == 2)?"PASS":"*FAIL*"));
		System.out.println("Adam Koprowski's report lev(\"1\", \"Blah blah blah blah blah blah\")) = 1 (should be ... large)");
		System.out.print("damlevlim(\"1\", \"Blah blah blah blah blah blah\", 30) = " + damlevlim("1", "Blah blah blah blah blah blah", 30));
		System.out.println(" " + ((damlevlim("1", "Blah blah blah blah blah blah", 30) == 29)?"PASS":"*FAIL*"));
		System.out.print("damlevlim(\"1\", \"Blah blah blah blah blah blah\", 10) = " + damlevlim("1", "Blah blah blah blah blah blah", 10));
		System.out.println(" " + ((damlevlim("1", "Blah blah blah blah blah blah", 10) == 10)?"PASS":"*FAIL*"));
		System.out.print("damlevlim(\"shortisbetter\", \"short\", 10) = " + damlevlim("shortisbetter", "short", 10));
		System.out.println(" " + ((damlevlim("shortisbetter", "short", 10) == 8)?"PASS":"*FAIL*"));
		System.out.print("damlevlim(\"shortisbetter\", \"short\", 2) = " + damlevlim("shortisbetter", "short", 2));
		System.out.println(" " + ((damlevlim("shortisbetter", "short", 2) == 2)?"PASS":"*FAIL*"));
		System.out.print("damlevlim(\"short\", \"shortisbetter\", 10) = " + damlevlim("short", "shortisbetter", 10));
		System.out.println(" " + ((damlevlim("short", "shortisbetter", 10) == 8)?"PASS":"*FAIL*"));
		System.out.print("damlevlim(\"short\", \"shortisbetter\", 2) = " + damlevlim("short", "shortisbetter", 2));
		System.out.println(" " + ((damlevlim("short", "shortisbetter", 2) == 2)?"PASS":"*FAIL*"));
		System.out.print("damlevlim(\"short\", \"shorts\", 2) = " + damlevlim("short", "shorts", 2));
		System.out.println(" " + ((damlevlim("short", "shorts", 2) == 1)?"PASS":"*FAIL*"));
		System.out.print("damlevlim(\"sohrt\", \"short\", 3) = " + damlevlim("sohrt", "short", 3));
		System.out.println(" " + ((damlevlim("sohrt", "short", 3) == 1)?"PASS":"*FAIL*"));
		System.out.print("levlim(\"sohrt\", \"short\", 3) = " + levlim("sohrt", "short", 3));
		System.out.println(" " + ((levlim("sohrt", "short", 3) == 2)?"PASS":"*FAIL*"));
		System.out.print("levlim(\"sohrts\", \"short\", 2) = " + levlim("sohrts", "short", 3));
		System.out.println(" " + ((levlim("sohrts", "short", 2) == 2)?"PASS":"*FAIL*"));
		System.out.print("damlevlim(\"short\", \"\", 2) = " + damlevlim("short", "", 2));
		System.out.println(" " + ((damlevlim("short", "", 2) == 2)?"PASS":"*FAIL*"));
		System.out.print("damlev(\"short\", \"shrt\") = " + damlev("short", "shrt"));
		System.out.println(" " + ((damlev("short", "shrt") == 1)?"PASS":"*FAIL*"));
		System.out.println("November 6 2009 damlevlim failing with zero-length arguments");
		System.out.print("damlevlim(\"\", \"\", 3) = " + damlevlim("", "", 3));
		System.out.println(" " + ((damlevlim("", "", 3) == 0)?"PASS":"*FAIL*"));
		System.out.print("damlevlim(\"other one is zero-length\", \"\", 3) = " + damlevlim("other one is null", "", 3));
		System.out.println(" " + ((damlevlim("other one is zero-length", "", 3) == 3)?"PASS":"*FAIL*"));
		System.out.print("damlevlim(\"\", \"other one is zero-length\", 3) = " + damlevlim("", "other one is zero-length", 3));
		System.out.println(" " + ((damlevlim("", "other one is zero-length", 3) == 3)?"PASS":"*FAIL*"));
		System.out.println("November 27 2009 transposition weirdness?");
		System.out.print("damlev(\"banana\", \"baanaa\") = " + damlev("banana", "baanaa"));
		System.out.println(" " + ((damlev("banana", "baanaa") == 2)?"PASS":"*FAIL*"));
		System.out.println("November 27 2009 transposition at either end check");
		System.out.print("damlev(\"banana\", \"banaan\") = " + damlev("banana", "banaan"));
		System.out.println(" " + ((damlev("banana", "banaan") == 1)?"PASS":"*FAIL*"));
		System.out.print("damlev(\"banana\", \"abnana\") = " + damlev("banana", "abnana"));
		System.out.println(" " + ((damlev("banana", "abnana") == 1)?"PASS":"*FAIL*"));
		System.out.println("November 27 2009 transposition at either end check");
		System.out.print("damlev(\"banana\", \"banaan\") = " + damlev("banana", "banaan"));
		System.out.println(" " + ((damlev("banana", "banaan") == 1)?"PASS":"*FAIL*"));
		System.out.println("\n-----------------------Done--------------------------\n");
		System.out.println("Performance timings: expect tests to take about 7 seconds on a 2GHz Intel Core2");
		long TEST_CYCLES = 10000;
		String STRING1 = "0876786s896d8cvs9dvdfvdf87v09df8v7d0fv7df0v87dsf0v87dfv";
		String STRING2 = "7869vc87b 9cv8b7cvb,2jk3,2gj5k43,25gj4k3,2g5j4k3,2g5jk423,";
		int total;
		for (int j = 0; j < 4; j++)
		{
			total = 0;
			long lTimer = System.nanoTime();
			for (int i = 0; i < TEST_CYCLES; i++)
				total += damlev(STRING1, STRING2);
			System.out.println(TEST_CYCLES + " tests (average result: " + ((float)total / (float)TEST_CYCLES) + ") with array allocated in function average ns: " + ((System.nanoTime() - lTimer) / TEST_CYCLES));

			total = 0;
			lTimer = System.nanoTime();
			for (int i = 0; i < TEST_CYCLES; i++)
				total += damlevlim(STRING1, STRING2, 3);
			System.out.println(TEST_CYCLES + " tests of damlevlim(,,3) (average result: " + ((float)total / (float)TEST_CYCLES) + ") with array allocated in function average ns: " + ((System.nanoTime() - lTimer) / TEST_CYCLES));

			total = 0;
			lTimer = System.nanoTime();
			int[] aiWS = getWorkspace(STRING1.length(), STRING2.length());
			for (int i = 0; i < TEST_CYCLES; i++)
				total += damlev(STRING1, STRING2, aiWS);
			System.out.println(TEST_CYCLES + " tests (average result: " + ((float)total / (float)TEST_CYCLES) + ") with array preallocated average ns: " + ((System.nanoTime() - lTimer) / TEST_CYCLES));

			total = 0;
			lTimer = System.nanoTime();
			aiWS = getWorkspace(STRING1.length(), STRING2.length());
			for (int i = 0; i < TEST_CYCLES; i++)
				total += damlevlim(STRING1, STRING2, 3, aiWS);
			System.out.println(TEST_CYCLES + " tests of damlevlim(,,3) (average result: " + ((float)total / (float)TEST_CYCLES) + ") with array preallocated average ns: " + ((System.nanoTime() - lTimer) / TEST_CYCLES));
		}
		java.io.BufferedReader brIn = new java.io.BufferedReader(new java.io.InputStreamReader(System.in));
		String sIn = null;
		while (true)
		{
			System.out.println("Two words, separated by space: ");
			sIn = brIn.readLine();
			if (sIn == null)
				break;
			String[] asIn = sIn.split(" ");
			if (asIn.length != 2)
				continue;
			System.out.println("lev(" + asIn[0] + ", " + asIn[1] + ") = " + lev(asIn[0], asIn[1]));
			System.out.println("levlim(" + asIn[0] + ", " + asIn[1] + ", 3) = " + levlim(asIn[0], asIn[1], 3));
			System.out.println("damlev(" + asIn[0] + ", " + asIn[1] + ") = " + damlev(asIn[0], asIn[1]));
			System.out.println("damlevlim(" + asIn[0] + ", " + asIn[1] + ", 3) = " + damlevlim(asIn[0], asIn[1], 3));
		}

	}
}
