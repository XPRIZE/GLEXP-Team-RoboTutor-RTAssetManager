package cmu.xprize;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import util.AssetUtils;
import util.CAudio_Data;
import util.JSON_Util;


































public class StoryManager
{
  private String[] folderList;
  private HashMap<String, String> wordMap;
  private int wordDups;
  private HashMap<String, String> lineMap;
  private int lineDups;
  private HashMap<Integer, Integer> levelMap;
  private ArrayList<Story_Index> stories;
  private ArrayList<ArrayList<String>> listOfLevels;
  protected StringBuffer masterLst;
  protected StringBuffer masterErr;
  
  public void normalizeSubFolderNames(String inputPath)
  {
    this.folderList = AssetUtils.listFolder(inputPath, true);
    
    for (String objectname : this.folderList)
    {
      String srcPath = inputPath + "\\" + objectname;
      
      File element = new File(srcPath);
      
      if (element.isDirectory())
      {
        System.out.println("Processing Subfolder: " + objectname);
        
        normalizeEnFolderNames(inputPath + "\\" + objectname, objectname);
      }
    }
  }
  

  public void normalizeEnFolderNames(String inputPath, String protoType)
  {
    int normalIndex = 1;
    
    this.folderList = AssetUtils.listFolder(inputPath, true);
    
    for (String objectname : this.folderList)
    {
      System.out.println("Processing: " + objectname);
      
      String srcPath = inputPath + "\\" + objectname;
      
      File element = new File(srcPath);
      
      if (element.isDirectory())
      {
        String dstPath = inputPath + "\\" + protoType + "_" + normalIndex++;
        
        File rename = new File(dstPath);
        
        element.renameTo(rename);
      }
    }
  }
  

  private int parseLevel(String name)
  {
    int levelIndex = -1;
    Integer localInteger1;
    Integer localInteger2; for (Integer i1 = Integer.valueOf(100); i1.intValue() > 0; localInteger2 = i1 = Integer.valueOf(i1.intValue() - 1))
    {
      if (name.contains(i1.toString())) {
        levelIndex = i1.intValue();
        break;
      }
      localInteger1 = i1;
    }
    




    return levelIndex;
  }
  

  public boolean isAudioFile(String filename)
  {
    boolean result = false;
    
    if (filename.endsWith(".wav")) {
      result = true;
    }
    return result;
  }
  
  public boolean isSegFile(String filename)
  {
    boolean result = false;
    
    if ((filename.endsWith(".seg")) || 
      (filename.endsWith(".txt")) || 
      (filename.endsWith(".kb"))) {
      result = true;
    }
    return result;
  }
  
  public boolean isStoryFile(String filename)
  {
    boolean result = false;
    
    if ((filename.endsWith(".json")) || 
      (filename.endsWith(".txt")) || 
      (filename.endsWith(".bmp")) || 
      (filename.endsWith(".png")) || 
      (filename.endsWith(".jpeg")) || 
      (filename.endsWith(".jpg")))
    {
      result = true;
    }
    return result;
  }
  






  public boolean folderHasStoryContent(String[] storyList)
  {
    boolean result = false;
    
    for (String storyFile : storyList)
    {
      if (storyFile.endsWith(".wav")) {
        result = true;
      }
    }
    
    return result;
  }
  

  public boolean hasStoryContent(String storyPath)
  {
    String[] storyFiles = AssetUtils.listFolder(storyPath, true);
    
    return folderHasStoryContent(storyFiles);
  }
  


  public int migrateStoryContentA(String storyPath, String outputPath, Integer storyIndex, Integer levelIndex, String language)
  {
    int validStory = 0;
    
    String[] storyFiles = AssetUtils.listFolder(storyPath, true);
    
    String storyBase = outputPath + "/assets/story/" + language + "/";
    String audioBase = outputPath + "/assets/audio/" + language + "/cmu/xprize/story_reading/";
    String audioHigh = audioBase + "quality_high";
    String audioLow = audioBase + "quality_low";
    String audioOrig = audioBase + "quality_orig/";
    
    String levelFolder = levelIndex.toString();
    String storyFolder = levelIndex.toString() + "_" + storyIndex.toString();
    String segFolder = "segmentation";
    
    if (folderHasStoryContent(storyFiles))
    {
      System.out.println("Migrating Story: " + storyIndex + "  - in Folder: " + storyPath);
      validStory = 1;
      
      File newFile = new File(audioHigh);
      newFile.mkdirs();
      newFile = new File(audioLow);
      newFile.mkdirs();
      
      Story_Data story = extractStoryData(storyPath);
      
      String name = story.story_name;
      String title = story.title;
      
      String logText = language + "|\t" + storyFolder + "|\t" + (name != null ? name : title) + "\n";
      this.masterLst.append(logText);
      
      for (String storyFile : storyFiles)
      {
        String srcPath = storyPath + "/" + storyFile;
        
        File element = new File(srcPath);
        
        String contentPath = storyBase + levelFolder + "/" + storyFolder;
        String audioPathOrig = audioOrig + levelFolder + "/" + storyFolder;
        String segPath = audioOrig + levelFolder + "/" + storyFolder + "/" + segFolder;
        
        if (element.isFile())
        {
          if (isStoryFile(storyFile)) {
            newFile = new File(contentPath);
            newFile.mkdirs();
            
            copyFile(srcPath, contentPath + "/" + storyFile);
          }
          if (isAudioFile(storyFile)) {
            newFile = new File(audioPathOrig);
            newFile.mkdirs();
            
            copyFile(srcPath, audioPathOrig + "/" + storyFile);
          }
          if (isSegFile(storyFile)) {
            newFile = new File(segPath);
            newFile.mkdirs();
            
            copyFile(srcPath, segPath + "/" + storyFile);
          }
        }
      }
    }
    
    return validStory;
  }
  









  public void migrateStoryLevelContentB(String levelPath, String outputPath, String levelFolder, String language)
  {
    String[] storyList = AssetUtils.listFolder(levelPath, true);
    
    for (String storyFolder : storyList)
    {
      String srcPath = levelPath + "/" + storyFolder;
      
      File element = new File(srcPath);
      
      if (element.isDirectory())
      {
        migrateStoryContentB(srcPath, outputPath, levelFolder, storyFolder, language);
      }
    }
  }
  














  public int migrateStoryContentB(String storyPath, String outputPath, String levelFolder, String storyFolder, String language)
  {
    int validStory = 0;
    
    String[] storyFiles = AssetUtils.listFolder(storyPath, true);
    
    String storyBase = outputPath + "/assets/story/" + language + "/";
    String audioBase = outputPath + "/assets/audio/" + language + "/cmu/xprize/story_reading/";
    String audioHigh = audioBase + "quality_high";
    String audioLow = audioBase + "quality_low";
    String audioOrig = audioBase + "quality_orig/";
    String segFolder = "segmentation";
    
    System.out.println("Migrating Story: " + storyFolder);
    validStory = 1;
    
    File newFile = new File(audioHigh);
    newFile.mkdirs();
    newFile = new File(audioLow);
    newFile.mkdirs();
    
    Story_Data story = extractStoryData(storyPath);
    
    String name = story.story_name;
    String title = story.title;
    
    String logText = language + "|\t" + storyFolder + "|\t" + (name != null ? name : title) + "\n";
    this.masterLst.append(logText);
    
    for (String storyFile : storyFiles)
    {
      String srcPath = storyPath + "/" + storyFile;
      
      File element = new File(srcPath);
      
      String contentPath = storyBase + levelFolder + "/" + storyFolder;
      String audioPathOrig = audioOrig + levelFolder + "/" + storyFolder;
      String segPath = audioOrig + levelFolder + "/" + storyFolder + "/" + segFolder;
      
      if (element.isFile())
      {
        if (isStoryFile(storyFile)) {
          newFile = new File(contentPath);
          newFile.mkdirs();
          
          copyFile(srcPath, contentPath + "/" + storyFile);
        }
        if (isAudioFile(storyFile)) {
          newFile = new File(audioPathOrig);
          newFile.mkdirs();
          
          copyFile(srcPath, audioPathOrig + "/" + storyFile);
        }
        if (isSegFile(storyFile)) {
          newFile = new File(segPath);
          newFile.mkdirs();
          
          copyFile(srcPath, segPath + "/" + storyFile);
        }
      }
    }
    
    return validStory;
  }
  







  public void migrateStories(String inputPath, String outputPath, String language)
  {
    this.masterLst = new StringBuffer();
    
    String logText = "LANGUAGE|\tSTORY_FOLDER|\tSTORY_NAME\n";
    this.masterLst.append(logText);
    
    inputPath = inputPath + File.separator + language;
    
    String[] levelList = AssetUtils.listFolder(inputPath, true);
    
    for (String levelFolder : levelList)
    {
      String srcPath = inputPath + "/" + levelFolder;
      
      File element = new File(srcPath);
      
      if (element.isDirectory())
      {
        System.out.println("Migrating Level: " + levelFolder);
        
        migrateStoryLevelContentB(srcPath, outputPath, levelFolder, language);
      }
    }
    
    saveLogString(this.masterLst.toString(), outputPath + "/migration_cross_ref.csv");
  }
  









  public void migrateStoryLevelContentA(String levelPath, String outputPath, int levelIndex, String language)
  {
    String[] storyList = AssetUtils.listFolder(levelPath, true);
    
    int storyIndex = 1;
    
    for (String storyFolder : storyList)
    {
      String srcPath = levelPath + "/" + storyFolder;
      
      File element = new File(srcPath);
      
      if (element.isDirectory())
      {
        storyIndex += migrateStoryContentA(srcPath, outputPath, Integer.valueOf(storyIndex), Integer.valueOf(levelIndex), language);
      }
    }
  }
  







  public void migrateNumberedStories(String inputPath, String outputPath, String language)
  {
    this.masterLst = new StringBuffer();
    
    String logText = "LANGUAGE|\tSTORY_FOLDER|\tSTORY_NAME\n";
    this.masterLst.append(logText);
    
    String[] levelList = AssetUtils.listFolder(inputPath, true);
    
    for (String levelFolder : levelList)
    {


      int levelIndex = parseLevel(levelFolder);
      


      if (levelIndex != -1)
      {
        String srcPath = inputPath + "/" + levelFolder;
        
        File element = new File(srcPath);
        
        if (element.isDirectory())
        {
          System.out.println("Migrating Level: " + levelIndex + "  - in Folder: " + levelFolder);
          
          migrateStoryLevelContentA(srcPath, outputPath, levelIndex, language);
        }
      }
    }
    
    saveLogString(this.masterLst.toString(), outputPath + "/migration_cross_ref.csv");
  }
  







  public void migrateFlatStories(String inputPath, String outputPath, String language)
  {
    this.masterLst = new StringBuffer();
    
    String logText = "LANGUAGE|\tSTORY_FOLDER|\tSTORY_NAME\n";
    this.masterLst.append(logText);
    

    HashMap<Integer, Integer> levelCount = new HashMap();
    

    String[] storyFolderList = AssetUtils.listFolder(inputPath, true);
    
    for (String storyFolder : storyFolderList)
    {
      String srcPath = inputPath + "/" + storyFolder;
      
      File element = new File(srcPath);
      
      if (element.isDirectory())
      {

        if (hasStoryContent(srcPath))
        {
          Story_Data story = extractStoryData(srcPath);
          


          int levelIndex = Integer.parseInt(story.level);
          


          if (levelIndex != -1)
          {
            System.out.println("Migrating Level: " + levelIndex + "  - in Folder: " + storyFolder);
            Integer storyIndex;
            if (levelCount.containsKey(Integer.valueOf(levelIndex))) {
              storyIndex = (Integer)levelCount.get(Integer.valueOf(levelIndex));
            }
            else {
              storyIndex = Integer.valueOf(1);
            }
            
            Integer storyIndex = Integer.valueOf(storyIndex.intValue() + migrateStoryContentA(srcPath, outputPath, storyIndex, Integer.valueOf(levelIndex), language));
            
            levelCount.put(Integer.valueOf(levelIndex), storyIndex);
          }
        }
      }
    }
    
    saveLogString(this.masterLst.toString(), outputPath + "/migration_cross_ref.csv");
  }
  

  public void validateLevelSegmentation(String inputPath)
  {
    String[] storyList = AssetUtils.listFolder(inputPath, true);
    
    for (String storyFolder : storyList)
    {
      String srcPath = inputPath + "/" + storyFolder;
      
      Story_Data story = extractStoryData(srcPath);
      
      String name = story.story_name;
      String title = story.title;
      
      Story_Content[] pages = story.data;
      
      for (Story_Content page : pages)
      {
        Story_Audio[][] paragraphs = page.text;
        
        for (Story_Audio[] paragraph : paragraphs)
        {
          for (Story_Audio sentence : paragraph) {
            String logText;
            if (sentence.narration.length == 0)
            {
              logText = storyFolder + "|\t" + (name != null ? name : title) + " - no narration data for : " + sentence.sentence + "\n";
              this.masterLst.append(logText);
              System.out.println(logText);
            } else {
              for (Story_Narration utterance : sentence.narration) {
                String logText;
                if ((utterance.from == -1) || (utterance.until == -1))
                {

                  logText = storyFolder + "|\t" + (name != null ? name : title) + " - utterance bounds error : " + sentence.sentence + "\n";
                  this.masterLst.append(logText);
                  System.out.println(logText);
                } else {
                  for (Story_SegData segment : utterance.segmentation)
                  {
                    if ((segment.start == -1) || (segment.end == -1))
                    {

                      String logText = storyFolder + "|\t" + (name != null ? name : title) + " - segment bounds error : " + sentence.sentence + "\n";
                      this.masterLst.append(logText);
                      System.out.println(logText);
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  }
  
  public void validateSegmentation(String basePath, String language) {
    this.masterLst = new StringBuffer();
    
    String logText = "Segmentation Faults\n";
    this.masterLst.append(logText);
    
    String inputPath = basePath + "/assets/story/" + language;
    
    String[] levelList = AssetUtils.listFolder(inputPath, true);
    
    for (String levelFolder : levelList)
    {


      int levelIndex = Integer.parseInt(levelFolder);
      


      if (levelIndex != -1)
      {
        String srcPath = inputPath + "/" + levelFolder;
        
        File element = new File(srcPath);
        
        System.out.println("Validating Level: " + levelIndex + "  - in Folder: " + levelFolder);
        
        validateLevelSegmentation(srcPath);
      }
    }
    
    saveLogString(this.masterLst.toString(), basePath + "/validation_check.log");
  }
  







  private void generateStoryInitiator(int levelNdx, int storyNdx, String type, String feature)
  {
    String tag = levelNdx + "_" + storyNdx + ":" + type;
    
    ((ArrayList)this.listOfLevels.get(levelNdx - 1)).add(storyNdx - 1, tag);
    
    String jsonSeg = "\t\t\"" + tag + "\": {\n";
    this.masterLst.append(jsonSeg);
    
    jsonSeg = "\t\t\"type\": \"INITIATOR\",\n";
    this.masterLst.append(jsonSeg);
    
    jsonSeg = "\t\t\"datasource\": \"{\\\"scene_bindings\\\" : {\\\"story_reading\\\": {\\\"type\\\": \\\"SCENEDATA_MAP\\\", \\\"databindings\\\": [{\\\"name\\\": \\\"SstoryReading\\\",\\\"datasource\\\": \\\"[encfolder]" + levelNdx + "_" + storyNdx + ":" + feature + "\\\"}]}}}\",\n";
    this.masterLst.append(jsonSeg);
    
    jsonSeg = "\t\t\"intent\": \"story_reading\",\n";
    this.masterLst.append(jsonSeg);
    
    jsonSeg = "\t\t\"intentdata\": \"native\",\n";
    this.masterLst.append(jsonSeg);
    
    jsonSeg = "\t\t\"buttontype\": \"text\",\n";
    this.masterLst.append(jsonSeg);
    
    jsonSeg = "\t\t\"buttonvalue\": \"" + tag + "\"\n";
    this.masterLst.append(jsonSeg);
    
    jsonSeg = "\t\t},\n\n";
    this.masterLst.append(jsonSeg);
  }
  


  private void generateLevelInitiators(String inputPath, int levelNdx)
  {
    String[] storyList = AssetUtils.listFolder(inputPath, true);
    
    int storyCnt = storyList.length;
    int storyNdx = 1;
    
    this.levelMap.put(Integer.valueOf(levelNdx), Integer.valueOf(storyCnt));
    
    this.listOfLevels.add(levelNdx - 1, new ArrayList());
    
    for (String storyFolder : storyList)
    {
      generateStoryInitiator(levelNdx, storyNdx, "a.HEAR", "FTR_USER_HEAR");
      generateStoryInitiator(levelNdx, storyNdx, "a.READ", "FTR_USER_READ");
      storyNdx++;
    }
  }
  


  public void generateInitiatorTables(String basePath, String language)
  {
    this.masterLst = new StringBuffer();
    
    this.listOfLevels = new ArrayList();
    
    String jsonSeg = "\t\"storyInitiators\": {\n";
    this.masterLst.append(jsonSeg);
    
    this.levelMap = new HashMap();
    
    String inputPath = basePath + "/assets/story/" + language;
    
    String[] levelList = AssetUtils.listFolder(inputPath, true);
    
    for (String levelFolder : levelList)
    {


      int levelIndex = Integer.parseInt(levelFolder);
      


      if (levelIndex != -1)
      {
        String srcPath = inputPath + "/" + levelFolder;
        
        File element = new File(srcPath);
        
        System.out.println("Generating Initiators for level: " + levelIndex + "  - in Folder: " + levelFolder);
        
        generateLevelInitiators(srcPath, levelIndex);
      }
    }
    
    jsonSeg = "\t},\n";
    this.masterLst.append(jsonSeg);
    
    saveLogString(this.masterLst.toString(), basePath + "/storyInitiators.json");
  }
  



  public void generateTransition(int row, int col, String name, String next, String harder, String easier)
  {
    String cell_row = new Integer(row + 1).toString();
    String cell_col = new Integer(col + 1).toString();
    
    String jsonSeg = "\t\t\"" + name + "\": {\n";
    this.masterLst.append(jsonSeg);
    
    jsonSeg = "\t\t\"type\": \"TRANSITION\",\n";
    this.masterLst.append(jsonSeg);
    
    jsonSeg = "\t\t\"skill\": \"stories\",\n";
    this.masterLst.append(jsonSeg);
    
    jsonSeg = "\t\t\"tutor_id\": \"" + name + "\",\n";
    this.masterLst.append(jsonSeg);
    
    jsonSeg = "\t\t\"cell_row\": \"" + cell_row + "\",\n";
    this.masterLst.append(jsonSeg);
    
    jsonSeg = "\t\t\"cell_column\": \"" + cell_col + "\",\n";
    this.masterLst.append(jsonSeg);
    
    jsonSeg = "\t\t\"same\": \"" + name + "\",\n";
    this.masterLst.append(jsonSeg);
    
    jsonSeg = "\t\t\"next\": \"" + next + "\",\n";
    this.masterLst.append(jsonSeg);
    
    jsonSeg = "\t\t\"harder\": \"" + harder + "\",\n";
    this.masterLst.append(jsonSeg);
    
    jsonSeg = "\t\t\"easier\": \"" + easier + "\"},\n\n";
    this.masterLst.append(jsonSeg);
  }
  







  public void generateTransitionTables(String basePath, String language)
  {
    System.out.println("Generating Tables for level: " + basePath + "  - Language: " + language);
    
    this.masterLst = new StringBuffer();
    
    String jsonSeg = "\t\"storyTransitions\": {\n";
    this.masterLst.append(jsonSeg);
    
    String inputPath = basePath + "/assets/story/" + language;
    
    int levelCnt = this.levelMap.size();
    
    for (int i1 = 0; i1 < levelCnt; i1++)
    {
      ArrayList<String> listOfStories = (ArrayList)this.listOfLevels.get(i1);
      
      int storyCnt = listOfStories.size();
      
      for (int i2 = 0; i2 < storyCnt; i2++)
      {
        String name = (String)listOfStories.get(i2);
        
        String harder;
        String harder;
        if (i1 < levelCnt - 1) {
          ArrayList<String> harderStories = (ArrayList)this.listOfLevels.get(i1 + 1);
          
          harder = (String)harderStories.get(1);
        }
        else {
          harder = (String)listOfStories.get(1);
        }
        
        String easier;
        
        String easier;
        if (i1 > 0) {
          ArrayList<String> easierStories = (ArrayList)this.listOfLevels.get(i1 - 1);
          
          easier = (String)easierStories.get(1);
        }
        else {
          easier = (String)listOfStories.get(0);
        }
        
        String next;
        
        String next;
        if (i2 < storyCnt - 1) {
          next = (String)listOfStories.get(i2 + 1);
        }
        else {
          next = harder;
        }
        
        generateTransition(i1, i2, name, next, harder, easier);
      }
    }
    
    jsonSeg = "\t},\n";
    this.masterLst.append(jsonSeg);
    
    saveLogString(this.masterLst.toString(), basePath + "/storyTransitions.json");
  }
  

  public void generateTables(String basePath, String language)
  {
    generateInitiatorTables(basePath, language);
    generateTransitionTables(basePath, language);
  }
  


  protected void copyFile(String inputPath, String outputPath)
  {
    OutputStream out = null;
    InputStream in = null;
    
    byte[] buffer = new byte['Ѐ'];
    


    try
    {
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
        in = null;
        out = null;
      }
    }
    catch (Exception e) {
      System.out.println("INFO: File Copy Failed: " + inputPath + " - reason: " + e);
    }
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
  






  public void extractStoryIndices(String inputPath, String saveName)
  {
    FileWriter out = null;
    
    this.stories = new ArrayList();
    this.folderList = AssetUtils.listFolder(inputPath, true);
    
    for (String levelFolder : this.folderList)
    {
      String srcPath = inputPath + "\\" + levelFolder;
      
      File element = new File(srcPath);
      
      if (element.isDirectory())
      {
        System.out.println("Indexing Story Level: " + levelFolder);
        
        extractLevelStories(srcPath, levelFolder);
      }
    }
    
    if (this.stories.size() > 0)
    {
      JSON_Util storyList = new JSON_Util();
      
      storyList.startArray("dataSource");
      
      for (Story_Index index : this.stories) {
        storyList.addObject(index);
      }
      storyList.endArray();
      
      String jsonStories = storyList.close();
      
      System.out.println(jsonStories);
      

      try
      {
        String savePath = inputPath + "\\" + saveName;
        
        out = new FileWriter(savePath, false);
        


        out.write(jsonStories);
        out.close();
        
        System.out.println("Story Index Serialized.");
      }
      catch (Exception e)
      {
        System.out.println("Story Index Serialization Error: " + e);
      }
    }
  }
  







  public void extractLevelStories(String inputPath, String levelFolder)
  {
    this.folderList = AssetUtils.listFolder(inputPath, true);
    
    for (String storyFolder : this.folderList)
    {
      String srcPath = inputPath + "\\" + storyFolder;
      
      File element = new File(srcPath);
      
      if (element.isDirectory())
      {
        Story_Index index = extractStoryIndex(srcPath, levelFolder, storyFolder);
        
        if (index != null)
        {
          System.out.println("Indexing Story Folder: " + storyFolder);
          
          this.stories.add(index);
        }
      }
    }
  }
  


  public Story_Data extractStoryData(String inputPath)
  {
    String jsonPath = inputPath + "/storydata.json";
    Story_Data story = null;
    
    File storyFile = new File(jsonPath);
    
    if (storyFile.exists()) {
      try
      {
        story = new Story_Data();
        
        story.loadStoryDataFactory(jsonPath);
      }
      catch (Exception e) {
        System.out.println("Error Parsing: " + jsonPath);
        story = null;
      }
    }
    



    return story;
  }
  

  public Story_Index extractStoryIndex(String inputPath, String levelFolder, String storyFolder)
  {
    String jsonPath = inputPath + "\\storydata.json";
    Story_Data story = null;
    Story_Index index = null;
    
    File storyFile = new File(jsonPath);
    
    if (storyFile.exists()) {
      try
      {
        story = new Story_Data();
        
        story.loadStoryDataFactory(jsonPath);
        
        index = new Story_Index();
        


        if (story.story_name != null) {
          index.storyName = story.story_name;
        }
        else if (story.title != null) {
          index.storyName = story.title;
        }
        index.levelFolder = levelFolder;
        index.storyFolder = storyFolder;
        index.viewtype = "ASB_Data";
      }
      catch (Exception e) {
        System.out.println("Error Parsing: " + jsonPath);
        story = null;
      }
    }
    



    return index;
  }
  

  public void extractAudio(String inputPath, String langFtr)
  {
    FileWriter out = null;
    
    this.folderList = AssetUtils.listFolder(inputPath, true);
    
    this.wordMap = new HashMap();
    this.wordDups = 0;
    
    this.lineMap = new HashMap();
    this.lineDups = 0;
    


    if (langFtr.equals("LANG_SW")) {
      for (String objectname : this.folderList)
      {
        String srcPath = inputPath + "\\" + objectname;
        
        File element = new File(srcPath);
        
        if (element.isDirectory())
        {
          System.out.println("Processing Subfolder: " + objectname);
          
          extractUtterances(inputPath + "\\" + objectname, langFtr);
        }
      }
    }
    else
    {
      System.out.println("Processing Flat folder: " + inputPath);
      
      extractUtterances(inputPath, langFtr);
    }
    

    JSON_Util wordList = new JSON_Util();
    
    wordList.addElement("language", langFtr);
    wordList.startArray("utterance");
    
    Object tObjects = this.wordMap.entrySet().iterator();
    
    while (((Iterator)tObjects).hasNext()) {
      Map.Entry entry = (Map.Entry)((Iterator)tObjects).next();
      
      wordList.addArray((String)entry.getValue());
    }
    wordList.endArray();
    
    String wordJSON = wordList.close();
    

    try
    {
      String savePath = inputPath + "\\wordlist.json";
      
      out = new FileWriter(savePath, false);
      


      out.write(wordJSON);
      out.close();
      
      System.out.println("Story word list Serialized.");
    }
    catch (Exception e)
    {
      System.out.println("Story word list Serialization Error: " + e);
    }
    

    System.out.println("WordMap: Dups: " + this.wordDups + " -- Size: " + this.wordMap.size());
    System.out.println(wordJSON);
    



    JSON_Util lineList = new JSON_Util();
    
    lineList.addElement("language", langFtr);
    lineList.startArray("utterance");
    
    tObjects = this.lineMap.entrySet().iterator();
    
    while (((Iterator)tObjects).hasNext()) {
      Map.Entry entry = (Map.Entry)((Iterator)tObjects).next();
      
      lineList.addArray((String)entry.getValue());
    }
    lineList.endArray();
    
    String lineJSON = lineList.close();
    

    try
    {
      String savePath = inputPath + "\\linelist.json";
      
      out = new FileWriter(savePath, false);
      


      out.write(lineJSON);
      out.close();
      
      System.out.println("Story line list Serialized.");
    }
    catch (Exception e)
    {
      System.out.println("Story line list Serialization Error: " + e);
    }
    

    System.out.println("LineMap: Dups: " + this.lineDups + " -- Size: " + this.lineMap.size());
    System.out.println(lineJSON);
  }
  


  public void extractUtterances(String inputPath, String langFtr)
  {
    this.folderList = AssetUtils.listFolder(inputPath, true);
    
    for (String objectname : this.folderList)
    {
      System.out.println("Processing: " + objectname);
      
      String srcPath = inputPath + "\\" + objectname;
      
      File element = new File(srcPath);
      
      if (element.isDirectory())
      {
        String jsonPath = srcPath + "\\storydata.json";
        
        Story_Data story = new Story_Data();
        
        File storyFile = new File(jsonPath);
        
        if (storyFile.exists()) {
          story.loadStoryDataFactory(jsonPath);
          
          if (story.story_name != null) {
            System.out.println("Start - Name: " + story.story_name);
          } else if (story.title != null) {
            System.out.println("Start - Title: " + story.title);
          }
          int pageNum = 1;
          
          for (Story_Content page : story.data)
          {
            int paraNum = 1;
            
            for (Story_Audio[] paragraph : page.text)
            {
              int sentNum = 1;
              
              for (Story_Audio sentence : paragraph)
              {
                System.out.println("PAGE:" + pageNum + "PARA:" + paraNum + "SENT:" + sentNum + " -->|" + sentence);
                sentNum++;
                


                String[] words = sentence.sentence.toLowerCase().split("\\s*\"*'*,*;*\\s*[^a-zA-Z0-9's]");
                




                for (int i1 = 0; i1 < words.length; i1++)
                {
                  if (words[i1].equals("")) {
                    words[i1] = "a";
                  }
                  else if (words[i1].equals("'")) {
                    words[i1] = "a";
                  }
                  else if (words[i1].startsWith("'")) {
                    words[i1] = words[i1].substring(1);
                  }
                }
                
                String wordset = String.join(" | ", words);
                System.out.println("WordSet:" + wordset);
                
                String hashline = AssetUtils.generateStrippedHash(sentence.sentence);
                if (this.lineMap.put(hashline, sentence.sentence) != null) {
                  this.lineDups += 1;
                }
                
                for (String word : words)
                {
                  String hashword = AssetUtils.generateCaseNormalHash(word);
                  if (this.wordMap.put(hashword, word) != null) {
                    this.wordDups += 1;
                  }
                }
              }
              paraNum++;
            }
            pageNum++;
          }
          System.out.println("END: ");
        }
        else {
          System.out.println("ERROR: ");
        }
      }
    }
  }
  

  public static void zipExtractAll(String zipName, String outputFolder)
  {
    byte[] buffer = new byte['Ѐ'];
    


    try
    {
      File folder = new File(outputFolder);
      
      if (!folder.exists()) {
        folder.mkdir();
      }
      

      ZipInputStream zis = new ZipInputStream(new FileInputStream(zipName));
      


      ZipEntry ze = zis.getNextEntry();
      
      while (ze != null)
      {
        String fileName = outputFolder + File.separator + ze.getName();
        File newFile = new File(fileName);
        
        System.out.println("file unzip : " + newFile.getAbsoluteFile());
        
        if (ze.isDirectory()) {
          newFile.mkdirs();

        }
        else
        {
          new File(newFile.getParent()).mkdirs();
          
          FileOutputStream fos = new FileOutputStream(newFile);
          
          int len;
          while ((len = zis.read(buffer)) > 0) {
            fos.write(buffer, 0, len);
          }
          
          fos.close();
        }
        ze = zis.getNextEntry();
      }
      
      zis.closeEntry();
      zis.close();
      
      System.out.println("Done");
    }
    catch (IOException e) {
      System.out.println("Error: " + e);
    }
  }
  

  public static void buildEmptyEncodedTree(String basePath, String prefix, int count, boolean recurse)
  {
    String subFolder = basePath + File.separator;
    



    File folder = new File(basePath);
    
    if (!folder.exists()) {
      folder.mkdir();
    }
    
    for (int n = 1; n <= count; n++)
    {
      String newFolder = subFolder + prefix + n;
      


      folder = new File(newFolder);
      
      if (!folder.exists()) {
        folder.mkdir();
      }
      
      if (recurse)
      {
        buildEmptyEncodedTree(newFolder, n + "_", 99, false);
      }
    }
  }
  



  public void compareWordLists(String inputPath)
  {
    FileWriter out = null;
    
    int wordDups = 0;
    int missingCnt = 0;
    
    HashMap<String, String> canonMap = new HashMap();
    
    String listPath = inputPath + "/wordlist.json";
    String canonPath = inputPath + "/canonlist.json";
    String origPath = inputPath + "/origlist.json";
    
    CAudio_Data wordList = new CAudio_Data();
    CAudio_Data canonList = new CAudio_Data();
    CAudio_Data origList = new CAudio_Data();
    
    wordList.loadDataFactory(listPath);
    canonList.loadDataFactory(canonPath);
    origList.loadDataFactory(origPath);
    


    for (String word : canonList.utterance)
    {
      word = word.toLowerCase();
      
      if (canonMap.containsKey(word)) {
        System.out.println("ERROR: Canon contains duplicate: " + word);
        wordDups++;
      }
      else {
        canonMap.put(word, word);
      }
    }
    


    for (String word : wordList.utterance)
    {
      word = word.toLowerCase();
      
      if (!canonMap.containsKey(word)) {
        System.out.println("Adding missing Canon word: " + word);
        
        missingCnt++;
        canonMap.put(word, word);
      }
    }
    


    for (String word : origList.utterance)
    {
      word = word.toLowerCase();
      
      if (!canonMap.containsKey(word)) {
        System.out.println("Adding missing original Canon word: " + word);
        
        missingCnt++;
        canonMap.put(word, word);
      }
    }
    


    JSON_Util wordlistJSON = new JSON_Util();
    
    wordlistJSON.addElement("language", wordList.language);
    wordlistJSON.startArray("utterance");
    
    Object tObjects = canonMap.entrySet().iterator();
    
    while (((Iterator)tObjects).hasNext()) {
      Map.Entry entry = (Map.Entry)((Iterator)tObjects).next();
      
      wordlistJSON.addArray((String)entry.getValue());
    }
    wordlistJSON.endArray();
    
    String wordJSON = wordlistJSON.close();
    

    try
    {
      String savePath = inputPath + "\\canonlist.json";
      
      out = new FileWriter(savePath, false);
      


      out.write(wordJSON);
      out.close();
      
      System.out.println("Canon word list Serialized.");
    }
    catch (Exception e)
    {
      System.out.println("Canon word list Serialization Error: " + e);
    }
    
    System.out.println("Canon: Dups: " + wordDups + " -- Missing: " + missingCnt + " -- Size: " + canonMap.size());
    System.out.println(wordJSON);
  }
}
