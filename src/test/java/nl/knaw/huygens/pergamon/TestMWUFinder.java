package nl.knaw.huygens.pergamon;

import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;

public class TestMWUFinder {
  @Test
  public void basic() {
    MWUFinder f = new MWUFinder(singletonList(asList("foo", "bar", "baz")).stream());
    String text = "somewhere in this string lives foo  bar  baz, the mwu...";
    List<MWUFinder.Hit> found = f.find(text).collect(Collectors.toList());

    assertEquals(1, found.size());
    assertEquals("foo  bar  baz", found.get(0).span.getCoveredText(text));
    assertEquals(asList("foo", "bar", "baz"), found.get(0).pattern);
  }
}
