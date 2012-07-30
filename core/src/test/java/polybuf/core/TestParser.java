package polybuf.core;

public class TestParser extends ScalarParser {

  public TestParser(StringParser stringParser) {
    super(stringParser);
  }
  @Override
  protected boolean isPositiveInfinityLiteral(String string) {
    return "Infinity".equals(string);
  }

  @Override
  protected boolean isNegativeInfinityLiteral(String string) {
    return "-Infinity".equals(string);
  }

  @Override
  protected boolean isNaNLiteral(String string) {
    return "NaN".equals(string);
  }
}