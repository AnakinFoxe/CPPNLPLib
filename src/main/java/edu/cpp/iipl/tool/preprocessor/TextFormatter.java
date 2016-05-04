/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.cpp.iipl.tool.preprocessor;

import java.util.Properties;

/**
 * Preprocess text to remove unnecessary characters.
 * Note: only suitable for English
 * @author Xing
 */
public class TextFormatter {

    private boolean REMOVE_HTML_TAG         = false;
    private boolean REMOVE_HTML_CODE        = false;
    private boolean REMOVE_URL              = false;
    private boolean REMOVE_BETWEEN_BRACKETS = false;
    private boolean REMOVE_IRREGULAR_SYMBOL = false;
    private boolean REMOVE_EXTRA_SPACE      = false;
    private boolean REMOVE_NON_ALPHANUMERIC = false;

    private boolean FORMAT_EMAIL            = false;
    private boolean FORMAT_PUNCTUATION      = false;
    private boolean FORMAT_UNIT             = false;

    /**
     * Default constructor.
     * Remove all non-alphanumeric symbols and extra spaces
     */
    public TextFormatter() {
        REMOVE_NON_ALPHANUMERIC = true;
    }

    public TextFormatter(Properties props) {
        if (props.containsKey("remove")) {
            String rmOptions = props.getProperty("remove");

            String[] options = rmOptions.trim().split(",");
            for (String option : options)
                switch (option.trim()) {
                    case "html_tag":
                        REMOVE_HTML_TAG = true;
                        break;
                    case "html_code":
                        REMOVE_HTML_CODE = true;
                        break;
                    case "url":
                        REMOVE_URL = true;
                        break;
                    case "between_brackets":
                        REMOVE_BETWEEN_BRACKETS = true;
                        break;
                    case "irregular_symbol":
                        REMOVE_IRREGULAR_SYMBOL = true;
                        break;
                    case "extra_space":
                        REMOVE_EXTRA_SPACE = true;
                        break;
                    default:
                        break;
                }
        }

        if (props.containsKey("format")) {
            String fmtOptions = props.getProperty("format");

            String[] options = fmtOptions.trim().split(",");
            for (String option : options)
                switch (option.trim()) {
                    case "email":
                        FORMAT_EMAIL = true;
                        break;
                    case "punctuation":
                        FORMAT_PUNCTUATION = true;
                        break;
                    case "unit":
                        FORMAT_UNIT = true;
                        break;
                    default:
                        break;
                }
        }
    }

    /**
     * Removers
     */
    private String removeHtmlTag(String line) {
        if (REMOVE_HTML_TAG)
            return line.replaceAll("<.+?>", " ");
        else
            return line;
    }

    private String removeHtmlCode(String line) {
        if (REMOVE_HTML_CODE)
            return line.replaceAll("&#[0-9]+", " ");
        else
            return line;
    }

    private String removeUrl(String line) {
        if (REMOVE_URL) {
            String parsed = line.replaceAll("http[s]?://[\\w\\d\\./]+", " ");
            parsed = parsed.replaceAll("([a-zA-Z0-9]+\\.)+[a-zA-Z]+", " ");
            return parsed;
        } else
            return line;
    }

    private String removeBetweenBrackets(String line) {
        if (REMOVE_BETWEEN_BRACKETS)
            return line.replaceAll("\\(.+?\\)", " ");
        else
            return line;
    }

    private String removeIrregularSymbols(String line) {
        if (REMOVE_IRREGULAR_SYMBOL)
            return line.replaceAll("[^a-zA-Z0-9,.?!%&']", " ");
        else
            return line;
    }

    private String removeExtraSpace(String line) {
        if (REMOVE_EXTRA_SPACE)
            return line.replaceAll("[ ]+", " ");
        else
            return line;
    }

    private String removeNonAlphanumeric(String line) {
        if (REMOVE_NON_ALPHANUMERIC) {
            // replacing everything except a-z A-Z 0-9 with space
            String parsed = line.replaceAll("[^a-zA-Z0-9]+", " ");

            // replacing redundant spaces
            parsed = parsed.replaceAll("[ ]+", " ");

            return parsed;
        } else
            return line;
    }


    /**
     * Formatters
     */
    private String formatEmail(String line) {
        if (FORMAT_EMAIL)
            return line.replaceAll("[a-zA-Z0-9\\.]+@[a-zA-Z0-9\\.]+[a-zA-Z]", "emailaddr");
        else
            return line;
    }
    
    private String formatPunctuation(String line) {
        if (FORMAT_PUNCTUATION) {
            String parsed = line.replaceAll("  ", " ");
            parsed = parsed.replaceAll(",", "");
            parsed = parsed.replaceAll("\\$", " ");
            parsed = parsed.replaceAll("\\?", " ");
            parsed = parsed.replaceAll("-", " ");
            parsed = parsed.replaceAll("//", "/");
            parsed = parsed.replaceAll("\\.\\.", ".");
            parsed = parsed.replaceAll(" / ", " ");
            parsed = parsed.replaceAll(" \\\\ ", " ");
            parsed = parsed.replaceAll("\\.", " . ");

            parsed = parsed.replaceAll("(^\\.|/)", "");
            parsed = parsed.replaceAll("(\\.|/)\\$", "");

            return parsed;
        } else
            return line;
    }

    private String formatUnit(String line) {
        if (FORMAT_UNIT) {
            // prepare
            String parsed = line.replaceAll("([0-9])([a-z])", "$1 $2");
            parsed = parsed.replaceAll("([a-z])([0-9])", "$1 $2");
            parsed = parsed.replaceAll(" x ", " xbi ");
            parsed = parsed.replaceAll("([0-9])( *)\\.( *)([0-9])", "$1.$4");

            // format units
            parsed = line.replaceAll("([0-9]+)( *)(inches|inch|in|')\\.?", "$1in. ");
            parsed = parsed.replaceAll("([0-9]+)( *)(foot|feet|ft|'')\\.?", "$1ft. ");
            parsed = parsed.replaceAll("([0-9]+)( *)(pounds|pound|lbs|lb)\\.?", "$1lb. ");
            parsed = parsed.replaceAll("([0-9]+)( *)(square|sq) ?\\.?(feet|foot|ft)\\.?", "$1sq.ft. ");
            parsed = parsed.replaceAll("([0-9]+)( *)(cubic|cu) ?\\.?(feet|foot|ft)\\.?", "$1cu.ft. ");
            parsed = parsed.replaceAll("([0-9]+)( *)(gallons|gallon|gal)\\.?", "$1gal. ");
            parsed = parsed.replaceAll("([0-9]+)( *)(ounces|ounce|oz)\\.?", "$1oz. ");
            parsed = parsed.replaceAll("([0-9]+)( *)(centimeters|cm)\\.?", "$1cm. ");
            parsed = parsed.replaceAll("([0-9]+)( *)(milimeters|mm)\\.?", "$1mm. ");
            parsed = parsed.replaceAll("°", "degrees ");
            parsed = parsed.replaceAll("([0-9]+)( *)(degrees|degree)\\.?", "$1deg. ");
            parsed = parsed.replaceAll(" v ", " volts ");
            parsed = parsed.replaceAll("([0-9]+)( *)(degrees|degree)\\.?", "$1volt. ");
            parsed = parsed.replaceAll("([0-9]+)( *)(watts|watt)\\.?", "$1watt. ");
            parsed = parsed.replaceAll("([0-9]+)( *)(amperes|ampere|amps|amp)\\.?", "$1amp. ");
            parsed = parsed.replaceAll("\\$([0-9]+)", "$1 dollars");
            parsed = parsed.replaceAll("([0-9]+)( *)(dollars|dollar|usd|bucks|buck)\\.?", "$1usd. ");


            return parsed;
        } else
            return line;
    }
    


    /**
     * Format input text according to property settings
     * @param text       Input string text
     * @return           Processed string text
    */
    public String format(String text) {
        // split words with a.A
        String parsed = text.replaceAll("(\\w)\\.([A-Z])", "$1 $2");

        // NOTE: must convert to lower case
        parsed = parsed.toLowerCase();

        // removals
        parsed = removeHtmlTag(parsed);
        parsed = removeHtmlCode(parsed);
        parsed = removeUrl(parsed);
        parsed = removeBetweenBrackets(parsed);

        // format
        parsed = formatEmail(parsed);
        parsed = formatUnit(parsed);
        parsed = formatPunctuation(parsed);
        
        // final processing (strong things)
        parsed = removeIrregularSymbols(parsed);
        parsed = removeExtraSpace(parsed);
        parsed = removeNonAlphanumeric(parsed);
        
        return parsed.toLowerCase();
    }
    
}
