package polybuf.core;

import java.io.IOException;

import com.google.common.primitives.UnsignedLong;

public class LoggingEncoder implements Encoder {
  private final StringBuilder log;
  
  public LoggingEncoder(StringBuilder log) {
    this.log = log;
  }
  
  public static EncoderFactory<StringBuilder> factory() {
    return new EncoderFactory<StringBuilder>() {
      @Override
      public Encoder encoder(StringBuilder output) throws IOException {
        return new LoggingEncoder(output);
      }
    };
  }
  
  @Override
  public void startRootList(String messageName) throws IOException {
    log.append("startRootList ").append(messageName).append("\n");
  }

  @Override
  public void endRootList(String messageName) throws IOException {
    log.append("endRootList ").append(messageName).append("\n");
  }

  @Override
  public void startRootMessage(String messageName) throws IOException {
    log.append("startRootMessage ").append(messageName).append("\n");
  }

  @Override
  public void endRootMessage(String messageName) throws IOException {
    log.append("endRootMessage ").append(messageName).append("\n");
  }

  @Override
  public void startRepeatedRoot(String messageName) throws IOException {
    log.append("startRepeatedRoot ").append(messageName).append("\n");
  }

  @Override
  public void endRepeatedRoot(String messageName) throws IOException {
    log.append("endRepeatedRoot ").append(messageName).append("\n");
  }

  @Override
  public void startMessageField(String fieldName) throws IOException {
    log.append("startMessageField ").append(fieldName).append("\n");
  }

  @Override
  public void endMessageField(String fieldName) throws IOException {
    log.append("endMessageField ").append(fieldName).append("\n");
  }

  @Override
  public void startRepeatedMessageField(String messageName) throws IOException {
    log.append("startRepeatedMessageField ").append(messageName).append("\n");
  }

  @Override
  public void endRepeatedMessageField(String messageName) throws IOException {
    log.append("endRepeatedMessageField ").append(messageName).append("\n");
  }

  @Override
  public void startRepeatedField(String fieldName) throws IOException {
    log.append("startRepeatedField ").append(fieldName).append("\n");
  }

  @Override
  public void endRepeatedField(String fieldName) throws IOException {
    log.append("endRepeatedField ").append(fieldName).append("\n");
  }

  @Override
  public void scalarField(String fieldName, boolean fieldValue) throws IOException {
    log.append("scalarBooleanField ").append(fieldName).append(" ").append(fieldValue).append("\n");
  }

  @Override
  public void scalarField(String fieldName, int fieldValue) throws IOException {
    log.append("scalarIntegerField ").append(fieldName).append(" ").append(fieldValue).append("\n");
  }

  @Override
  public void scalarField(String fieldName, long fieldValue) throws IOException {
    log.append("scalarLongField ").append(fieldName).append(" ").append(fieldValue).append("\n");
  }

  @Override
  public void scalarField(String fieldName, UnsignedLong fieldValue) throws IOException {
    log.append("scalarUnsignedLongField ").append(fieldName).append(" ").append(fieldValue).append("\n");
  }

  @Override
  public void scalarField(String fieldName, float fieldValue) throws IOException {
    log.append("scalarFloatField ").append(fieldName).append(" ").append(fieldValue).append("\n");
  }

  @Override
  public void scalarField(String fieldName, double fieldValue) throws IOException {
    log.append("scalarDoubleField ").append(fieldName).append(" ").append(fieldValue).append("\n");
  }

  @Override
  public void scalarField(String fieldName, String fieldValue) throws IOException {
    log.append("scalarStringField ").append(fieldName).append(" ").append(fieldValue).append("\n");
  }

  @Override
  public void repeatedScalarField(String fieldName, boolean fieldValue) throws IOException {
    log.append("repeatedScalarBooleanField ").append(fieldName).append(" ").append(fieldValue).append("\n");
  }

  @Override
  public void repeatedScalarField(String fieldName, int fieldValue) throws IOException {
    log.append("repeatedScalarIntegerField ").append(fieldName).append(" ").append(fieldValue).append("\n");
  }

  @Override
  public void repeatedScalarField(String fieldName, long fieldValue) throws IOException {
    log.append("repeatedScalarLongField ").append(fieldName).append(" ").append(fieldValue).append("\n");
  }

  @Override
  public void repeatedScalarField(String fieldName, UnsignedLong fieldValue) throws IOException {
    log.append("repeatedScalarUnsignedLongField ").append(fieldName).append(" ").append(fieldValue).append("\n");
  }

  @Override
  public void repeatedScalarField(String fieldName, float fieldValue) throws IOException {
    log.append("repeatedScalarFloatField ").append(fieldName).append(" ").append(fieldValue).append("\n");
  }

  @Override
  public void repeatedScalarField(String fieldName, double fieldValue) throws IOException {
    log.append("repeatedScalarDoubleField ").append(fieldName).append(" ").append(fieldValue).append("\n"); 
  }

  @Override
  public void repeatedScalarField(String fieldName, String fieldValue) throws IOException {
    log.append("repeatedScalarStringField ").append(fieldName).append(" ").append(fieldValue).append("\n");
  }
}