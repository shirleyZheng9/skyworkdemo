package com.iwhalecloud.bote.doc.common.support.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;

/**
 * <p>
 * Empty Boolean serialization.
 * </p>
 */
public class NullBooleanSerializer extends JsonSerializer<Boolean> {

  @Override
  public void serialize(Boolean value, JsonGenerator gen, SerializerProvider serializers)
    throws IOException {
    gen.writeBoolean(false);
  }
}
