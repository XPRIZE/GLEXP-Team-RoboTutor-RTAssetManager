package cmu.xprize;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.io.PrintStream;
import java.io.StringWriter;
import org.json.JSONString;
import org.json.JSONWriter;





















public class JSON_Util
{
  private StringWriter outString;
  private JSONWriter writer;
  
  public JSON_Util()
  {
    this.outString = new StringWriter();
    this.writer = new JSONWriter(this.outString);
    
    this.writer.object();
  }
  
  public JSON_Util(ILoadableObject obj)
  {
    this.outString = new StringWriter();
    this.writer = new JSONWriter(this.outString);
    
    this.writer.object();
    
    obj.toJSON(this);
  }
  
  public void addObject(ILoadableObject obj)
  {
    try {
      this.writer.object();
      
      obj.toJSON(this);
      
      this.writer.endObject();
    }
    catch (Exception e) {
      System.out.println("JSON_Util - Json Object add failed: " + e);
    }
  }
  
  public void addObject(String key, String value)
  {
    try
    {
      this.writer.object();
      
      addElement(key, value);
      
      this.writer.endObject();
    }
    catch (Exception e) {
      System.out.println("JSON_Util - Json Object add failed: " + e);
    }
  }
  
  public void startHashMap(String name)
  {
    try
    {
      this.writer.key(name);
      this.writer.object();
    }
    catch (Exception e) {
      System.out.println("JSON_Util - Json HashMap start failed: " + e);
    }
  }
  
  public void endHashMap()
  {
    try {
      this.writer.endObject();
    }
    catch (Exception e) {
      System.out.println("JSON_Util - Json HashMap end failed: " + e);
    }
  }
  

  public void startArray(String name)
  {
    try
    {
      this.writer.key(name);
      this.writer.array();
    }
    catch (Exception e) {
      System.out.println("JSON_Util - Json Array start failed: " + e);
    }
  }
  
  public void addArray(String stringval)
  {
    try {
      this.writer.value(stringval);
    }
    catch (Exception e) {
      System.out.println("JSON_Util - Json Array add failed: " + e);
    }
  }
  
  public void addArray(int intval)
  {
    try {
      this.writer.value(intval);
    }
    catch (Exception e) {
      System.out.println("JSON_Util - Json Array add failed: " + e);
    }
  }
  
  public void addArray(ILoadableObject objval)
  {
    try {
      addObject(objval);
    }
    catch (Exception e) {
      System.out.println("JSON_Util - Json Array add failed: " + e);
    }
  }
  
  public void addStringArray(String name, String[] strArray)
  {
    startArray(name);
    
    for (String item : strArray)
    {
      addArray(item);
    }
    endArray();
  }
  

  public void addIntArray(String name, int[] intArray)
  {
    startArray(name);
    
    for (int item : intArray)
    {
      addArray(item);
    }
    endArray();
  }
  

  public void addHexArray(String name, int[] intArray)
  {
    startArray(name);
    
    for (int item : intArray)
    {
      addArray(("0x" + Integer.toHexString(item)).toUpperCase());
    }
    endArray();
  }
  

  public void addValueArray(String name, JSONString[] objArray)
  {
    startArray(name);
    
    for (JSONString item : objArray)
    {
      this.writer.value(item);
    }
    endArray();
  }
  

  public void addObjectArray(String name, ILoadableObject[] objArray)
  {
    startArray(name);
    
    for (ILoadableObject item : objArray)
    {
      addArray(item);
    }
    endArray();
  }
  
  public void endArray()
  {
    try
    {
      this.writer.endArray();
    }
    catch (Exception e) {
      System.out.println("JSON_Util - Json Array end failed: " + e);
    }
  }
  
  public String getValue()
  {
    return this.outString.toString();
  }
  
  public void addElement(String key, String value)
  {
    try {
      this.writer.key(key);
      this.writer.value(value);
    }
    catch (Exception e) {
      System.out.println("JSON_Util - Json Element add String failed: " + e);
    }
  }
  
  public void addElement(String key, int value)
  {
    try {
      this.writer.key(key);
      this.writer.value(value);
    }
    catch (Exception e) {
      System.out.println("JSON_Util - Json Element add int failed: " + e);
    }
  }
  
  public void addElement(String key, boolean value)
  {
    try {
      this.writer.key(key);
      this.writer.value(value);
    }
    catch (Exception e) {
      System.out.println("JSON_Util - Json Element add boolean failed: " + e);
    }
  }
  
  public void addElement(String key, ILoadableObject obj)
  {
    try {
      this.writer.key(key);
      addObject(obj);
    }
    catch (Exception e) {
      System.out.println("JSON_Util - Json Element add obj failed: " + e);
    }
  }
  
  public String close()
  {
    try {
      this.writer.endObject();
    }
    catch (Exception e) {
      System.out.println("JSON_Util - Json close failed: " + e);
    }
    
    return this.outString.toString();
  }
  
  public String prettyClose()
  {
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    JsonParser jp = new JsonParser();
    JsonElement je = jp.parse(close());
    String prettyJsonString = gson.toJson(je);
    
    return prettyJsonString;
  }
}
