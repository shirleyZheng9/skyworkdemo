package com.iwhalecloud.bote.doc.common.support.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;

/**
 * <p>
 * Null Object serialization.
 * </p>
 */
public class NullObjectSerializer extends JsonSerializer<Object> {

  @Override
  public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers)
    throws IOException {
    gen.writeStartObject();
    gen.writeEndObject();
  }
}
