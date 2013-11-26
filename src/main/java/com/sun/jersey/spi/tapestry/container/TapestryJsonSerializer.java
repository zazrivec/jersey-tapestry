package com.sun.jersey.spi.tapestry.container;

import org.apache.tapestry5.json.JSONObject;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;

import java.io.IOException;

public class TapestryJsonSerializer extends JsonSerializer<JSONObject> {

    @Override
    public void serialize(JSONObject jsonObject, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException, JsonProcessingException {
        jsonGenerator.writeRaw(jsonObject.toCompactString());
    }
}
