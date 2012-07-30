package polybuf.classifiers;

import static org.junit.Assert.assertFalse;
import static polybuf.core.DefaultStringParserTest.*;

import org.junit.Test;

import com.google.common.base.Charsets;

public class Base64ClassifierTest {

  @Test
  public void invalidLengthStrictBytes() {
    assertFalse(Base64Classifier.isStrictBase64(invalidLengthBase64.getBytes(Charsets.UTF_8)));
  }

  @Test
  public void invalid1PadStrictBytes() {
    assertFalse(Base64Classifier.isStrictBase64(invalid1PadBase64.getBytes(Charsets.UTF_8)));
  }

  @Test
  public void invalid2PadStrictBytes() {
    assertFalse(Base64Classifier.isStrictBase64(invalid2PadBase64.getBytes(Charsets.UTF_8)));
  }
}
