package cmu.xprize;

public abstract interface IScope
{
  public abstract IScriptable mapSymbol(String paramString)
    throws Exception;
  
  public abstract String parseTemplate(String paramString);
  
  public abstract void put(String paramString, IScriptable paramIScriptable);
}
