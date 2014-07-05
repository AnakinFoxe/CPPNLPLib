/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.csupomona.nlp.util;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Xing
 */
public class MapUtil {
    
    /*
    * Refer to http://stackoverflow.com/questions/2864840/treemap-sort-by-value
    * Sort the Map entities according to the values in descendant order.
    * It is not able to sort HashMap
    * @param map Value could be integer or float
    * @return map Sorted map
    */
    public static <K, V extends Comparable<? super V>> Map<K, V> 
        sortByValue( Map<K, V> map )
    {
        List<Map.Entry<K, V>> list =
            new LinkedList<Map.Entry<K, V>>( map.entrySet() );
        
        Collections.sort( list, new Comparator<Map.Entry<K, V>>()
        {
            public int compare( Map.Entry<K, V> o1, Map.Entry<K, V> o2 )
            {
                return (o2.getValue()).compareTo( o1.getValue() );
            }
        } );

        Map<K, V> result = new LinkedHashMap<K, V>();
        for (Map.Entry<K, V> entry : list)
        {
            result.put( entry.getKey(), entry.getValue() );
        }
        return result;
    }
        
    /*
    * Update the count of N-gram in the HashMap.
    * @param map HashMap to be updated
    * @param ngram String type N-gram
    * @return Nothing
    */
    public static void updateHashMap(HashMap<String, Integer> map, String word) 
        throws NullPointerException{
        if (map.containsKey(word))
            map.put(word, map.get(word)+1);   // add one
        else
            map.put(word, 1);  // init as one
    }
}
