package edu.ucsb.cs156.diningcachingproxy.utilities;

public class Sleep {
  public static void sleepQuietly(long millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }
}
