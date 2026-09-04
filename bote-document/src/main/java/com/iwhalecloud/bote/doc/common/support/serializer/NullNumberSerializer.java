package com.iwhalecloud.bote.doc.common.support.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;

/**
 * <p>
 * Null to Number serialization.
 * </p>
 */
public class NullNumberSerializer extends JsonSerializer<Number> {

  @Override
  public void serialize(Number value, JsonGenerator gen, SerializerProvider serializers)
    throws IOException {
    gen.writeNumber(0);
  }
}
