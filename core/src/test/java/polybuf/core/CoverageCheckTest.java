package polybuf.core;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import polybuf.core.test.Coverage;

import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Message;

public class CoverageCheckTest {

  private static class Counter {
    private int required = 0;
    private int optional_no_default = 0;
    private int optional_with_default = 0;
    private int repeated = 0;

    public void increment(FieldDescriptor field) {
      if (field.isRequired()) {
        required += 1;
      }
      else if (field.isRepeated()) {
        repeated += 1;
      }
      else if (field.hasDefaultValue()) {
        optional_with_default += 1;
      }
      else {
        optional_no_default += 1;
      }
    }
    
    public boolean isValidForType(FieldDescriptor.Type type) {
      // do not support or test for groups yet
      assertFalse(FieldDescriptor.Type.GROUP == type);
      return required > 0 && optional_no_default > 0 && repeated > 0 &&
          (FieldDescriptor.Type.MESSAGE == type || optional_with_default > 0);
    }
  }
  
  private void countDescriptorFields(Descriptor message) {
    for (FieldDescriptor field : message.getFields()) {
      Counter counter = counters.get(field.getType());
      if (counter == null) {
        counter = new Counter();
        counters.put(field.getType(), counter); 
      }
      counter.increment(field);
    }
    for (Descriptor nested : message.getNestedTypes()) {
      countDescriptorFields(nested);
    }
  }
  
  private void countMessages(Class<?> generatedTopClass) throws Exception {
    for (Class<?> member : generatedTopClass.getDeclaredClasses()) {
      if (Message.class.isAssignableFrom(member)) {
        Descriptor descriptor = (Descriptor) member.getDeclaredMethod("getDescriptor").invoke(null);
        countDescriptorFields(descriptor);
      }
    }
  }
  
  private Map<FieldDescriptor.Type,Counter> counters;
  
  @Before
  public void init() {
    counters = new HashMap<FieldDescriptor.Type,Counter>();
  }
  
  @Test
  public void test() throws Exception {
    countMessages(Coverage.class);
    for (FieldDescriptor.Type type : FieldDescriptor.Type.values()) {
      if (FieldDescriptor.Type.GROUP != type) {
        Counter counter = counters.get(type);
        assertNotNull(counter);
        assertTrue(type.toString(), counter.isValidForType(type));
      }
    }
  }
}
