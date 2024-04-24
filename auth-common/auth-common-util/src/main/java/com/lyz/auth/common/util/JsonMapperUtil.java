package com.lyz.auth.common.util;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.TimeZone;

/**
 * Desc:Json tool
 *
 * @author lyz
 * @version 1.0.0
 * @date 2023/3/8 15:12
 */
@UtilityClass
public class JsonMapperUtil {

    private static final ObjectMapper OBJECT_MAPPER = Jackson2ObjectMapperBuilder
            .json()
            .createXmlMapper(false)
            .dateFormat(new SimpleDateFormat(DatePattern.NORM_DATE_PATTERN))
            .timeZone(TimeZone.getTimeZone("GMT+8"))
            .build()
            .enable(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS,false)
            .registerModule(new SimpleModule()
                    .addSerializer(Long.class, ToStringSerializer.instance)
                    .addSerializer(Long.TYPE, ToStringSerializer.instance)
                    .addSerializer(Double.class, new DoubleSerializer())
                    .addSerializer(Double.TYPE, new DoubleSerializer())
                    .addDeserializer(Date.class, new DateTimeDeserializer())
            );

    @SneakyThrows
    public static String toJSONString(Object obj) {
        if (Objects.isNull(obj)) {
            return null;
        }
        return OBJECT_MAPPER.writeValueAsString(obj);
    }

    @SneakyThrows
    public static String toJSONPrettyString(Object obj) {
        if (Objects.isNull(obj)) {
            return null;
        }
        return OBJECT_MAPPER.writeValueAsString(obj);
    }

    @SneakyThrows
    public static <T> T readValue(String content, Class<T> clazz) {
        if (!StringUtils.hasText(content) || Objects.isNull(clazz)) {
            return null;
        }
        return OBJECT_MAPPER.readValue(content, clazz);
    }

    @SneakyThrows
    public static <T> T readValue(InputStream inputStream, Class<T> clazz) {
        if (ObjectUtil.hasNull(inputStream, clazz)) {
            return null;
        }
        return OBJECT_MAPPER.readValue(inputStream, clazz);
    }

    @SneakyThrows
    public static <T> T readValue(JsonNode jsonNode, Class<T> clazz) {
        if (ObjectUtil.hasNull(jsonNode, clazz)) {
            return null;
        }
        return OBJECT_MAPPER.treeToValue(jsonNode, clazz);
    }

    @SneakyThrows
    public static void writeValue(OutputStream out, Object value) {
        OBJECT_MAPPER.writeValue(out, value);
    }

    @SneakyThrows
    public static JsonNode readTree(Object obj) {
        if (Objects.isNull(obj)) {
            return null;
        }
        if (obj.getClass() == String.class) {
            return OBJECT_MAPPER.readTree((String) obj);
        }
        return OBJECT_MAPPER.readTree(OBJECT_MAPPER.writeValueAsString(obj));
    }

    private class DoubleSerializer extends JsonSerializer<Double> {

        @Override
        public void serialize(Double value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            BigDecimal d = new BigDecimal(value.toString());
            gen.writeNumber(d.stripTrailingZeros().toPlainString());
        }

        @Override
        public Class<Double> handledType() {
            return Double.class;
        }
    }

    private class DateTimeDeserializer extends JsonDeserializer<Date> {

        @Override
        public Date deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            String text = p.getText();
            if (!StringUtils.hasText(text)) {
                return null;
            }
            if (text.length() == DatePattern.NORM_DATETIME_PATTERN.length()) {
                return DateUtil.parse(text, DatePattern.NORM_DATETIME_PATTERN);
            }
            if (text.length() == DatePattern.UTC_MS_WITH_XXX_OFFSET_PATTERN.length()) {
                return DateUtil.parse(text, DatePattern.UTC_MS_WITH_XXX_OFFSET_PATTERN);
            }
            return DateUtil.parse(text, DatePattern.NORM_DATE_PATTERN);
        }
    }
}
