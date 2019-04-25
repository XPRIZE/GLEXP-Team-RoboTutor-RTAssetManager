package cmu.xprize;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;


public class AudioHasher
{
  private MessageDigest md;
  protected StringBuffer masterLst;
  protected StringBuffer masterErr;
  protected String quality;
  protected boolean saveLocalLogs = false;
  
  public AudioHasher()
  {
    try
    {
      this.md = MessageDigest.getInstance("MD5");
    }
    catch (NoSuchAlgorithmException ex)
    {
      Logger.getLogger(AudioHasher.class.getName()).log(Level.SEVERE, null, ex);
    }
    
    this.quality = "low";
    
    this.masterLst = new StringBuffer();
    this.masterErr = new StringBuffer();
    
    String logText = "HASH,\tTEXT,\tFOLDER\r\n";
    this.masterLst.append(logText);
    
    String ErrText = "FILE OBJECT,\tFOLDER\r\n";
    this.masterErr.append(ErrText);
  }
  
  protected void setQuality(String newQuality) {
    this.quality = newQuality;
  }
  




  public String generateStrippedHash(String objectname)
  {
    System.out.println("filename   :" + objectname);
    

    String hashName = objectname.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
    System.out.println("prunedname :" + hashName);
    

    byte[] nameBytes = hashName.getBytes(Charset.forName("UTF-8"));
    
    nameBytes = this.md.digest(nameBytes);
    
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < nameBytes.length; i++) {
      sb.append(Integer.toString((nameBytes[i] & 0xFF) + 256, 16).substring(1));
    }
    
    System.out.println("hashname :" + sb.toString());
    
    return sb.toString();
  }
  


  protected String hashPath(String objectname)
  {
    String text = objectname.substring(0, objectname.length() - 4);
    
    return generateStrippedHash(text);
  }


  /**
   * Copy the assets from inputPath into outputPath.
   *
   * @param inputPath
   * @param outputPath
   * @param recurse
   */
  public void storeAssets(String inputPath, String outputPath, Boolean recurse)
  {
    File inputFile = new File(inputPath);
    
    if (inputFile.exists())
    {
      if (inputFile.isFile()) {
        processFileAssets(inputPath, outputPath);
      }
      else if (inputFile.isDirectory()) {
        processFolderAssets(inputPath, outputPath, recurse);
      }
      
      writeMasterLogs(AssetManager.LOG_FOLDER);
    }
    else {
      System.out.println("Error: Input Path Not Found ->" + inputPath);
    }
  }
  


  protected int processFolderAssets(String inputPath, String outputPath, Boolean recurse)
  {
    int fileCount = 0;
    String[] files = null;
    String[] folderList = null;
    

    StringBuffer folderLst = new StringBuffer();
    StringBuffer folderErr = new StringBuffer();
    
    folderList = AssetUtils.listFolder(inputPath, false);
    


    if (inputPath.length() > 0) {
      inputPath = inputPath + "/";
    }
    for (String objectname : folderList)
    {


      boolean skip = false;
      
      for (String test : AssetManager.RTIGNORE) {
        if (objectname.equals(test)) {
          skip = true;
          System.out.println("Skipping: " + objectname);
          break;
        }
      }
      if (!skip)
      {

        System.out.println("Processing: " + objectname);
        
        boolean success = true;
        
        String srcPath = inputPath + objectname;
        
        File element = new File(srcPath);
        
        if (element.isDirectory())
        {
          if (objectname.startsWith("quality_"))
          {
            String qualityType = objectname.replace("quality_", "");
            



            if (qualityType.equals(this.quality))
            {





              processFolderAssets(srcPath, outputPath, Boolean.valueOf(true));
            }
          }
          else {
            String outPath = outputPath + "/" + objectname;
            
            File outputFile = new File(outPath);
            
            if (!outputFile.exists()) {
              success = outputFile.mkdirs();
            }
            if ((success) && (recurse.booleanValue())) {
              int subCount = processFolderAssets(srcPath, outPath, Boolean.valueOf(true));
              
              if (subCount == 0) {
                outputFile.delete();
              }
              else {
                fileCount += subCount;
              }
            }
          }
        }
        else if (element.isFile())
        {
          fileCount++;
          


          if (objectname.endsWith(".mp3"))
          {
            String hashText = hashPath(objectname);
            
            String outPath = outputPath + "/" + hashText + ".mp3";
            
            String logText = hashText + ",\t" + objectname + ",\t" + outputPath + "\r\n";
            this.masterLst.append(logText);
            folderLst.append(logText);
            
            processFileAssets(srcPath, outPath);

          }
          else
          {
            String outPath = outputPath + "/" + objectname;
            
            processFileAssets(srcPath, outPath);
          }
        }
        else {
          System.out.println("Skipping: " + srcPath);
          
          String logText = objectname + ",\t" + outputPath + "\r\n";
          this.masterErr.append(logText);
          folderErr.append(logText);
        }
      }
    }
    if (this.saveLocalLogs) {
      saveLogString(folderLst.toString(), outputPath + "/local_cross_ref.csv");
      saveLogString(folderErr.toString(), outputPath + "/local_error_ref.csv");
    }
    
    return fileCount;
  }
  

  protected void processFileAssets(String inputPath, String outputPath)
  {
    OutputStream out = null;
    InputStream in = null;
    
    long iModDate = 0L;
    long oModDate = 0L;
    
    byte[] buffer = new byte['Ѐ'];
    
    try
    {
      File infile = new File(inputPath);
      File outfile = new File(outputPath);
      
      iModDate = infile.lastModified();
      
      if (outfile.exists()) {
        oModDate = outfile.lastModified();
      }
      
      if (iModDate != oModDate)
      {
        System.out.println("File copy:" + inputPath + " -to- " + outputPath);
        try
        {
          in = new FileInputStream(inputPath);
          out = new FileOutputStream(outputPath);
          int read;
          while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
          }
          
          in.close();
          out.flush();
          out.close();
        }
        catch (FileNotFoundException e) {
          System.out.println("INFO: Skipping missing file: " + inputPath + " - reason: " + e);
        }
        catch (IOException e) {
          System.out.println("ERROR: Failed to copy asset file: " + inputPath + " - reason: " + e);

        }
        finally
        {

          outfile.setLastModified(iModDate);
          
          in = null;
          out = null;
        }
      }
      else {
        System.out.println("File Unchanged - Skipped:" + inputPath + " -to- " + outputPath);
      }
    }
    catch (Exception e) {
      System.out.println("INFO: Date Check Failed: " + inputPath + " - reason: " + e);
    }
  }


  /**
   * Create the folder outPath on the connected device.
   * @param outPath
   * @return
   */
  public Boolean createFolderAsset(String outPath)
  {
    Runtime rt1 = Runtime.getRuntime();
    
    Boolean success = Boolean.valueOf(true);
    
    try
    {
      Process pr = rt1.exec("adb shell mkdir " + outPath);
      try
      {
        pr.waitFor();
      }
      catch (InterruptedException ex) {
        Logger.getLogger(AssetManager.class.getName()).log(Level.SEVERE, null, ex);
      }
      
      System.out.println("Mkdir Complete: " + outPath);
    }
    catch (IOException ex) {
      Logger.getLogger(AssetManager.class.getName()).log(Level.SEVERE, null, ex);
      success = Boolean.valueOf(false);
    }
    
    return success;
  }
  

  protected void createFolderAssets(String inputPath, String outputPath, Boolean recurse)
  {
    String[] files = null;
    String[] folderList = null;
    

    StringBuffer folderLst = new StringBuffer();
    StringBuffer folderErr = new StringBuffer();
    
    folderList = AssetUtils.listFolder(inputPath, true);
    


    if (inputPath.length() > 0) {
      inputPath = inputPath + "/";
    }
    for (String objectname : folderList)
    {
      boolean success = true;
      
      String srcPath = inputPath + objectname;
      
      File element = new File(srcPath);
      
      if (element.isDirectory())
      {
        String outPath = outputPath + "/" + objectname;
        
        createFolderAsset(outPath);
        


        createFolderAssets(srcPath, outPath, Boolean.valueOf(true));
      }
    }
  }
  


  public void writeMasterLogs(String targetPath)
  {
    saveLogString(this.masterLst.toString(), targetPath + "/master_cross_ref.csv");
    saveLogString(this.masterErr.toString(), targetPath + "/master_error_ref.csv");
  }
  

  protected void saveLogString(String outputData, String outputPath)
  {
    FileWriter logWriter = null;
    
    try
    {
      logWriter = new FileWriter(outputPath);
      System.out.println("File save: " + outputPath);
      
      logWriter.write(outputData); return;
    }
    catch (IOException ex) {
      Logger.getLogger(AudioHasher.class.getName()).log(Level.SEVERE, null, ex);
    }
    finally {
      try {
        logWriter.close();
      } catch (IOException ex) {
        Logger.getLogger(AudioHasher.class.getName()).log(Level.SEVERE, null, ex);
      }
    }
  }
}
