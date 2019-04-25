package cmu.xprize;

public class CErrorManager
{
  public static void logEvent(String TAG, String Msg, boolean printTrace)
  {
    System.out.println(TAG + " - " + Msg);
    try
    {
      Thread.sleep(400L);
    }
    catch (InterruptedException localInterruptedException) {}
  }



  public static void logEvent(String TAG, String Msg, Exception e, boolean printTrace)
  {
    if ((printTrace) && (e != null)) {
      e.printStackTrace();
    }
    if (e != null) {
      System.out.println(TAG + " - " + Msg + e);
    } else {
      System.out.println(TAG + " - " + Msg);
    }
    try {
      Thread.sleep(600L);
    }
    catch (InterruptedException localInterruptedException) {}
  }
}
