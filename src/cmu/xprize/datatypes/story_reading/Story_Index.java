/*    */ package cmu.xprize.datatypes.story_reading;
/*    */ 
/*    */ import org.json.JSONObject;
/*    */ import cmu.xprize.CClassMap;
/*    */ import cmu.xprize.ILoadableObject;
/*    */ import cmu.xprize.IScope;
/*    */ import util.JSON_Helper;
/*    */ import cmu.xprize.JSON_Util;
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ public class Story_Index
/*    */   implements ILoadableObject
/*    */ {
/*    */   public String storyName;
/*    */   public String levelFolder;
/*    */   public String storyFolder;
/*    */   public String viewtype;
/*    */   
/*    */   public void loadJSON(JSONObject jsonObj, IScope scope)
/*    */   {
/* 57 */     JSON_Helper.parseSelf(jsonObj, this, CClassMap.classMap, scope);
/*    */   }
/*    */   
/*    */ 
/*    */   public void toJSON(JSON_Util writer)
/*    */   {
/* 63 */     writer.addElement("storyName", this.storyName);
/* 64 */     writer.addElement("levelFolder", this.levelFolder);
/* 65 */     writer.addElement("storyFolder", this.storyFolder);
/* 66 */     writer.addElement("viewtype", this.viewtype);
/*    */   }
/*    */ }


/* Location:              /Users/kevindeland/Desktop/RTAssetManager.jar!/RTAssetManager/datatypes/story_reading/Story_Index.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */