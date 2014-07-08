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
 * Map Utility to provide some basic but frequently used operations 
 * @author Xing
 */
public class MapUtil {
    
    /**
     * Sort the Map entities according to the values in descendant order.
     * Refer to http://stackoverflow.com/questions/2864840/treemap-sort-by-value
     * It is not able to sort HashMap
     * @param map        Map to be sorted
     * @param <K>       Key of map entry
     * @param <V>       Value of map entry, could be integer or float
     * @return           Sorted map
    */
    public static <K, V extends Comparable<? super V>> Map<K, V> 
        sortByValue( Map<K, V> map )
    {
        List<Map.Entry<K, V>> list =
            new LinkedList<>( map.entrySet() );
        
        Collections.sort( list, new Comparator<Map.Entry<K, V>>()
        {
            @Override
            public int compare( Map.Entry<K, V> o1, Map.Entry<K, V> o2 )
            {
                return (o2.getValue()).compareTo( o1.getValue() );
            }
        } );

        Map<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : list)
        {
            result.put( entry.getKey(), entry.getValue() );
        }
        return result;
    }
        
    /**
     * Update +1 to the count of word in the HashMap.
     * @param map        HashMap to be updated
     * @param word       String type word
    */
    public static void updateHashMap(HashMap<String, Integer> map, 
            String word) throws NullPointerException{
        if (map.containsKey(word))
            map.put(word, map.get(word)+1);   // add one
        else
            map.put(word, 1);  // init as one
    }
    
    /**
     * Update +n to the count of word in the HashMap.
     * @param map        HashMap to be updated
     * @param word       String type word
     * @param n          Count to be added
    */
    public static void updateHashMap(HashMap<String, Integer> map, 
            String word, Integer n) throws NullPointerException{
        if (map.containsKey(word))
            map.put(word, map.get(word)+n);   // add n
        else
            map.put(word, n);  // init as n
    }
    
    /**
     * Calculate the sum of values in the HashMap
     * @param map        HashMap to be calculated
     * @return           Sum of values of the HashMap
    */
    public static int sumHashMap(HashMap<String, Integer> map) 
            throws NullPointerException{
        int sum = 0;
        for (Integer value : map.values())
            sum += value;
        
        return sum;
    }
}
