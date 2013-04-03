
package no.priv.garshol.duke;

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Collection;
import java.io.IOException;
import java.io.Writer;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.BufferedReader;

import org.xml.sax.SAXException;

import no.priv.garshol.duke.utils.Utils;

/**
 * Compare two specific records to understand their scores.
 */
public class DebugCompare {

  public static void main(String[] argv) throws IOException, SAXException {
    if (argv.length != 3) {
      usage();
      return;      
    }

    // load configuration
    Configuration config = ConfigLoader.load(argv[0]);
    Database database = config.createDatabase(false);
    if (database.isInMemory()) {
      System.err.println("ERROR: Database index is in-memory. Cannot look up " +
                         "identities from earlier run");
      return;
    }
  
    // load records
    Record r1 = database.findRecordById(argv[1]);
    if (r1 == null) {
      System.err.println("Couldn't find record for '" + argv[1] + "'");
      return;
    }    
    Record r2 = database.findRecordById(argv[2]);
    if (r2 == null) {
      System.err.println("Couldn't find record for '" + argv[2] + "'");
      return;
    }

    // do comparison
    double prob = 0.5;
    for (String propname : r1.getProperties()) {
      System.out.println("---" + propname);
      Property prop = config.getPropertyByName(propname);
      if (prop.isIdProperty())
        continue;

      Collection<String> vs1 = r1.getValues(propname);
      Collection<String> vs2 = r2.getValues(propname);
      if (vs1.isEmpty() || vs2.isEmpty())
        continue; // no values to compare, so skip
      
      double high = 0.0;
      for (String v1 : vs1) {
        if (v1.equals(""))
          continue;
        
        for (String v2 : vs2) {
          if (v2.equals(""))
            continue;

          try {
            double d = prop.getComparator().compare(v1, v2);
            double p = prop.compare(v1, v2);
            System.out.println("'" + v1 + "' ~ '" + v2 + "': " + d +
                               " (prob " + p + ")");
            high = Math.max(high, p);
          } catch (Exception e) {
            throw new RuntimeException("Comparison of values '" + v1 + "' and "+
                                       "'" + v2 + "' failed", e);
          }
        }        
      }

      double newprob = Utils.computeBayes(prob, high);
      System.out.println("Result: " + prob + " -> " + newprob + "\n");
      prob = newprob;
    }

    System.out.println("Overall: " + prob);
  }

  private static void usage() {
    System.out.println("");
    System.out.println("java no.priv.garshol.duke.DebugCompare <cfgfile> <id1> <id2>");
    System.out.println("");
  }
  
}
