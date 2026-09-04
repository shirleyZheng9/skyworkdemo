package com.iwhalecloud.bote.doc.common.support.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;

/**
 * <p>
 * Empty array serialization.
 * </p>
 */
public class NullArraySerializer extends JsonSerializer<Object> {

  @Override
  public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers)
    throws IOException {
    if (value == null) {
      gen.writeStartArray();
      gen.writeEndArray();
    }
    else {
      gen.writeObject(value);
    }

  }
}
