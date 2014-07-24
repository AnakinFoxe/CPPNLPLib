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
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Translator using Google Translate API
 * @author Xing
 */
public class Translator {
    
    private final Google gt;    // Google Translate API class
    
    private final StanfordTools stan;   // Stanford NLP tools
    
    // essential path info
    private final String PATH_DATA;
    private final String PATH_KEY;
    private final String PATH_STATUS;
    
    // regular expression for generating new folders and files
    private String REG_PATH_SRC;
    private String REG_PATH_DST;
    
    private boolean isRegPathUpdated = false;   // a protection flag
    
    /**
     * Constructor. 
     * @param basePath      Base path for the data directory. 
     *                      Following files must be present:
     *                      1) google.txt contains Google access key 
     *                      2) translate_status.txt record translation status
     * @param source        Source text language. E.g. "en" for English
     * @param target        Target text language. E.g. "zh-CN" for Chinese
     */
    public Translator(String basePath, String source, String target) {
        this.PATH_DATA = basePath;
        this.PATH_KEY = this.PATH_DATA + "google.txt";
        this.PATH_STATUS = this.PATH_DATA + "translate_status.txt";
        
        String accessKey = readAccessKey();
        gt = new Google(accessKey, source, target);
        
        // Stanford NLP with only tokenize and ssplit would be much faster
        Properties props = new Properties();
        props.put("annotators", "tokenize, ssplit");
        stan = new StanfordTools(props);
    }
    
    
    /**
     * Read access key for Google Translate service
     * @return          Google access key
     */
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
    
    /**
     * Update regular expression based on source and target path
     * @param sourcePath        Path of the files to be translated
     * @param targetPath        Path of translated files
     */
    private void updatePathReg(String sourcePath, String targetPath) {
        // removing the heading dot. e.g. "./data"
        String procSource = sourcePath.replaceAll("^\\.", "");
        String procTarget = targetPath.replaceAll("^\\.", "");
        
        // only for windows platform
        if (System.getProperty("os.name").contains("Windows")) {
            procSource = procSource.replaceAll("/", "\\\\\\\\");
            procTarget = procTarget.replaceAll("/", "\\\\\\\\");
        }
        
        REG_PATH_SRC = procSource;
        REG_PATH_DST = procTarget;
        
        isRegPathUpdated = true;
    }
    
    /**
     * Generate corresponding new file path using regular expression
     * @param filePath      Original path
     * @return              Corresponding new path
     */
    private String getNewFilePath(String filePath) {
        if (isRegPathUpdated)
            return filePath.replaceAll(REG_PATH_SRC, REG_PATH_DST);
        else
            return null;
    }
    
    /**
     * Read through the path and record all the files into a list.
     * Note that directories will be excluded.
     * @param basePath          The path to be read
     * @return                  List of canonical file paths
     * @throws IOException 
     */
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
    
    /**
     * Read the translate_status.txt file to retrieve list of translated 
     * files.
     * @return          List of files already translated
     * @throws FileNotFoundException
     * @throws IOException 
     */
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
    
    /**
     * Create corresponding directory hierarchy according to source.
     * @param sourcePath            Source path 
     * @throws IOException 
     * @throws NullPointerException
     */
    private void createCorresFolders(String sourcePath) 
            throws IOException, NullPointerException {
        File[] files = new File(sourcePath).listFiles();
        
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
    
    /**
     * Parse DUC2004 document
     * @param br        BufferedReader to the file
     * @return          List of sentences from the text section of the file
     * @throws IOException
     */
    protected List<String> parseDoc(BufferedReader br) throws IOException {
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
                    text.addAll(stan.sentence(paragraph));
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
            text.addAll(stan.sentence(paragraph));
            text.add("\n");
        }

        return text;
    }
    
    /**
     * Parse the file into a list of sentences
     * @param filePath      Path to the target file
     * @return              List of sentences from the file
     * @throws FileNotFoundException
     * @throws IOException
     */
    protected List<String> parseFile(String filePath) 
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
    
    /**
     * Replace HTML character entities with corresponding punctuations
     * @param text      Input text 
     * @return          Processed text
     */
    private String replaceHtmlEntity(String text) {
        String procText = text.replaceAll("&quot;", "\"");
        procText = procText.replaceAll("&amp;", "&");
        procText = procText.replaceAll("&lt;", "<");
        procText = procText.replaceAll("&gt;", ">");
        procText = procText.replaceAll("&apos;", "'");
        
        return procText;
    }
    
    /**
     * Call Google Translate API and translate the input sentences
     * @param text      List of sentences to be translated
     * @return          List of translated sentences
     * @throws MalformedURLException
     * @throws IOException 
     */
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
    
    /**
     * Write text to file.
     * Note: Will overwrite the old file if names are the same.
     * @param text          List of sentences
     * @param filePath      Path to the file
     * @throws IOException 
     */
    private void writeText(List<String> text, String filePath) 
            throws IOException {
        FileWriter fw = new FileWriter(filePath, false);    // overwrite
        try (BufferedWriter bw = new BufferedWriter(fw)) {
            for (String line : text) 
                bw.write(line + "\n");
        }
    }
    
    /**
     * Compute the character count of the list of sentences.
     * Note: Whitespace is included.
     * @param text      List of sentences
     * @return 
     */
    private Integer getCharCount(List<String> text) {
        Integer charCount = 0;
        for (String line : text) 
            charCount += line.length();
        
        return charCount;
    }
   
    /**
     * Update translation status into translate_status.txt file
     * @param filePath      The source file which is already translated
     * @param charCount     The character count of the source file
     * @throws IOException 
     */
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
    
    /**
     * Start translation
     * @param quota             Daily quota limit for Google translate
     * @param sourcePath        Path to the files to be translated
     * @param translatePath     Path for translated files
     * @throws IOException
     */
    public void run(Integer quota, String sourcePath, String translatePath) 
            throws IOException {
        // update path info
        updatePathReg(sourcePath, translatePath);
        
        // load all the files in the basePath, include subfolders
        List<String> filePaths = loadAllFilePath(sourcePath);
        
        // load processed file path for status file
        HashSet<String> processedFiles = loadStatusFile();
        
        // create corresponding folder if it does not exist
        createCorresFolders(sourcePath);
        
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
            writeText(translatedText, newFilePath);
            
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
