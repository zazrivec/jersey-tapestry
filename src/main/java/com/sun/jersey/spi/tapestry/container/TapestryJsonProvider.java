package com.sun.jersey.spi.tapestry.container;

import org.apache.tapestry5.json.JSONObject;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.Providers;
import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.Map;

@Provider
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TapestryJsonProvider implements MessageBodyReader<JSONObject>, MessageBodyWriter<JSONObject> {

    private static final String CHARSET = "charset";

    public static final Charset UTF8 = Charset.forName("UTF-8");

    @Context
    protected Providers providers;

    @Override
    public boolean isReadable(Class<?> type, Type genericType,
                              Annotation[] annotations, MediaType mediaType) {
        return JSONObject.class == getDomainClass(genericType);
    }

    String readInputStreamAsString(Reader entityReader)
            throws IOException {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        int result = entityReader.read();
        while(result != -1) {
            byte b = (byte)result;
            buf.write(b);
            result = entityReader.read();
        }
        return buf.toString();
    }

    public static final Charset getCharset(MediaType m) {
        String name = (m == null) ? null : m.getParameters().get("charset");
        return (name == null) ? UTF8 : Charset.forName(name);
    }

    @Override
    public JSONObject readFrom(Class<JSONObject> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException, WebApplicationException {
        try {
            Charset charset = getCharset(mediaType);
            Map<String, String> mediaTypeParameters = mediaType.getParameters();
            Reader entityReader = new InputStreamReader(entityStream, charset);
            return new JSONObject(readInputStreamAsString(entityReader));
        } catch(Exception e) {
            throw new WebApplicationException(e);
        }
    }

    @Override
    public void writeTo(JSONObject jsonObject, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
        try {
            Charset charset = getCharset(mediaType);
            OutputStreamWriter osw = new OutputStreamWriter(entityStream, charset);
            PrintWriter pw = new PrintWriter(osw);
            jsonObject.print(pw);
            pw.flush();
        } catch(Exception e) {
            throw new WebApplicationException(e);
        }
    }

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return isReadable(type, genericType, annotations, mediaType);
    }

    @Override
    public long getSize(JSONObject jsonObject, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return -1;
    }

    private Class<?> getDomainClass(Type genericType) {
        if(genericType instanceof Class) {
            return (Class<?>) genericType;
        } else if(genericType instanceof ParameterizedType) {
            return (Class<?>) ((ParameterizedType) genericType).getActualTypeArguments()[0];
        } else {
            return null;
        }
    }
}
