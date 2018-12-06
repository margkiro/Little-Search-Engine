package search;

import java.io.*;
import java.util.*;

/**
 * This class encapsulates an occurrence of a keyword in a document. It stores the
 * document name, and the frequency of occurrence in that document. Occurrences are
 * associated with keywords in an index hash table.
 * 
 * @author Sesh Venugopal
 * 
 */
class Occurrence {
	/**
	 * Document in which a keyword occurs.
	 */
	String document;
	
	/**
	 * The frequency (number of times) the keyword occurs in the above document.
	 */
	int frequency;
	
	/**
	 * Initializes this occurrence with the given document,frequency pair.
	 * 
	 * @param doc Document name
	 * @param freq Frequency
	 */
	public Occurrence(String doc, int freq) {
		document = doc;
		frequency = freq;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "(" + document + "," + frequency + ")";
	}
}

/**
 * This class builds an index of keywords. Each keyword maps to a set of documents in
 * which it occurs, with frequency of occurrence in each document. Once the index is built,
 * the documents can searched on for keywords.
 *
 */
public class LittleSearchEngine {
	
	/**
	 * This is a hash table of all keywords. The key is the actual keyword, and the associated value is
	 * an array list of all occurrences of the keyword in documents. The array list is maintained in descending
	 * order of occurrence frequencies.
	 */
	HashMap<String,ArrayList<Occurrence>> keywordsIndex;
	
	/**
	 * The hash table of all noise words - mapping is from word to itself.
	 */
	HashMap<String,String> noiseWords;
	
	/**
	 * Creates the keyWordsIndex and noiseWords hash tables.
	 */
	public LittleSearchEngine() {
		keywordsIndex = new HashMap<String,ArrayList<Occurrence>>(1000,2.0f);
		noiseWords = new HashMap<String,String>(100,2.0f);
	}
	
	/**
	 * This method indexes all keywords found in all the input documents. When this
	 * method is done, the keywordsIndex hash table will be filled with all keywords,
	 * each of which is associated with an array list of Occurrence objects, arranged
	 * in decreasing frequencies of occurrence.
	 * 
	 * @param docsFile Name of file that has a list of all the document file names, one name per line
	 * @param noiseWordsFile Name of file that has a list of noise words, one noise word per line
	 * @throws FileNotFoundException If there is a problem locating any of the input files on disk
	 */
	public void makeIndex(String docsFile, String noiseWordsFile) 
	throws FileNotFoundException {
		// load noise words to hash table
		Scanner sc = new Scanner(new File(noiseWordsFile));
		while (sc.hasNext()) {
			String word = sc.next();
			noiseWords.put(word,word);
		}
		
		// index all keywords
		sc = new Scanner(new File(docsFile));
		while (sc.hasNext()) {
			String docFile = sc.next();
			HashMap<String,Occurrence> kws = loadKeyWords(docFile);
			mergeKeyWords(kws);
		}
		
	}

	/**
	 * Scans a document, and loads all keywords found into a hash table of keyword occurrences
	 * in the document. Uses the getKeyWord method to separate keywords from other words.
	 * 
	 * @param docFile Name of the document file to be scanned and loaded
	 * @return Hash table of keywords in the given document, each associated with an Occurrence object
	 * @throws FileNotFoundException If the document file is not found on disk
	 */
	public HashMap<String,Occurrence> loadKeyWords(String docFile) 
	throws FileNotFoundException {
		// COMPLETE THIS METHOD
		// THE FOLLOWING LINE HAS BEEN ADDED TO MAKE THE METHOD COMPILE
		if(docFile == null){
			throw new FileNotFoundException();
		}
		
		HashMap<String, Occurrence> keys = new HashMap<String, Occurrence>();
		Scanner sc = new Scanner(new File(docFile));
		
		while(sc.hasNext()){
			String word = getKeyWord(sc.next());
			if(word!= null){
				if(keys.containsKey(word)){
					Occurrence occ = keys.get(word);
					occ.frequency++;
				}else{
					Occurrence occ = new Occurrence(docFile,1);
					keys.put(word, occ);
				}
			}
		}
		return keys;
	}
	
	/**
	 * Merges the keywords for a single document into the master keywordsIndex
	 * hash table. For each keyword, its Occurrence in the current document
	 * must be inserted in the correct place (according to descending order of
	 * frequency) in the same keyword's Occurrence list in the master hash table. 
	 * This is done by calling the insertLastOccurrence method.
	 * 
	 * @param kws Keywords hash table for a document
	 */
	public void mergeKeyWords(HashMap<String,Occurrence> kws) {
		// COMPLETE THIS METHOD
		ArrayList<Occurrence> occ = new ArrayList<Occurrence>();
		for(String keys: kws.keySet()){
			Occurrence n = kws.get(keys);
			if(!keywordsIndex.containsKey(keys)){
				ArrayList<Occurrence> occ2 = new ArrayList<Occurrence>();
				occ2.add(n);
				keywordsIndex.put(keys, occ2);
			}else{
				occ = keywordsIndex.get(keys);
				occ.add(n);
				insertLastOccurrence(occ);
				keywordsIndex.put(keys, occ);
			}
		}
	}
	
	/**
	 * Given a word, returns it as a keyword if it passes the keyword test,
	 * otherwise returns null. A keyword is any word that, after being stripped of any
	 * TRAILING punctuation, consists only of alphabetic letters, and is not
	 * a noise word. All words are treated in a case-INsensitive manner.
	 * 
	 * Punctuation characters are the following: '.', ',', '?', ':', ';' and '!'
	 * 
	 * @param word Candidate word
	 * @return Keyword (word without trailing punctuation, LOWER CASE)
	 */
	public String getKeyWord(String word) {
		// COMPLETE THIS METHOD
		// THE FOLLOWING LINE HAS BEEN ADDED TO MAKE THE METHOD COMPILE
		word = word.trim();
		char c = word.charAt(word.length()-1);
		while((c=='.') || (c==',') || (c=='?' )|| (c==':') || (c==';') ||(c== '!')){
			word = word.substring(0, word.length()-1);
			if(word.length()>1){
				c = word.charAt(word.length()-1);
			}else{
				break;
			}
		}
		
		String lc = word.toLowerCase();
		if(noiseWords.containsKey(lc)){
			return null;
		}
		for(int i = 0; i<lc.length(); i++){
			if(!Character.isLetter(lc.charAt(i)))
				return null;
		}
		return lc;
	}
	
	/**
	 * Inserts the last occurrence in the parameter list in the correct position in the
	 * same list, based on ordering occurrences on descending frequencies. The elements
	 * 0..n-2 in the list are already in the correct order. Insertion of the last element
	 * (the one at index n-1) is done by first finding the correct spot using binary search, 
	 * then inserting at that spot.
	 * 
	 * @param occs List of Occurrences
	 * @return Sequence of mid point indexes in the input list checked by the binary search process,
	 *         null if the size of the input list is 1. This returned array list is only used to test
	 *         your code - it is not used elsewhere in the program.
	 */
	public ArrayList<Integer> insertLastOccurrence(ArrayList<Occurrence> occs) {
		// COMPLETE THIS METHOD
		// THE FOLLOWING LINE HAS BEEN ADDED TO MAKE THE METHOD COMPILE
		ArrayList<Integer> mid = new ArrayList<Integer>();
		int begin = 0;
		int end = occs.size()-1;
		int middle;
		int freq = occs.get(end).frequency;
		
		if(occs.size()==1)
		return null;
		
		while(end>=begin){
			middle = (begin+end)/2;
			mid.add(middle);
			int freq2 = occs.get(middle).frequency;
			
			if(freq2 == freq){
				break;
			}else if(freq2<freq){
				end = middle - 1;
			}else if(freq2>freq){
				begin = middle +1;
			}
		}
		Occurrence tmp = occs.remove(occs.size()-1);
		occs.add(mid.get(mid.size()-1),tmp);
		return mid;
	}
	
	/**
	 * Search result for "kw1 or kw2". A document is in the result set if kw1 or kw2 occurs in that
	 * document. Result set is arranged in descending order of occurrence frequencies. (Note that a
	 * matching document will only appear once in the result.) Ties in frequency values are broken
	 * in favor of the first keyword. (That is, if kw1 is in doc1 with frequency f1, and kw2 is in doc2
	 * also with the same frequency f1, then doc1 will appear before doc2 in the result. 
	 * The result set is limited to 5 entries. If there are no matching documents, the result is null.
	 * 
	 * @param kw1 First keyword
	 * @param kw1 Second keyword
	 * @return List of NAMES of documents in which either kw1 or kw2 occurs, arranged in descending order of
	 *         frequencies. The result size is limited to 5 documents. If there are no matching documents,
	 *         the result is null.
	 */
	public ArrayList<String> top5search(String kw1, String kw2) {
		// COMPLETE THIS METHOD
		// THE FOLLOWING LINE HAS BEEN ADDED TO MAKE THE METHOD COMPILE
		kw1 = kw1.toLowerCase();
		kw2 = kw2.toLowerCase();
		
		ArrayList<String> top5 = new ArrayList<String>();
		ArrayList<Occurrence> one = new ArrayList<Occurrence>();
		ArrayList<Occurrence> two = new ArrayList<Occurrence>();
		
		if(keywordsIndex.get(kw1) != null)
			one = keywordsIndex.get(kw1);
		
		if(keywordsIndex.get(kw2) != null)
			two = keywordsIndex.get(kw2);
		
		int tmp1 = 0;
		//int tmp2 = 0;
		//int tmp3 = 0;
		
		while(tmp1<one.size()){
			
			if(top5.size()<5){
				
				int list = one.get(tmp1).frequency;
				String first = one.get(tmp1).document;
				
				for(int tmp2 = 0; tmp2<two.size(); tmp2++){
					int list2 = two.get(tmp2).frequency;
					String second = two.get(tmp2).document;
					
					if(list2<=list){
						if(!top5.contains(first) && top5.size()<=4)
							top5.add(second);
					}else if(list2>list){
						if(!top5.contains(second) && top5.size()<=4)
							top5.add(second);
					}
				
				}
			}
			tmp1++;
		}
		
		for(int tmp3 = 0; tmp3<top5.size(); tmp3++){
			System.out.println(top5.get(tmp3));
		}
		
		if(top5.size() ==0)
			return null;
		
		return top5;
	}
}
