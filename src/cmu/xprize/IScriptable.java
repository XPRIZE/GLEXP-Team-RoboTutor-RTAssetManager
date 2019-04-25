package cmu.xprize;

public abstract interface IScriptable
{
  public abstract void setName(String paramString);
  
  public abstract String getName();
  
  public abstract String getType();
  
  public abstract Object getValue();
  
  public abstract String toString();
  
  public abstract void set(String paramString);
  
  public abstract void add(String paramString);
  
  public abstract void subtract(String paramString);
  
  public abstract Object evaluate(boolean paramBoolean);
  
  public abstract void preEnter();
  
  public abstract String applyNode();
}
