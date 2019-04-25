package cmu.xprize;

 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.nio.file.Files;
 import java.nio.file.Paths;
 import java.util.HashMap;
 import java.util.Iterator;


 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;



 public class AssetManager {
   static boolean debug = false;
   static boolean compress = true;

   static Boolean RECURSE = Boolean.valueOf(true);
   static String BASEFOLDER;
   static String LOG_FOLDER;
   static String ROOTASSETFOLDER = "sdcard/robotutor_assets";

   static String[] RTIGNORE = {".git"};


   static String[] ZIPIGNORE = {".git", "migration_cross_ref.en.csv", "migration_cross_ref.sw.csv", "validation_check.log", "storyInitiators.json", "storyTransitions.json"};


   public static void main(String[] args) {
     AssetUtils assetUtils = new AssetUtils();
     StoryManager storyUtils = new StoryManager();

     LOG_FOLDER = BASEFOLDER = System.getProperty("user.dir");

     System.out.println("Working Directory = " + BASEFOLDER);

     String[] parmArray = args[0].split(":");

     HashMap<String, String> vars = new HashMap();

     for (String parm : parmArray) {
       String[] keyvalue = parm.split("=");

       vars.put(keyvalue[0], keyvalue[1]);
     }

     Object tObjects = vars.entrySet().iterator();

     while (((Iterator) tObjects).hasNext()) {
       Map.Entry entry = (Map.Entry) ((Iterator) tObjects).next();

       System.out.println("Java Argument: Key-> " + entry.getKey() + "=" + entry.getValue());
     }

     // only using cmd, src, and compress

     String commmand = vars.get("cmd") != null ? (String) vars.get("cmd") : "noop";
     Boolean debug = Boolean.valueOf(vars.get("debug") != null ? Boolean.parseBoolean((String) vars.get("debug")) : false);
     Boolean clean = Boolean.valueOf(vars.get("clean") != null ? Boolean.parseBoolean((String) vars.get("clean")) : false);
     Boolean compress = Boolean.valueOf(vars.get("compress") != null ? Boolean.parseBoolean((String) vars.get("compress")) : false);
     String quality = vars.get("quality") != null ? (String) vars.get("quality") : "low";
     String language = vars.get("language") != null ? (String) vars.get("language") : "en";
     String SRCFOLDER = vars.get("src") != null ? (String) vars.get("src") : "";
     String DSTFOLDER = vars.get("dst") != null ? (String) vars.get("dst") : "";
     String DATFOLDER = vars.get("dat") != null ? (String) vars.get("dat") : "";
     String indexPath = vars.get("index") != null ? (String) vars.get("index") : "";


     AssetObject targetAsset = new AssetObject(SRCFOLDER);

     AudioHasher assetInstaller;
     if (compress.booleanValue()) {
       assetInstaller = new AudioZipHasher();
     } else {
       assetInstaller = new AudioHasher();
     }

     if (vars.get("cmd") != null) {
       System.out.println("Running: " + args[0]);
       Runtime rt;
       InputStream stdout;
       switch ((String) vars.get("cmd")) {

         case "build_dist":
           assetInstaller.setQuality(quality);

           String AssetFile = SRCFOLDER + ".json";
           String IndexFile = SRCFOLDER + ".json";


           if (debug.booleanValue()) {
             SRCFOLDER = "asset_test" + File.separator + SRCFOLDER;
             AssetFile = SRCFOLDER + ".json";

             targetAsset.loadAssetFactory(BASEFOLDER + File.separator + AssetFile);

             DSTFOLDER = targetAsset.getVersionedName();

             DSTFOLDER = "asset_test" + File.separator + DSTFOLDER;
             LOG_FOLDER = LOG_FOLDER + File.separator + "asset_test";
           } else {
             AssetFile = SRCFOLDER + ".json";

             targetAsset.loadAssetFactory(BASEFOLDER + File.separator + AssetFile);

             DSTFOLDER = targetAsset.getVersionedName();
           }

           if (compress.booleanValue()) {
             DSTFOLDER = DSTFOLDER + ".zip";
           }
           if (clean.booleanValue()) {
             if (compress.booleanValue()) {
               System.out.println("Running: Zip-Clean");
               try {
                 String zipPath = BASEFOLDER + File.separator + DSTFOLDER;

                 File zipFile = new File(zipPath);

                 if (zipFile.exists()) {
                   Files.delete(Paths.get(zipPath, new String[0]));
                 }
               } catch (IOException ex) {
                 Logger.getLogger(AssetManager.class.getName()).log(Level.SEVERE, null, ex);
               }
             } else {
               System.out.println("Running: Clean");
               assetUtils.cleanFolder(BASEFOLDER + File.separator + DSTFOLDER, false);
             }
           }


           if (!indexPath.equals("")) {
             storyUtils.extractStoryIndices(BASEFOLDER + File.separator + SRCFOLDER + File.separator + indexPath, IndexFile);
           }


           assetInstaller.storeAssets(BASEFOLDER + File.separator + SRCFOLDER, BASEFOLDER + File.separator + DSTFOLDER, RECURSE);


           targetAsset.saveAssetHistory(BASEFOLDER + File.separator + AssetFile);


           if (!compress.booleanValue()) {


             assetInstaller.createFolderAsset(ROOTASSETFOLDER);

             assetInstaller.createFolderAssets(BASEFOLDER + File.separator + DSTFOLDER, ROOTASSETFOLDER, RECURSE);

             rt = Runtime.getRuntime();
             try {
               System.out.println("\nExecuting ADB Push: adb push " + DSTFOLDER + "/assets sdcard/robotutor_assets");
               Process pr = rt.exec("adb push " + DSTFOLDER + "/assets sdcard/robotutor_assets");

               stdout = pr.getInputStream();

               BufferedReader reader = new BufferedReader(new InputStreamReader(stdout));

               String line;

               while ((line = reader.readLine()) != null) {
                 System.out.print("Stdout: " + line + "\r");
               }
               try {
                 pr.waitFor();
               } catch (InterruptedException ex) {
                 Logger.getLogger(AssetManager.class.getName()).log(Level.SEVERE, null, ex);
               }

               System.out.println("\nPush Complete");
             } catch (IOException ex) {
               Logger.getLogger(AssetManager.class.getName()).log(Level.SEVERE, null, ex);
             }
           }
           break;


       }
     }
   }
 }
