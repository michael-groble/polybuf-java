package polybuf.classifiers;

import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.zip.GZIPInputStream;

import org.apache.commons.codec.binary.Base64;
import org.junit.Test;

import polybuf.core.DefaultStringParser;
import polybuf.core.StringParser;

import com.google.common.base.Charsets;
import com.google.common.base.Splitter;

public class Base64GuessTest {

  private List<String> words(String line) {
    List<String> words = new LinkedList<String>();
    for (String word : Splitter.on(' ').trimResults().omitEmptyStrings().split(line)) {
      words.add(word.toLowerCase());
      words.add(word.toUpperCase());
      words.add(word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase());
    }
    return words;
  }

  private void log(String string, int type) {
    // System.out.println(string + " " + type);
  }

  private static interface RandomEncoder {
    public String nextEncoded(byte[] bytes);
  }

  private static class RandomByteEncoder implements RandomEncoder {
    private final Random rnd = new Random();

    @Override
    public String nextEncoded(byte[] bytes) {
      rnd.nextBytes(bytes);
      return Base64.encodeBase64String(bytes);
    }
  }

  private static class RandomAsciiEncoder implements RandomEncoder {
    private final Random rnd = new Random();

    @Override
    public String nextEncoded(byte[] bytes) {
      for (int i = 0; i < bytes.length; ++i) {
        bytes[i] = (byte) rnd.nextInt(128);
      }
      return Base64.encodeBase64String(bytes);
    }
  }

  private void assertRandomBytes(int n, float maxErrorRate, RandomEncoder encoder) {
    int N = 10000;
    int errors = 0;
    byte[] bytes = new byte[n];
    for (int i = 0; i < N; ++i) {
      String encoded = encoder.nextEncoded(bytes);
      if (!Base64Classifier.isLikelyBase64(encoded.getBytes(Charsets.US_ASCII))) {
        log(encoded, 2);
        ++errors;
      }
    }
    assertTrue(n + " " + errors, errors / N <= maxErrorRate);
  }

  @Test
  public void randomBytes() {
    RandomEncoder rnd = new RandomByteEncoder();
    for (int i = 1; i <= 50; ++i) {
      float maxErrorRate = 0.0001f;
      if (i == 3) {
        maxErrorRate = 0.001f; // 4 bytes encoded, so no padding (and trickier)
      }
      assertRandomBytes(i, maxErrorRate, rnd);
    }
  }

  @Test
  public void encodedAsciiBytes() {
    RandomEncoder rnd = new RandomAsciiEncoder();
    for (int i = 1; i <= 50; ++i) {
      float maxErrorRate = 0.0001f;
      if (i == 3) {
        maxErrorRate = 0.001f; // 4 bytes encoded, so no padding (and trickier)
      }
      assertRandomBytes(i, maxErrorRate, rnd);
    }
  }

  /*
   * Testing the classifier requires a large text corpus which is not included in the source repository by default. The
   * format of the file is a plain text file with any number of space-delimited words on each line. The words should
   * consist solely of Base64 characters: a-z A-Z 0-9 + /
   */
  private BufferedReader wordReader() throws IOException {
    try {
      return new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(
          "src/test/files/words.txt.gz"))));
    }
    catch (FileNotFoundException ex) {
    }
    return null;
  }

  @Test
  public void wordsBase64() throws IOException {
    BufferedReader reader = wordReader();
    if (reader == null) {
      return;
    }
    int words = 0;
    int errors = 0;
    for (String line = reader.readLine(); line != null; line = reader.readLine()) {
      for (String word : words(line)) {
        ++words;
        String encoded = Base64.encodeBase64String(word.getBytes(Charsets.US_ASCII));
        if (!Base64Classifier.isLikelyBase64(encoded.getBytes(Charsets.US_ASCII))) {
          log(encoded, 1);
          ++errors;
        }
      }
    }
    float errorRate = (float) errors / (float) words;
    assertTrue("" + errorRate, errorRate < 0.001f);
  }

  @Test
  public void words() throws FileNotFoundException, IOException {
    BufferedReader reader = wordReader();
    if (reader == null) {
      return;
    }
    int words = 0;
    int fourCharErrors = 0;
    int otherErrors = 0;
    for (String line = reader.readLine(); line != null; line = reader.readLine()) {
      for (String word : words(line)) {
        ++words;
        if (Base64Classifier.isLikelyBase64(word.getBytes(Charsets.US_ASCII))) {
          log(word, 0);
          if (word.length() == 4) {
            ++fourCharErrors;
          }
          else {
            ++otherErrors;
          }
        }
      }
    }
    float otherRate = (float) otherErrors / (float) words;
    assertTrue("!=4 " + otherRate, otherRate < 0.002f);
    float errorRate = (float) fourCharErrors / (float) words;
    assertTrue("==4 " + errorRate, errorRate < 0.018f);
  }

  @Test
  public void compatibleWords() throws FileNotFoundException, IOException {
    BufferedReader reader = wordReader();
    if (reader == null) {
      return;
    }
    int words = 0;
    int fourCharErrors = 0;
    int otherErrors = 0;
    StringParser parser = new DefaultStringParser();
    for (String line = reader.readLine(); line != null; line = reader.readLine()) {
      for (String word : words(line)) {
        ++words;
        if (!word.equals(parser.asCompatibleString(word))) {
          log(word, 4);
          if (word.length() == 4) {
            ++fourCharErrors;
          }
          else {
            ++otherErrors;
          }
        }
      }
    }
    float otherRate = (float) otherErrors / (float) words;
    assertTrue("!=4 " + otherRate, otherRate < 0.001f);
    float errorRate = (float) fourCharErrors / (float) words;
    assertTrue("==4 " + errorRate, errorRate < 0.005f);
  }

}
