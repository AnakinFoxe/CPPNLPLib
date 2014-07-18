/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.csupomona.nlp.tool.translator;

import edu.csupomona.nlp.util.StanfordTools;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Xing
 */
public class Translator {
    
    private final Google gt;    // Google Translate API class
    
    private final String PATH_DATA = "./data/";
    private final String PATH_KEY = PATH_DATA + "google.txt";
    private final String PATH_STATUS = PATH_DATA + "translate_status.txt";
    
    private String REG_PATH_BASE;
    private String REG_PATH_TRANSLATE;
    
    public Translator(String source, String target) {
        String accessKey = readAccessKey();
        gt = new Google(accessKey, source, target);
        
        StanfordTools.init();
    }
    
    
    
    private String readAccessKey() {
        try {
            FileReader fr = new FileReader(PATH_KEY);
            BufferedReader br = new BufferedReader(fr);
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().length() > 0)
                    return line.trim();
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Translator.class.getName())
                    .log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Translator.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
        
        return null;
    }
    
    private void updatePathReg(String basePath, String translatePath) {
        String procBasePath = basePath.replaceAll("\\.", "");
        String procTranslatePath = translatePath.replaceAll("\\.", "");
        
        // only for windows platform
        if (System.getProperty("os.name").contains("Windows")) {
            procBasePath = procBasePath.replaceAll("/", "\\\\\\\\");
            procTranslatePath = procTranslatePath.replaceAll("/", "\\\\\\\\");
        }
        
        REG_PATH_BASE = procBasePath;
        REG_PATH_TRANSLATE = procTranslatePath;
    }
    
    private List<String> loadAllFilePath(String basePath) throws IOException {
        List<String> fileList = new ArrayList<>();
        
        File[] files = new File(basePath).listFiles();
        
        for (File file : files) {
            if (file.isDirectory())
                fileList.addAll(loadAllFilePath(file.getCanonicalPath()));
            else
                fileList.add(file.getCanonicalPath());
        }
        
        return fileList;
    }
    
    private HashSet<String> loadStatusFile() 
            throws FileNotFoundException, IOException {
        HashSet<String> processedFiles = new HashSet<>();
        
        FileReader fr = new FileReader(PATH_STATUS);
        BufferedReader br = new BufferedReader(fr);
        String line;
        while ((line = br.readLine()) != null) {
            String[] items = line.split(" ");
            processedFiles.add(items[0].trim());
        }
        
        return processedFiles;
    }
    
    private void createCorresFolders(String folderPath) 
            throws IOException {
        File[] files = new File(folderPath).listFiles();
        
        for (File file : files) {
            if (file.isDirectory()) {
                String path = file.getCanonicalPath();
                
                // get corresponding folder path
                String newPath = getNewFilePath(path);
                
                // if the folder does not exist, create it
                if (!new File(newPath).exists()) {
                    System.out.println("Creating: " + newPath);
                    new File(newPath).mkdir();
                }
                
                // go recurrsive
                createCorresFolders(path);
            }
        }
    }
    
    
    private List<String> parseDoc(BufferedReader br) throws IOException {
        List<String> text = new ArrayList<>();
        String line;
        String paragraph = "";
        
        boolean isText = false;
        while ((line = br.readLine()) != null) {
            // setting start and end point
            if (line.contains("<TEXT>"))
                isText = true;
            else if (line.contains("</TEXT>"))
                isText = false;
            
            // removing <***> and </***>
            line = line.replaceAll("<[A-Z]+>", "");
            line = line.replaceAll("</[A-Z]+>", "");
            
            if ((isText == true) && (line.length() > 0)) {
                // when meets a new paragraph, converts the collected paragraph
                // into sentences and reset the paragraph
                if (line.matches("^[\\s]+.*")) {
                    paragraph = paragraph.replaceAll("[ ]+", " ");
                    text.addAll(StanfordTools.sentence(paragraph));
                    text.add("\n");
                    paragraph = ""; // reset
                }
                
                // replace those strange `` and '' with "
                line = line.replaceAll("[`]{2,5}", "\"");
                line = line.replaceAll("[']{2,5}", "\"");
                
                // add this new line into paragraph
                paragraph += " " + line;
            }
        }
        
        // last paragraph
        if (paragraph.length() > 1) {
            paragraph = paragraph.replaceAll("[ ]+", " ");
            text.addAll(StanfordTools.sentence(paragraph));
            text.add("\n");
        }

        return text;
    }
    
    
    private List<String> parseFile(String filePath) 
            throws FileNotFoundException, IOException  {
        List<String> content = new ArrayList<>();
        
        FileReader fr = new FileReader(filePath);
        BufferedReader br = new BufferedReader(fr);
        
        // read first line to tell the type of file
        String line;
        while ((line = br.readLine()) != null) {
            if (line.trim().length() > 0) {
                if (line.contains("<DOC>")) {
                    // DUC documents
                    content.addAll(parseDoc(br));
                } else if (line.contains("DUC 2004")) {
                    // duc2004.task5.topicsets
                    // do nothing about it currently
                    
                } else {
                    // model or peer summaries
                    do {
                        // replace those strange `` and '' with "
                        line = line.replaceAll("[`]{2,5}", "\"");
                        line = line.replaceAll("[']{2,5}", "\"");
                        
                        content.add(line.trim());
                    } while((line = br.readLine()) != null);
                }
                break;
            }
        }
        
        return content;
    }
    
    private String replaceHtmlEntity(String text) {
        text = text.replaceAll("&quot;", "\"");
        text = text.replaceAll("&amp;", "&");
        text = text.replaceAll("&lt;", "<");
        text = text.replaceAll("&gt;", ">");
        text = text.replaceAll("&apos;", "'");
        
        return text;
    }
    
    private List<String> translate(List<String> text) 
            throws MalformedURLException, IOException {
        List<String> translatedText = new ArrayList<>();
        
        for (String line : text) {
            if (line.trim().length() > 0) 
                translatedText.add(replaceHtmlEntity(gt.translate(line.trim())));
            else
                translatedText.add(line);   // keep empty lines
        }
        
        return translatedText;
    }
    
    private String getNewFilePath(String filePath) {
        return filePath.replaceAll(REG_PATH_BASE, REG_PATH_TRANSLATE);
    }
    
    private void writeTranslatedText(List<String> text, String filePath) 
            throws IOException {
        FileWriter fw = new FileWriter(filePath, false);    // overwrite
        try (BufferedWriter bw = new BufferedWriter(fw)) {
            for (String line : text) 
                bw.write(line + "\n");
        }
    }
    
    private Integer getCharCount(List<String> text) {
        Integer charCount = 0;
        for (String line : text) 
            charCount += line.length();
        
        return charCount;
    }
   
    private void updateStatusFile(String filePath, Integer charCount) 
            throws IOException {
        FileWriter fw = new FileWriter(PATH_STATUS, true);
        
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd:HH:mm:ss");
        try (BufferedWriter bw = new BufferedWriter(fw)) {
            bw.write(filePath + " " 
                    + charCount.toString() + " "
                    + sdf.format(cal.getTime()) + "\n");
        }
    }
    
    public void run(Integer quota, String basePath, String translatePath) 
            throws IOException {
        // update path info
        updatePathReg(basePath, translatePath);
        
        // load all the files in the basePath, include subfolders
        List<String> filePaths = loadAllFilePath(basePath);
        
        // load processed file path for status file
        HashSet<String> processedFiles = loadStatusFile();
        
        // create corresponding folder if it does not exist
        createCorresFolders(basePath);
        
        for (String filePath : filePaths) {
            if (processedFiles.contains(filePath))
                continue;
            
            // parse the file and get the content
            List<String> content = parseFile(filePath);
            
            // check the quota
            Integer charCount = getCharCount(content);
            if ((quota -= charCount) <= 0) {
                System.out.println("Meets quota, end of work today.");
                break;
            }
            
            // translate the content
            List<String> translatedText = translate(content);
            
            // get file path for translated text
            String newFilePath = getNewFilePath(filePath);
            
            // write the translated text 
            writeTranslatedText(translatedText, newFilePath);
            
            // update the translation status (record the original file path)
            updateStatusFile(filePath, charCount);
            
            System.out.println("Done writing: " 
                    + filePath + " (" + charCount.toString() 
                    + ", quota: " + quota.toString() + ")... " 
                    + (filePaths.indexOf(filePath)+1) + "/"
                    + filePaths.size());
        }
    }
    
//    public static void main(String[] args){
//        Translator t = new Translator("en", "es");
//        
//        try {
//            t.run(2000000, "./data/duc/", "./data/translated/spanish/");
//        } catch (IOException ex) {
//            // when IOException is thrown out
//            // it means something wrong with the translation request
//            // most likely to be:
//            //      error 403: dailiy limit exceeded
//            //      error 400: key invalid 
//            Logger.getLogger(Translator.class.getName()).
//                    log(Level.SEVERE, null, ex);
//        }
// 
//
//    }
}
