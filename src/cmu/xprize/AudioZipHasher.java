package cmu.xprize;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;





public class AudioZipHasher
  extends AudioHasher
{
  private ZipOutputStream zipOutputStream = null;
  







  public void storeAssets(String inputPath, String outputPath, Boolean recurse)
  {
    try
    {
      this.zipOutputStream = new ZipOutputStream(new FileOutputStream(outputPath));
      
      File inputFile = new File(inputPath);
      
      if (inputFile.isFile()) {
        processFileAssets(inputPath, "");
      }
      else if (inputFile.isDirectory()) {
        processFolderAssets(inputPath, "", recurse);
      }
      





      this.zipOutputStream.close(); return;
    }
    catch (FileNotFoundException ex)
    {
      Logger.getLogger(AudioZipHasher.class.getName()).log(Level.SEVERE, null, ex);
    }
    catch (IOException ex)
    {
      Logger.getLogger(AudioZipHasher.class.getName()).log(Level.SEVERE, null, ex);
    }
    finally
    {
      try {
        this.zipOutputStream.close();
      } catch (IOException ex) {
        Logger.getLogger(AudioZipHasher.class.getName()).log(Level.SEVERE, null, ex);
      }
    }
  }
  


  protected int processFolderAssets(String inputPath, String outputPath, Boolean recurse)
  {
    int fileCount = 0;
    String DstPath = outputPath;
    String[] files = null;
    String[] folderList = null;
    

    StringBuffer folderLst = new StringBuffer();
    StringBuffer folderErr = new StringBuffer();
    try
    {
      ZipEntry folderZipEntry;
      if (outputPath.length() > 0)
      {
        DstPath = outputPath + "/";
        
        folderZipEntry = new ZipEntry(DstPath);
        this.zipOutputStream.putNextEntry(folderZipEntry);
      }
      folderList = AssetUtils.listFolder(inputPath, false);
      


      if (inputPath.length() > 0) {
        inputPath = inputPath + "/";
      }
      for (String objectname : folderList)
      {


        boolean skip = false;
        
        for (String test : AssetManager.ZIPIGNORE) {
          if (objectname.equals(test)) {
            skip = true;
            System.out.println("Skipping: " + objectname);
            break;
          }
        }
        if (!skip)
        {

          System.out.println("Processing: " + objectname);
          
          String srcPath = inputPath + objectname;
          
          File element = new File(srcPath);
          
          if (element.isDirectory())
          {
            if (objectname.startsWith("quality_"))
            {
              String qualityType = objectname.replace("quality_", "");
              



              if (qualityType.equals(this.quality))
              {



                processVirtualFolderAssets(srcPath, outputPath, Boolean.valueOf(true));
              }
            }
            else
            {
              String outPath = DstPath + objectname;
              
              if (recurse.booleanValue()) {
                int subCount = processFolderAssets(srcPath, outPath, Boolean.valueOf(true));
                
                if (subCount != 0)
                {


                  fileCount += subCount;
                }
              }
            }
          }
          else if (element.isFile())
          {
            fileCount++;
            


            if (objectname.toLowerCase().endsWith(".mp3"))
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
        } }
      if (outputPath.length() > 0) {
        this.zipOutputStream.closeEntry();
      }
    }
    catch (IOException ex)
    {
      Logger.getLogger(AudioZipHasher.class.getName()).log(Level.SEVERE, null, ex);
    }
    
    if (this.saveLocalLogs) {
      saveLogString(folderLst.toString(), DstPath + "local_cross_ref.csv");
      saveLogString(folderErr.toString(), DstPath + "local_error_ref.csv");
    }
    
    return fileCount;
  }
  

  protected void processVirtualFolderAssets(String inputPath, String outputPath, Boolean recurse)
  {
    String DstPath = outputPath;
    String[] files = null;
    String[] folderList = null;
    

    StringBuffer folderLst = new StringBuffer();
    StringBuffer folderErr = new StringBuffer();
    
    if (outputPath.length() > 0)
    {
      DstPath = outputPath + "/";
    }
    
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
        
        String srcPath = inputPath + objectname;
        
        File element = new File(srcPath);
        
        if (element.isDirectory())
        {
          if (objectname.startsWith("quality_"))
          {
            String qualityType = objectname.replace("quality_", "");
            



            if (qualityType.equals(this.quality))
            {



              processVirtualFolderAssets(srcPath, outputPath, Boolean.valueOf(true));
            }
          }
          else
          {
            String outPath = DstPath + objectname;
            
            if (recurse.booleanValue()) {
              processFolderAssets(srcPath, outPath, Boolean.valueOf(true));
            }
          }
        }
        else if (element.isFile())
        {


          if (objectname.toLowerCase().endsWith(".mp3"))
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
  }
  



  protected void processFileAssets(String inputPath, String fileName)
  {
    try
    {
      long sourceFileTime = new File(inputPath).lastModified();
      
      ZipEntry zipEntry = new ZipEntry(fileName);
      
      this.zipOutputStream.putNextEntry(zipEntry);
      
      FileInputStream fileInputStream = new FileInputStream(inputPath);
      byte[] buf = new byte['Ѐ'];
      

      int bytesRead;
      
      while ((bytesRead = fileInputStream.read(buf)) > 0) {
        this.zipOutputStream.write(buf, 0, bytesRead);
      }
      

      this.zipOutputStream.closeEntry();
      


      zipEntry.setTime(sourceFileTime);
    }
    catch (IOException ex)
    {
      Logger.getLogger(AudioZipHasher.class.getName()).log(Level.SEVERE, null, ex);
    }
  }
  


  protected void saveLogString(String outputData, String outputPath)
  {
    try
    {
      ZipEntry zipEntry = new ZipEntry(outputPath);
      this.zipOutputStream.putNextEntry(zipEntry);
      
      this.zipOutputStream.write(outputData.getBytes(), 0, outputData.length());
      

      this.zipOutputStream.closeEntry();
    }
    catch (IOException ex)
    {
      Logger.getLogger(AudioZipHasher.class.getName()).log(Level.SEVERE, null, ex);
    }
  }
}
