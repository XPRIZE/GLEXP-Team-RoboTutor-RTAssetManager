package cmu.xprize;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AssetUtils
{
  static MessageDigest md;
  
  public AssetUtils()
  {
    try
    {
      md = MessageDigest.getInstance("MD5");
    }
    catch (NoSuchAlgorithmException ex)
    {
      Logger.getLogger(AudioHasher.class.getName()).log(Level.SEVERE, null, ex);
    }
  }
  
  public void cleanFolder(final String path, final boolean deleteSelf)
  {
    try {
      File folder = new File(path);
      if (folder.exists())
      {
        Path directory = Paths.get(path, new String[0]);
        
        Files.walkFileTree(directory, new SimpleFileVisitor()
        {
          public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
          {
            Files.delete(file);
            return FileVisitResult.CONTINUE;
          }
          
          public FileVisitResult postVisitDirectory(Path dir, IOException exc)
            throws IOException
          {
            if ((!dir.toString().equals(path)) || (deleteSelf)) {
              Files.delete(dir);
            }
            return FileVisitResult.CONTINUE;
          }
        });
      }
    }
    catch (IOException ex) {
      Logger.getLogger(AssetManager.class.getName()).log(Level.SEVERE, null, ex);
    }
  }
  



  public static String generateStrippedHash(String objectname)
  {
    System.out.println("filename   :" + objectname);
    

    String hashName = objectname.replaceAll("[^a-zA-Z0-9]", "");
    System.out.println("prunedname :" + hashName);
    

    byte[] nameBytes = hashName.getBytes(Charset.forName("UTF-8"));
    
    nameBytes = md.digest(nameBytes);
    
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < nameBytes.length; i++) {
      sb.append(Integer.toString((nameBytes[i] & 0xFF) + 256, 16).substring(1));
    }
    
    System.out.println("hashname :" + sb.toString());
    
    return sb.toString();
  }
  

  public static String generateCaseNormalHash(String objectname)
  {
    System.out.println("filename   :" + objectname);
    

    byte[] nameBytes = objectname.getBytes(Charset.forName("UTF-8"));
    
    nameBytes = md.digest(nameBytes);
    
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < nameBytes.length; i++) {
      sb.append(Integer.toString((nameBytes[i] & 0xFF) + 256, 16).substring(1));
    }
    
    System.out.println("hashname :" + sb.toString());
    
    return sb.toString();
  }
  

  public static String[] listFolder(String path, boolean quiet)
  {
    File folder = new File(path);
    String[] names = new String[0];
    int i1 = 0;
    
    File[] listOfFiles = folder.listFiles();
    
    if (listOfFiles != null)
    {
      names = new String[listOfFiles.length];
      
      if (!quiet) {
        System.out.println("Listing folder: " + path);
      }
      for (File fileObj : listOfFiles)
      {
        names[(i1++)] = fileObj.getName();
        
        if (!quiet) {
          if (fileObj.isFile()) {
            System.out.println("File " + fileObj.getName());
          } else if (fileObj.isDirectory()) {
            System.out.println("Folder " + fileObj.getName());
          }
        }
      }
    }
    
    return names;
  }
}
