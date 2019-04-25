package cmu.xprize;

import RTAssetManager.datatypes.bubblepop.CBp_DataA;
import RTAssetManager.datatypes.tutor_data.defdata_scenes;
import RTAssetManager.datatypes.tutor_data.defdata_tutor;
import RTAssetManager.datatypes.tutor_data.defvar_tutor;
import java.util.HashMap;


public class CClassMap
{
  public static HashMap<String, Class> classMap = new HashMap();
  


  static
  {
    classMap.put("INITIATOR", CAs_Data.class);
    classMap.put("TRANSITION", CAt_Data.class);
    
    classMap.put("TUTORDATA_MAP", defdata_tutor.class);
    classMap.put("TUTORVAR_MAP", defvar_tutor.class);
    classMap.put("SCENEDATA_MAP", defdata_scenes.class);
    
    classMap.put("STRING_ARRAY", String[].class);
    
    classMap.put("string", String.class);
    classMap.put("bool", Boolean.class);
    classMap.put("int", Integer.class);
    classMap.put("float", Float.class);
    classMap.put("byte", Byte.class);
    classMap.put("long", Long.class);
    classMap.put("short", Short.class);
    classMap.put("object", Object.class);
    
    classMap.put("Bp_Data", CBp_DataA.class);
  }
}
