package com.iwhalecloud.bote.doc.common.support.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import java.util.HashMap;

/**
 * <p>
 * Null jsonObject serialization.
 * </p>
 */
public class NullJsonObjectSerializer extends JsonSerializer<Object> {

  @Override
  public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers)
    throws IOException {
    gen.writeObject(new HashMap<>());
  }
}
