package com.iwhalecloud.bote.doc.common.support.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;

/**
 * <p>
 * Null String serialization.
 * </p>
 */
public class NullStringSerializer extends JsonSerializer<String> {

  @Override
  public void serialize(String value, JsonGenerator gen, SerializerProvider serializers)
    throws IOException {
    gen.writeString("");
  }
}
