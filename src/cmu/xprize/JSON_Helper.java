package cmu.xprize;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Iterator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cmu.xprize.IScope;


public class JSON_Helper
{
  public static String _cacheSource;
  public static String _externFiles;
  static final boolean DBG = false;
  private static final String TAG = "JSON_HELPER";
  
  public JSON_Helper(String cs, String ef)
  {
    set_cacheSource(cs);
    set_externFiles(ef);
  }
  
  public static void set_cacheSource(String cs) { _cacheSource = cs; }
  public static void set_externFiles(String ef) { _externFiles = ef; }
  
  public static String cacheData(String fileName)
  {
    return cacheData(fileName, _cacheSource);
  }
  
  public static String cacheDataByName(String fileName)
  {
    return cacheData(fileName, "DEFINED");
  }
  

  public static String cacheData(String fileName, String localcacheSource)
  {
    InputStream in = null;
    
    StringBuilder buffer = new StringBuilder();
    byte[] databuffer = new byte['Ѐ'];
    





    try
    {
      switch (localcacheSource)
      {
      case "EXTERN": 
        String filePath = _externFiles + "/" + fileName;
        
        in = new FileInputStream(filePath);
        break;
      
      case "DEFINED": 
        in = new FileInputStream(fileName);
      }
      
      
      BufferedReader br = new BufferedReader(new InputStreamReader(in));
      String line = null;
      


      while ((line = br.readLine()) != null)
      {


        if ((line.contains("image\":")) && 
          (line.contains("null"))) {
          line = line.replace("null", "\"\"");
        }
        


        line = line.replaceFirst("//.*$", "");
        
        buffer.append(line);
      }
      in.close();
    }
    catch (FileNotFoundException e) {
      System.out.println("JSON_HELPER - ERROR: " + e);
    }
    catch (IOException e) {
      System.out.println("JSON_HELPER - ERROR: " + e);
    }
    
    return buffer.toString();
  }
  




















  public static void parseSelf(JSONObject jsonObj, Object self, HashMap<String, Class> classMap, IScope scope)
  {
    Class tClass = self.getClass();
    


    Field[] fields = tClass.getFields();
    






    for (Field field : fields)
    {
      Class<?> fieldClass = field.getType();
      Class<?> fieldTyoe = field.getClass();
      String className = fieldClass.toString();
      String fieldName = field.getName();
      Object field_obj = null;
      
      JSONObject nJsonObj = null;
      




      try
      {
        if (jsonObj.has(fieldName))
        {

          if (fieldClass.equals(String.class))
          {
            try {
              field.set(self, jsonObj.getString(fieldName));
            }
            catch (JSONException e) {
              CErrorManager.logEvent("JSON_HELPER", "field conversion:", e, false);
            }
            

          }
          else if (fieldClass.equals(Boolean.class))
          {
            try {
              field.set(self, Boolean.valueOf(jsonObj.getBoolean(fieldName)));
            }
            catch (JSONException e) {
              CErrorManager.logEvent("JSON_HELPER", "field conversion:", e, false);
            }
            

          }
          else if (className.equals("float"))
          {
            try {
              field.setFloat(self, Float.parseFloat(jsonObj.getString(fieldName)));
            }
            catch (JSONException e) {
              CErrorManager.logEvent("JSON_HELPER", "field conversion:", e, false);
            }
            

          }
          else if (className.equals("boolean"))
          {
            try {
              field.set(self, Boolean.valueOf(jsonObj.getBoolean(fieldName)));
            }
            catch (JSONException e) {
              CErrorManager.logEvent("JSON_HELPER", "field conversion:", e, false);

            }
            

          }
          else if (className.equals("long"))
          {
            try {
              field.set(self, Long.valueOf(jsonObj.getLong(fieldName)));
            }
            catch (JSONException e) {
              CErrorManager.logEvent("JSON_HELPER", "field conversion:", e, false);

            }
            

          }
          else if (className.equals("int")) {
            try {
              field.set(self, Integer.valueOf(jsonObj.getInt(fieldName)));
            }
            catch (JSONException e) {
              CErrorManager.logEvent("JSON_HELPER", "field conversion:", e, false);

            }
            

          }
          else if (fieldClass.equals(HashMap.class))
          {
            nJsonObj = jsonObj.getJSONObject(fieldName);
            
            HashMap<String, Object> field_Map = new HashMap();
            
            field.set(self, field_Map);
            
            Iterator<?> keys = nJsonObj.keys();
            










            Class<?> elemClass = null;
            String classType = null;
            boolean globalType = false;
            boolean isPrimitive = false;
            
            if (nJsonObj.has("type")) {
              try {
                classType = nJsonObj.getString("type");
                elemClass = (Class)classMap.get(classType);
                isPrimitive = !elemClass.isPrimitive();
                globalType = true;
              }
              catch (Exception e) {
                CErrorManager.logEvent("JSON_HELPER", "ERROR: no ClassMap defined for: " + classType + "  >> ", e, true);
              }
            }
            
            while (keys.hasNext())
            {
              String key = (String)keys.next();
              Object eObj = null;
              






              if ((!key.equals("COMMENT")) && (!key.equals("type")))
              {




                JSONObject elem = null;
                
                if (isPrimitive) {
                  if (elemClass.isArray())
                  {







                    JSONArray nArr = nJsonObj.getJSONArray(key);
                    
                    Class<?> compClass = elemClass.getComponentType();
                    
                    eObj = Array.newInstance(compClass, nArr.length());
                    
                    parseArray(nJsonObj, self, classMap, scope, nArr, compClass, eObj);
                    
                    field_Map.put(key, eObj);
                  }
                }
                else {
                  elem = nJsonObj.getJSONObject(key);
                  













                  if (elem.has("maptype"))
                  {




                    JSONObject nJsonMap = jsonObj.getJSONObject(elem.getString("maptype"));
                    JSONObject mapElem = nJsonMap.getJSONObject(elem.getString("mapname"));
                    


                    elemClass = (Class)classMap.get(mapElem.getString("type"));
                    


                    try
                    {
                      eObj = elemClass.newInstance();
                    }
                    catch (Exception e) {
                      CErrorManager.logEvent("JSON_HELPER", "ClassMap missing Element or Default Constructor: " + mapElem.getString("type") + " : ", e, false);
                    }
                    

                    ((ILoadableObject)eObj).loadJSON(mapElem, scope);
                  }
                  else
                  {
                    try {
                      if (!globalType) {
                        elemClass = (Class)classMap.get(elem.getString("type"));
                      }
                      


                      try
                      {
                        eObj = elemClass.newInstance();
                      }
                      catch (Exception e) {
                        CErrorManager.logEvent("JSON_HELPER", "ClassMap missing Element or Default Constructor: " + elem.getString("type") + " : ", e, false);
                      }
                    }
                    catch (Exception e) {
                      CErrorManager.logEvent("JSON_HELPER", "Check Syntax on Element: " + key + " : ", e, false);
                    }
                  }
                  




                  ((ILoadableObject)eObj).loadJSON(elem, scope);
                  



                  if (((eObj instanceof IScriptable)) && (!elem.has("novar")))
                  {


                    ((IScriptable)eObj).setName(key);
                    



                    if (scope != null) {
                      scope.put(key, (IScriptable)eObj);
                      System.out.println("JSON_HELPER - Adding to scope: " + key);
                    }
                  } else {
                    field_Map.put(key, eObj);

                  }
                  
                }
                
              }
              
            }
            

          }
          else if (fieldClass.isArray())
          {

            JSONArray nArr = jsonObj.getJSONArray(fieldName);
            
            Class<?> elemClass = fieldClass.getComponentType();
            
            Object field_Array = Array.newInstance(elemClass, nArr.length());
            
            field.set(self, parseArray(jsonObj, self, classMap, scope, nArr, elemClass, field_Array));

          }
          else
          {

            try
            {

              field_obj = fieldClass.newInstance();
              
              nJsonObj = jsonObj.getJSONObject(fieldName);
              
              ((ILoadableObject)field_obj).loadJSON(nJsonObj, scope);
              
              field.set(self, field_obj);

            }
            catch (JSONException localJSONException1) {}
          }
        }
      }
      catch (Exception e)
      {
        CErrorManager.logEvent("JSON_HELPER", "ERROR: parseSelf:", e, true);
      }
    }
  }
  
















  static Object parseArray(JSONObject jsonObj, Object self, HashMap<String, Class> classMap, IScope scope, JSONArray nArr, Class<?> elemClass, Object field_Array)
    throws JSONException, IllegalAccessException, InstantiationException
  {
    JSONObject nJsonObj = null;
    
    try
    {
      for (int i = 0; i < nArr.length(); i++) {
        try {
          Object eObj = null;
          
          if (elemClass.isArray()) {
            JSONArray subArr = nArr.getJSONArray(i);
            
            Class<?> subElemClass = elemClass.getComponentType();
            Object subField_Array = Array.newInstance(subElemClass, subArr.length());
            
            eObj = parseArray(jsonObj, self, classMap, scope, subArr, subElemClass, subField_Array);
          } else if (elemClass.equals(String.class)) {
            eObj = nArr.getString(i);
          } else if (elemClass.equals(Integer.TYPE))
          {



            try
            {


              eObj = Integer.valueOf(nArr.getInt(i));
            }
            catch (Exception e) {
              try {
                String valuestr = nArr.getString(i);
                
                eObj = Integer.valueOf(new BigInteger(valuestr.substring(2), 16).intValue());
              }
              catch (Exception e2) {
                CErrorManager.logEvent("JSON_HELPER", "Json Array Format Error: ", e2, false);
              }
            }
          }
          else if (elemClass.equals(Float.TYPE)) {
            eObj = Float.valueOf((float)nArr.getDouble(i));
          } else if (elemClass.equals(Double.TYPE)) {
            eObj = Double.valueOf(nArr.getDouble(i));
          } else {
            nJsonObj = nArr.getJSONObject(i);
            


            if (nJsonObj.has("type")) {
              Class<?> subClass = (Class)classMap.get(nJsonObj.getString("type"));
              



              if (subClass == null) {
                System.out.println("JSON_HELPER - ClassMap missing for:" + nJsonObj.getString("type"));
              }
              else {
                eObj = subClass.newInstance();

              }
              

            }
            else
            {

              eObj = elemClass.newInstance();
            }
            
            ((ILoadableObject)eObj).loadJSON(nJsonObj, scope);
          }
          
          Array.set(field_Array, i, eObj);
        }
        catch (NullPointerException e) {
          e.printStackTrace();
          System.out.println("JSON_HELPER - Null Object in :" + nJsonObj);
        }
      }
    }
    catch (Exception e) {
      CErrorManager.logEvent("JSON_HELPER", "Json Array Format Error: ", e, false);
    }
    
    return field_Array;
  }
}
