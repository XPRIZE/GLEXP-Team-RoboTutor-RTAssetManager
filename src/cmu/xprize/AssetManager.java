package cmu.xprize;

 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
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

     String commmand = vars.get("cmd") != null ? vars.get("cmd") : "noop"; // always build_dist
     Boolean compress = vars.get("compress") != null && Boolean.parseBoolean(vars.get("compress")); // true or false
     String quality = vars.get("quality") != null ? vars.get("quality") : "low"; // always low
     String SRCFOLDER = vars.get("src") != null ? vars.get("src") : "";
     String DSTFOLDER = vars.get("dst") != null ? vars.get("dst") : "";


     AssetObject targetAsset = new AssetObject(SRCFOLDER);

     AudioHasher assetInstaller;
     if (compress) {
       assetInstaller = new AudioZipHasher();
     } else {
       assetInstaller = new AudioHasher();
     }

     if (vars.get("cmd") != null) {
       System.out.println("Running: " + args[0]);
       Runtime rt;
       InputStream stdout;
       switch (vars.get("cmd")) {

         case "build_dist":
           assetInstaller.setQuality(quality);

           String AssetFile;

           AssetFile = SRCFOLDER + ".json";
           //targetAsset.loadAssetFactory(BASEFOLDER + File.separator + AssetFile); // XXX e.g. <dir>/English_TutorAssets.1.1.0.json
           // I don't think we need this...

           DSTFOLDER = targetAsset.getVersionedName();
           // XXX e.g. English_TutorAssets.1.1.0/

           if (compress) {
             DSTFOLDER = DSTFOLDER + ".zip";
             // XXX e.g. English_TutorAssets.1.1.0.zip
           }


           assetInstaller.storeAssets(BASEFOLDER + File.separator + SRCFOLDER, BASEFOLDER + File.separator + DSTFOLDER, RECURSE);
           // copies from SRCFOLDER to DSTFOLDER


           //targetAsset.saveAssetHistory(BASEFOLDER + File.separator + AssetFile);


           // If not compress, then push onto tablet.
           if (!compress) {

             // create a folder on tablet
             assetInstaller.createFolderAsset(ROOTASSETFOLDER);

             assetInstaller.createFolderAssets(BASEFOLDER + File.separator + DSTFOLDER, ROOTASSETFOLDER, RECURSE);

             rt = Runtime.getRuntime();
             try {
               System.out.println("\nExecuting ADB Push: adb push " + DSTFOLDER + "/assets /sdcard/robotutor_assets");
               Process pr = rt.exec("adb push " + DSTFOLDER + "/assets /sdcard/robotutor_assets");

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

                 System.err.println("\nInterruptedException");
                 System.err.println(ex.getMessage());
                 ex.printStackTrace(System.err);
               }

               System.out.println("\nPush Complete");
             } catch (IOException ex) {
               Logger.getLogger(AssetManager.class.getName()).log(Level.SEVERE, null, ex);

               System.err.println("\nIOException");
               System.err.println(ex.getMessage());
               ex.printStackTrace(System.err);
             }
           }
           break;


       }
     }
   }
 }
