
package no.priv.garshol.duke.test;

import org.junit.Test;
import org.junit.Before;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.assertEquals;

import no.priv.garshol.duke.comparators.NumericComparator;

public class NumericComparatorTest {
  private NumericComparator comp;

  @Before
  public void setUp() {
    comp = new NumericComparator();
  }

  @Test
  public void testEqual() {
    assertEquals(1.0, comp.compare("42", "42"));
  }

  @Test
  public void testEqual2() {
    assertEquals(1.0, comp.compare("42.0", "42.0"));
  }

  @Test
  public void testHalf() {
    assertEquals(0.5, comp.compare("21.0", "42.0"));
  }

  @Test
  public void testHalfInverted() {
    assertEquals(0.5, comp.compare("42.0", "21.0"));
  }

  @Test
  public void testHalfBelowMin() {
    comp.setMinRatio(0.75);
    assertEquals(0.0, comp.compare("21.0", "42.0"));
  }

  @Test
  public void testHalfAboveMin() {
    comp.setMinRatio(0.25);
    assertEquals(0.5, comp.compare("21.0", "42.0"));
  }

  @Test
  public void testZero() {
    assertEquals(1.0, comp.compare("0.0", "0.0"));
  }

  @Test
  public void testFirstIsZero() {
    assertEquals(0.0, comp.compare("0.0", "42.0"));
  }

  @Test
  public void testSecondIsZero() {
    assertEquals(0.0, comp.compare("42.0", "0.0"));
  }
}