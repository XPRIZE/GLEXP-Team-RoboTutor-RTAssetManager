package cmu.xprize;

import org.json.JSONObject;

public abstract interface ILoadableObject
{
  public abstract void loadJSON(JSONObject paramJSONObject, IScope paramIScope);
  
  public abstract void toJSON(JSON_Util paramJSON_Util);
}
