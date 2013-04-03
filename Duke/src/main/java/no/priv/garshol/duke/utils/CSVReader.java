
package no.priv.garshol.duke.utils;

import java.io.Reader;
import java.io.IOException;

public class CSVReader {
  private Reader in;
  private char[] buf;
  private int pos; // where we are in the buffer
  private int len;
  private String[] tmp;

  public CSVReader(Reader in) throws IOException {
    this.buf = new char[65386];
    this.pos = 0;
    this.len = in.read(buf, 0, buf.length);
    this.tmp = new String[1000];
    this.in = in;
  }

  // this is used for testing!
  public CSVReader(Reader in, int buflen) throws IOException {
    this.buf = new char[buflen];
    this.pos = 0;
    this.len = in.read(buf, 0, buf.length);
    this.tmp = new String[1000];
    this.in = in;
  }
  
  public String[] next() throws IOException {
    if (len == -1 || pos >= len)
      return null;

    int colno = 0;
    int rowstart = pos; // used for rebuffering at end
    int prev = pos - 1;
    boolean escaped_quote = false; // did we find an escaped quote?
    while (pos < len) {
      boolean startquote = false;
      if (buf[pos] == '"') {
        startquote = true;
        prev++;
        pos++;
      }

      // scan forward, looking for end of string
      while (true) {
        while (pos < len &&
               (startquote || buf[pos] != ',') &&
               (startquote || (buf[pos] != '\n' && buf[pos] != '\r')) &&
               !(startquote && buf[pos] == '"'))
          pos++;

        if (pos + 1 >= len ||
            (!(buf[pos] == '"' && buf[pos+1] == '"')))
          break; // we found the end of this value, so stop
        else {
          // found a "". carry on
          escaped_quote = true;
          pos += 2; // step to character after next
        }
      }

      if (escaped_quote)
        tmp[colno++] = unescape(new String(buf, prev + 1, pos - prev - 1));
      else
        tmp[colno++] = new String(buf, prev + 1, pos - prev - 1);
      
      if (startquote)
        pos++; // step over the '"'
      prev = pos;

      if (pos >= len)
        break; // jump out of the loop to rebuffer and try again
      
      if (buf[pos] == '\r' || buf[pos] == '\n') {
        pos++; // step over the \r or \n
        if (pos >= len)
          break; // jump out of the loop to rebuffer and try again
        if (buf[pos] == '\n')
          pos++; // step over this, too
        break; // we're done
      }
      pos++; // step over either , or \n
    }

    if (pos >= len) {
      // this means we've exhausted the buffer. that again means either we've
      // read the entire stream, or we need to fill up the buffer.
      System.arraycopy(buf, rowstart, buf, 0, len - rowstart);
      len = len - rowstart;
      int read = in.read(buf, len, buf.length - len);
      if (read != -1) {
        len += read;
        pos = 0;
        return next();
      } else
        len = -1;
    }

    String[] row = new String[colno];
    for (int ix = 0; ix < colno; ix++) 
      row[ix] = tmp[ix];
    return row;
  }

  public void close() throws IOException {
    in.close();
  }

  private String unescape(String val) {
    return val.replace("\"\"", "\"");
  }
}