package lsh.core;

/*
 * Corner containing grid hashes and associated points
 * 
 * Native String representation- [id],0,1,2,3,...,n[*payload]
 * 
 * Can be without points
 */

public class Corner {
  public final String id;
  // can be null for "runt" corners
  public final int[] hashes;
  public final String payload;

  public Corner(int[] corner) {
    this.hashes = corner;
    this.id = null;
    this.payload = null;
  }

  public Corner(int[] corner, String id, String payload) {
    this.hashes = corner;
    this.id = id;
    this.payload = payload;
  }

  @Override
  public boolean equals(Object other) {
    Corner co = (Corner) other;
    if (null != id && null != co.id)
      return id.equals(co.id);
    int[] ohash = co.hashes;
    for(int i = 0; i < hashes.length; i++) {
      if (hashes[i] != ohash[i])
        return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    if (null != id)
      return id.hashCode();
    int sum = 0;
    for(int i = 0; i < hashes.length; i++) {
      sum += hashes[i]*i;
    }
    return sum;
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    if (null != id)
      sb.append(id);
    if (null != hashes)
      for(int i = 0; i < hashes.length; i++) {
        sb.append(',');
        sb.append(hashes[i]);
      }
    if (null != payload)
      sb.append('*' + payload);
    return sb.toString();
  }

  public static Corner newCorner(String blob) {
    return newCorner(blob, "", false);
  }

  public static Corner newCorner(String blob, String tail) {
    return newCorner(blob, tail, false);
  }

  /*
   * blob = id,0,1,2,...,n
   * tail = rest of line, want payload
   */
  public static Corner newCorner(String blob, String tail, boolean runt) {
    String payload = Point.getPayload(tail);
    int[] hashes = null;
    String id = null;
    if (! runt) {
      String[] parts = blob.split(Point.SPLIT);
      if (null != parts[0] && parts[0].length() > 0) {
        id = parts[0];
      }
      hashes = new int[parts.length - 2];
      for(int i = 0; i < parts.length - 2; i++) {
        hashes[i] = Integer.parseInt(parts[i + 1]);
      }
    }
    return new Corner(hashes, id, payload);
  }
}

