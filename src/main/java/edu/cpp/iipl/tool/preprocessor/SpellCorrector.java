package edu.cpp.iipl.tool.preprocessor;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * http://blog.faroo.com/2012/06/07/improved-edit-distance-based-spelling-correction/
 *
 * From Java version: https://github.com/gpranav88/symspell
 *
 * Created by xing on 4/22/16.
 */

// TODO: need to rewrite this class in the future because the code looks like shit
public class SpellCorrector {

    private static int editDistanceMax=2;
    private static int verbose = 0;
    //0: top suggestion
    //1: all suggestions of smallest edit distance
    //2: all suggestions <= editDistanceMax (slower, no early termination)

    private static class dictionaryItem
    {
        public List<Integer> suggestions = new ArrayList<>();
        public int count = 0;
    }

    private static class suggestItem
    {
        public String term = "";
        public int distance = 0;
        public int count = 0;

        @Override
        public boolean equals(Object obj)
        {
            return term.equals(((suggestItem)obj).term);
        }

        @Override
        public int hashCode()
        {
            return term.hashCode();
        }
    }

    //Dictionary that contains both the original words and the deletes derived from them. A term might be both word and delete from another word at the same time.
    //For space reduction a item might be either of type dictionaryItem or Int.
    //A dictionaryItem is used for word, word/delete, and delete with multiple suggestions. Int is used for deletes with a single suggestion (the majority of entries).
    private static HashMap<String, Object> dictionary = new HashMap<String, Object>(); //initialisierung

    //List of unique words. By using the suggestions (Int) as index for this list they are translated into the original String.
    private static List<String> wordlist = new ArrayList<String>();

    //create a non-unique wordlist from sample text
    //language independent (e.g. works with Chinese characters)
    private static Iterable<String> parseWords(String text)
    {
        // \w Alphanumeric characters (including non-latin characters, umlaut characters and digits) plus "_"
        // \d Digits
        // Provides identical results to Norvigs regex "[a-z]+" for latin characters, while additionally providing compatibility with non-latin characters
        List<String> allMatches = new ArrayList<String>();
        Matcher m = Pattern.compile("[\\w-[\\d_]]+").matcher(text.toLowerCase());
        while (m.find()) {
            allMatches.add(m.group());
        }
        return allMatches;
    }

    public static int maxlength = 0;//maximum dictionary term length

    //for every word there all deletes with an edit distance of 1..editDistanceMax created and added to the dictionary
    //every delete entry has a suggestions list, which points to the original term(s) it was created from
    //The dictionary may be dynamically updated (word frequency and new words) at any time by calling createDictionaryEntry
    private static boolean CreateDictionaryEntry(String key, String language)
    {
        boolean result = false;
        dictionaryItem value=null;
        Object valueo;
        valueo = dictionary.get(language+key);
        if (valueo!=null)
        {
            //int or dictionaryItem? delete existed before word!
            if (valueo instanceof Integer) {
                int tmp = (int)valueo;
                value = new dictionaryItem();
                value.suggestions.add(tmp);
                dictionary.put(language + key,value);
            }

            //already exists:
            //1. word appears several times
            //2. word1==deletes(word2)
            else
            {
                value = (dictionaryItem)valueo;
            }

            //prevent overflow
            if (value.count < Integer.MAX_VALUE) value.count++;
        }
        else if (wordlist.size() < Integer.MAX_VALUE)
        {
            value = new dictionaryItem();
            value.count++;
            dictionary.put(language + key, value);

            if (key.length() > maxlength) maxlength = key.length();
        }

        //edits/suggestions are created only once, no matter how often word occurs
        //edits/suggestions are created only as soon as the word occurs in the corpus,
        //even if the same term existed before in the dictionary as an edit from another word
        //a treshold might be specifid, when a term occurs so frequently in the corpus that it is considered a valid word for spelling correction
        if(value.count == 1)
        {
            //word2index
            wordlist.add(key);
            int keyint = (int)(wordlist.size() - 1);

            result = true;

            //create deletes
            for (String delete : Edits(key, 0, new HashSet<String>()))
            {
                Object value2;
                value2 = dictionary.get(language+delete);
                if (value2!=null)
                {
                    //already exists:
                    //1. word1==deletes(word2)
                    //2. deletes(word1)==deletes(word2)
                    //int or dictionaryItem? single delete existed before!
                    if (value2 instanceof Integer)
                    {
                        //transformes int to dictionaryItem
                        int tmp = (int)value2;
                        dictionaryItem di = new dictionaryItem();
                        di.suggestions.add(tmp);
                        dictionary.put(language + delete,di);
                        if (!di.suggestions.contains(keyint)) AddLowestDistance(di, key, keyint, delete);
                    }
                    else if (!((dictionaryItem)value2).suggestions.contains(keyint)) AddLowestDistance((dictionaryItem) value2, key, keyint, delete);
                }
                else
                {
                    dictionary.put(language + delete, keyint);
                }

            }
        }
        return result;
    }

    //create a frequency dictionary from a corpus
    public static void CreateDictionary(String corpus, String language)
    {
        File f = new File(corpus);
        if(!(f.exists() && !f.isDirectory()))
        {
            System.out.println("File not found: " + corpus);
            return;
        }

        System.out.println("Creating dictionary ...");
        long startTime = System.currentTimeMillis();
        long wordCount = 0;

        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(corpus));
            String line;
            while ((line = br.readLine()) != null)
            {
                for (String key : parseWords(line))
                {
                    if (CreateDictionaryEntry(key, language)) wordCount++;
                }
            }
        }
        catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        //wordlist.TrimExcess();
        long endTime = System.currentTimeMillis();
        System.out.println("\rDictionary: " + wordCount + " words, " + dictionary.size() + " entries, edit distance=" + editDistanceMax + " in " + (endTime-startTime)+"ms ");
    }

    //save some time and space
    private static void AddLowestDistance(dictionaryItem item, String suggestion, int suggestionint, String delete)
    {
        //remove all existing suggestions of higher distance, if verbose<2
        //index2word
        //TODO check
        if ((verbose < 2) && (item.suggestions.size() > 0) && (wordlist.get(item.suggestions.get(0)).length()-delete.length() > suggestion.length() - delete.length())) item.suggestions.clear();
        //do not add suggestion of higher distance than existing, if verbose<2
        if ((verbose == 2) || (item.suggestions.size() == 0) || (wordlist.get(item.suggestions.get(0)).length()-delete.length() >= suggestion.length() - delete.length())) item.suggestions.add(suggestionint);
    }

    //inexpensive and language independent: only deletes, no transposes + replaces + inserts
    //replaces and inserts are expensive and language dependent (Chinese has 70,000 Unicode Han characters)
    private static HashSet<String> Edits(String word, int editDistance, HashSet<String> deletes)
    {
        editDistance++;
        if (word.length() > 1)
        {
            for (int i = 0; i < word.length(); i++)
            {
                //delete ith character
                String delete =  word.substring(0,i)+word.substring(i+1);
                if (deletes.add(delete))
                {
                    //recursion, if maximum edit distance not yet reached
                    if (editDistance < editDistanceMax) Edits(delete, editDistance, deletes);
                }
            }
        }
        return deletes;
    }

    private static List<suggestItem> Lookup(String input, String language, int editDistanceMax)
    {
        //save some time
        if (input.length() - editDistanceMax > maxlength)
            return new ArrayList<suggestItem>();

        List<String> candidates = new ArrayList<String>();
        HashSet<String> hashset1 = new HashSet<String>();

        List<suggestItem> suggestions = new ArrayList<suggestItem>();
        HashSet<String> hashset2 = new HashSet<String>();

        Object valueo;

        //add original term
        candidates.add(input);

        while (candidates.size()>0)
        {
            String candidate = candidates.get(0);
            candidates.remove(0);

            //save some time
            //early termination
            //suggestion distance=candidate.distance... candidate.distance+editDistanceMax
            //if canddate distance is already higher than suggestion distance, than there are no better suggestions to be expected

            //label for c# goto replacement
            nosort:{

                if ((verbose < 2) && (suggestions.size() > 0) && (input.length()-candidate.length() > suggestions.get(0).distance))
                    break nosort;

                //read candidate entry from dictionary
                valueo = dictionary.get(language + candidate);
                if (valueo != null)
                {
                    dictionaryItem value= new dictionaryItem();
                    if (valueo instanceof Integer)
                        value.suggestions.add((int)valueo);
                    else value = (dictionaryItem)valueo;

                    //if count>0 then candidate entry is correct dictionary term, not only delete item
                    if ((value.count > 0) && hashset2.add(candidate))
                    {
                        //add correct dictionary term term to suggestion list
                        suggestItem si = new suggestItem();
                        si.term = candidate;
                        si.count = value.count;
                        si.distance = input.length() - candidate.length();
                        suggestions.add(si);
                        //early termination
                        if ((verbose < 2) && (input.length() - candidate.length() == 0))
                            break nosort;
                    }

                    //iterate through suggestions (to other correct dictionary items) of delete item and add them to suggestion list
                    Object value2;
                    for (int suggestionint : value.suggestions)
                    {
                        //save some time
                        //skipping double items early: different deletes of the input term can lead to the same suggestion
                        //index2word
                        //TODO
                        String suggestion = wordlist.get(suggestionint);
                        if (hashset2.add(suggestion))
                        {
                            //True Damerau-Levenshtein Edit Distance: adjust distance, if both distances>0
                            //We allow simultaneous edits (deletes) of editDistanceMax on on both the dictionary and the input term.
                            //For replaces and adjacent transposes the resulting edit distance stays <= editDistanceMax.
                            //For inserts and deletes the resulting edit distance might exceed editDistanceMax.
                            //To prevent suggestions of a higher edit distance, we need to calculate the resulting edit distance, if there are simultaneous edits on both sides.
                            //Example: (bank==bnak and bank==bink, but bank!=kanb and bank!=xban and bank!=baxn for editDistanceMaxe=1)
                            //Two deletes on each side of a pair makes them all equal, but the first two pairs have edit distance=1, the others edit distance=2.
                            int distance = 0;
                            if (suggestion != input)
                            {
                                if (suggestion.length() == candidate.length()) distance = input.length() - candidate.length();
                                else if (input.length() == candidate.length()) distance = suggestion.length() - candidate.length();
                                else
                                {
                                    //common prefixes and suffixes are ignored, because this speeds up the Damerau-levenshtein-Distance calculation without changing it.
                                    int ii = 0;
                                    int jj = 0;
                                    while ((ii < suggestion.length()) && (ii < input.length()) && (suggestion.charAt(ii) == input.charAt(ii))) ii++;
                                    while ((jj < suggestion.length() - ii) && (jj < input.length() - ii) && (suggestion.charAt(suggestion.length() - jj - 1) == input.charAt(input.length() - jj - 1))) jj++;
                                    if ((ii > 0) || (jj > 0)) {
                                        distance = DamerauLevenshteinDistance(suggestion.substring(ii, suggestion.length() - jj), input.substring(ii, input.length() - jj));
                                    }
                                    else distance = DamerauLevenshteinDistance(suggestion, input);
                                }
                            }

                            //save some time.
                            //remove all existing suggestions of higher distance, if verbose<2
                            if ((verbose < 2) && (suggestions.size() > 0) && (suggestions.get(0).distance > distance)) suggestions.clear();
                            //do not process higher distances than those already found, if verbose<2
                            if ((verbose < 2) && (suggestions.size() > 0) && (distance > suggestions.get(0).distance)) continue;

                            if (distance <= editDistanceMax)
                            {
                                value2 = dictionary.get(language + suggestion);
                                if (value2!=null)
                                {
                                    suggestItem si = new suggestItem();
                                    si.term = suggestion;
                                    si.count = ((dictionaryItem)value2).count;
                                    si.distance = distance;
                                    suggestions.add(si);
                                }
                            }
                        }
                    }//end foreach
                }//end if

                //add edits
                //derive edits (deletes) from candidate (input) and add them to candidates list
                //this is a recursive process until the maximum edit distance has been reached
                if (input.length() - candidate.length() < editDistanceMax)
                {
                    //save some time
                    //do not create edits with edit distance smaller than suggestions already found
                    if ((verbose < 2) && (suggestions.size() > 0) && (input.length() - candidate.length() >= suggestions.get(0).distance)) continue;

                    for (int i = 0; i < candidate.length(); i++)
                    {
                        String delete = candidate.substring(0, i)+candidate.substring(i+1);
                        if (hashset1.add(delete)) candidates.add(delete);
                    }
                }
            } //end lable nosort
        } //end while

        //sort by ascending edit distance, then by descending word frequency
        if (verbose < 2)
            //suggestions.Sort((x, y) => -x.count.CompareTo(y.count));
            Collections.sort(suggestions, new Comparator<suggestItem>()
            {
                public int compare(suggestItem f1, suggestItem f2)
                {
                    return -(f1.count-f2.count);
                }
            });
        else
            //suggestions.Sort((x, y) => 2*x.distance.CompareTo(y.distance) - x.count.CompareTo(y.count));
            Collections.sort(suggestions, new Comparator<suggestItem>()
            {
                public int compare(suggestItem x, suggestItem y)
                {
                    return ((2*x.distance-y.distance)>0?1:0) - ((x.count - y.count)>0?1:0);
                }
            });
        if ((verbose == 0)&&(suggestions.size()>1))
            return suggestions.subList(0, 1);
        else return suggestions;
    }

    private static void Correct(String input, String language)
    {
        List<suggestItem> suggestions = null;

        /*
        //Benchmark: 1000 x Lookup
        Stopwatch stopWatch = new Stopwatch();
        stopWatch.Start();
        for (int i = 0; i < 1000; i++)
        {
            suggestions = Lookup(input,language,editDistanceMax);
        }
        stopWatch.Stop();
        Console.WriteLine(stopWatch.ElapsedMilliseconds.ToString());
        */


        //check in dictionary for existence and frequency; sort by ascending edit distance, then by descending word frequency
        suggestions = Lookup(input, language, editDistanceMax);

        //display term and frequency
        for (suggestItem suggestion: suggestions)
        {
            System.out.println( suggestion.term + " " + suggestion.distance + " " + suggestion.count);
        }
        if (verbose !=0) System.out.println(suggestions.size() + " suggestions");
        System.out.println("done");
    }

    private static void ReadFromStdIn()
    {
        String word;
        BufferedReader br =  new BufferedReader(new InputStreamReader(System.in));
        try {
            while ((word = br.readLine())!=null)
            {
                Correct(word,"");
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void main(String[] args)
    {

        //Create the dictionary from a sample corpus
        //e.g. http://norvig.com/big.txt , or any other large text corpus
        //The dictionary may contain vocabulary from different languages.
        //If you use mixed vocabulary use the language parameter in Correct() and CreateDictionary() accordingly.
        //You may use CreateDictionaryEntry() to update a (self learning) dictionary incrementally
        //To extend spelling correction beyond single words to phrases (e.g. correcting "unitedkingom" to "united kingdom") simply add those phrases with CreateDictionaryEntry().
        CreateDictionary("big.txt","");
        ReadFromStdIn();
    }

    // Damerau–Levenshtein distance algorithm and code
    // from http://en.wikipedia.org/wiki/Damerau%E2%80%93Levenshtein_distance (as retrieved in June 2012)
    public static int DamerauLevenshteinDistance(String a, String b) {
        final int inf = a.length() + b.length() + 1;
        int[][] H = new int[a.length() + 2][b.length() + 2];
        for (int i = 0; i <= a.length(); i++) {
            H[i + 1][1] = i;
            H[i + 1][0] = inf;
        }
        for (int j = 0; j <= b.length(); j++) {
            H[1][j + 1] = j;
            H[0][j + 1] = inf;
        }
        HashMap<Character, Integer> DA = new HashMap<Character, Integer>();
        for (int d = 0; d < a.length(); d++)
            if (!DA.containsKey(a.charAt(d)))
                DA.put(a.charAt(d), 0);


        for (int d = 0; d < b.length(); d++)
            if (!DA.containsKey(b.charAt(d)))
                DA.put(b.charAt(d), 0);

        for (int i = 1; i <= a.length(); i++) {
            int DB = 0;
            for (int j = 1; j <= b.length(); j++) {
                final int i1 = DA.get(b.charAt(j - 1));
                final int j1 = DB;
                int d = 1;
                if (a.charAt(i - 1) == b.charAt(j - 1)) {
                    d = 0;
                    DB = j;
                }
                H[i + 1][j + 1] = min(
                        H[i][j] + d,
                        H[i + 1][j] + 1,
                        H[i][j + 1] + 1,
                        H[i1][j1] + ((i - i1 - 1))
                                + 1 + ((j - j1 - 1)));
            }
            DA.put(a.charAt(i - 1), i);
        }
        return H[a.length() + 1][b.length() + 1];
    }
    public static int min(int a, int b, int c, int d) {
        return Math.min(a, Math.min(b, Math.min(c, d)));
    }


    public void init() {
        CreateDictionary("big.txt","");
    }


    public String correct(String word) {
        List<suggestItem> suggestions = Lookup(word, "", editDistanceMax);

        if (suggestions != null && suggestions.size() > 0)
            return suggestions.get(0).term;
        else
            return "errorword";
    }

    public List<String> correct(List<String> words) {
        List<String> corrected = new ArrayList<>();

        for (String word : words)
            corrected.add(correct(word));

        return corrected;
    }

    public SpellCorrector() {
        init();
    }

}


