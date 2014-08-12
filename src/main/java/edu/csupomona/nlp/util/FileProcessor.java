/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.csupomona.nlp.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * File Processor to process batch of files.
 * Default use without overriding any methods could be used to parse DUC2004 files.
 * @author Xing
 */
public class FileProcessor {
    
    // regular expression for generating new folders and files
    private String REG_PATH_SRC;
    private String REG_PATH_DST;
    
    private boolean isRegPathUpdated;   // a protection flag
    
    public FileProcessor() {
        isRegPathUpdated = false;
    }
    
    /**
     * Update regular expression based on source and target path
     * @param sourcePath        Path of the files to be translated
     * @param targetPath        Path of translated files
     */
    protected void updatePathReg(String sourcePath, String targetPath) {
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
    protected String getNewFilePath(String filePath) {
        if (isRegPathUpdated)
            return filePath.replaceAll(REG_PATH_SRC, REG_PATH_DST);
        else
            return null;
    }
    
    /**
     * Create corresponding directory hierarchy according to source.
     * @param sourcePath            Source path 
     * @throws IOException 
     * @throws NullPointerException
     */
    protected void createCorresFolders(String sourcePath) 
            throws IOException, NullPointerException {
        File[] files = new File(sourcePath).listFiles();
        
        for (File file : files) {
            if (file.isDirectory()) {
                String path = file.getCanonicalPath();
                
                // get corresponding folder path
                String newPath = getNewFilePath(path);
                
                // if the folder does not exist, create it
                if (!new File(newPath).exists()) {
                    if (new File(newPath).mkdir())
                        System.out.println("Created: " + newPath);
                    else
                        System.out.println("Failed creating: " + newPath);
                }
                
                // go recurrsive
                createCorresFolders(path);
            }
        }
    }
    
    /**
     * Read through the path and record all the files into a list.
     * Note that directories will be excluded.
     * @param basePath          The path to be read
     * @return                  List of canonical file paths
     * @throws IOException 
     */
    protected List<String> loadAllFilePath(String basePath) throws IOException {
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
     * Parse the file into a list of sentences.
     * Child class need to expand this method for specific parsing.
     * @param filePath      Path to the target file
     * @return              List of sentences from the file
     * @throws FileNotFoundException
     * @throws IOException
     */
    protected List<String> processFile(String filePath) 
            throws FileNotFoundException, IOException  {
        List<String> content = new ArrayList<>();
        
        FileReader fr = new FileReader(filePath);
        BufferedReader br = new BufferedReader(fr);
        
        // read first line to tell the type of file
        String line;
        while ((line = br.readLine()) != null) {
            if (line.trim().length() > 0) 
                content.add(line.trim());
        }
        
        return content;
    }
    
    /**
     * Write text to file.
     * Note: Will overwrite the old file if names are the same.
     * @param text          List of sentences
     * @param filePath      Path to the file
     * @throws IOException 
     */
    protected void writeText(List<String> text, String filePath) 
            throws IOException {
        FileWriter fw = new FileWriter(filePath, false);    // overwrite
        try (BufferedWriter bw = new BufferedWriter(fw)) {
            for (String line : text) 
                bw.write(line + "\n");
        }
    }
    
    /**
     * Process every file from the source path and save processed
     * file to destination path with the same hierarchy.
     * @param srcPath           Path to the files to be translated
     * @param dstPath           Path for translated files
     * @throws IOException
     */
    public void process(String srcPath, String dstPath) 
            throws IOException {
        // update path info
        updatePathReg(srcPath, dstPath);
        
        // load all the files in the basePath, include subfolders
        List<String> filePaths = loadAllFilePath(srcPath);
        
        // create corresponding folder if it does not exist
        createCorresFolders(srcPath);
        
        for (String filePath : filePaths) {
            // parse the file and get the content
            List<String> content = processFile(filePath);
            
            // get file path for translated text
            String newFilePath = getNewFilePath(filePath);
            
            // write the translated text 
            writeText(content, newFilePath);
            
            System.out.println("Done writing ("
                    + (filePaths.indexOf(filePath)+1) + "/"
                    + filePaths.size()
                    + "): " + newFilePath);
        }
    }
    
}
