package cmu.xprize;

import java.io.File;
import java.io.FileWriter;
import org.json.JSONException;
import org.json.JSONObject;


public class AssetObject
  implements ILoadableObject
{
  private String assetName;
  private String versionPath;
  public String name = "";
  public String versionName = "";
  public int currentVersion = 1;
  public int currentRelease = 1;
  public int currentUpdate = 0;
  
  static final String VERSEP = ".";
  
  public AssetObject(String name)
  {
    this.assetName = name;
    this.versionPath = (this.assetName + "." + this.currentVersion + "." + this.currentRelease + "." + this.currentUpdate); // TODO change this annoying 1.1.0
    // XXX e.g. English_TutorAudio.1.1.0
  }
  
  public String getVersionedName() {
    return this.versionPath;
  }
  
  public String getName() {
    return this.assetName;
  }


  /**
   * Load the asset information from an existing JSON file.
   * @param asset_path
   */
  public void loadAssetFactory(String asset_path)
  {
    try
    {
      File assetFile = new File(this.versionPath); // XXX e.g. English_TutorAudio.1.1.0
      
      if (assetFile.exists())
      {
        loadJSON(new JSONObject(JSON_Helper.cacheData(asset_path, "DEFINED")), null); // loads existing
      }
    }
    catch (JSONException e) {
      System.out.println("Error: " + e);
    }
  }
  


  public void loadJSON(JSONObject jsonObj, IScope scope)
  {
    JSON_Helper.parseSelf(jsonObj, this, CClassMap.classMap, scope);
  }
  

  public void toJSON(JSON_Util writer) {}


  /**
   * Write the asset information to a JSON file.
   * @param saveName
   */
  public void saveAssetHistory(String saveName)
  {
    FileWriter out = null;
    
    JSON_Util asset = new JSON_Util();
    
    asset.addElement("name", this.assetName);
    asset.addElement("versionPath", this.versionPath);
    asset.addElement("currentVersion", this.currentVersion);
    asset.addElement("currentRelease", this.currentRelease);
    asset.addElement("currentUpdate", this.currentUpdate);
    
    String json = asset.close();
    
    System.out.println(json);
    

    try
    {
      out = new FileWriter(saveName, false);
      


      out.write(json);
      out.close();
      
      System.out.println("Asset History Serialized.");
    }
    catch (Exception e)
    {
      System.out.println("Asset History Serialization Error: " + e);
    }
  }
}
