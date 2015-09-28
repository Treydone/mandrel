package io.mandrel;

import io.mandrel.common.data.Spider;
import io.mandrel.common.schema.SchemaGenerator;
import io.mandrel.data.filters.link.LinkFilter;
import io.mandrel.requests.Requester;
import io.mandrel.requests.dns.NameResolver;
import io.mandrel.requests.http.ua.UserAgentProvisionner;
import io.mandrel.requests.proxy.ProxyServersSource;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 *
 */
public class OtherTest {

	@Test
	public void test() throws JsonProcessingException {

		ObjectMapper objectMapper = new ObjectMapper();

		Class<?> clazz = LinkFilter.class;
		for (JsonSubTypes.Type type : clazz.getAnnotation(JsonSubTypes.class).value()) {
			System.err.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(new SchemaGenerator(objectMapper).getSchema(type.value())));
		}

	}

	@Test
	public void test1() {

		int level = 0;
		Class<Spider> clazz = Spider.class;

		inspect(level, clazz, "root");

	}

	public void inspect(int level, Type clazz, String name) {

		if (level > 6)
			return;

		if (clazz instanceof Class && !((Class<?>) clazz).isEnum() && ((Class<?>) clazz).getPackage() != null
				&& ((Class<?>) clazz).getPackage().getName().startsWith("io.mandrel")) {
			int newLevel = level + 1;
			Field[] fields = ((Class<?>) clazz).getDeclaredFields();

			for (Field field : fields) {
				Class<?> fieldType = field.getType();
				String text;
				if (!field.isAnnotationPresent(JsonIgnore.class) && !Modifier.isStatic(field.getModifiers())) {
					if (List.class.equals(fieldType) || Map.class.equals(fieldType)) {
						Type type = field.getGenericType();
						if (type instanceof ParameterizedType) {
							ParameterizedType pType = (ParameterizedType) type;
							Type paramType = pType.getActualTypeArguments()[pType.getActualTypeArguments().length - 1];

							text = field.getName() + "(container of " + paramType + ")";
							System.err.println(StringUtils.leftPad(text, text.length() + newLevel * 5, "\t-   "));
							inspect(newLevel, paramType, "");
						}
					} else {
						text = field.getName()
								+ (field.getType().isPrimitive() || field.getType().equals(String.class) || field.getType().equals(LocalDateTime.class) ? " ("
										+ field.getType().getName() + ")" : "");
						System.err.println(StringUtils.leftPad(text, text.length() + newLevel * 5, "\t-   "));
						inspect(newLevel, fieldType, field.getName());
					}

					// JsonSubTypes subtype =
					// fieldType.getAnnotation(JsonSubTypes.class);
					// if (subtype != null) {
					// int subLevel = level + 2;
					// text = "subtypes:";
					// System.err.println(StringUtils.leftPad(text,
					// text.length() + subLevel * 5, "\t-   "));
					// for (JsonSubTypes.Type type : subtype.value()) {
					// text = "subtype:" + type.name();
					// System.err.println(StringUtils.leftPad(text,
					// text.length() + (subLevel + 1) * 5, "\t-   "));
					// inspect((subLevel + 1), type.value(), type.name());
					// }
					// }
				}
			}

			JsonSubTypes subtype = ((Class<?>) clazz).getAnnotation(JsonSubTypes.class);
			if (subtype != null) {
				int subLevel = level + 1;
				String text = "subtypes:";
				System.err.println(StringUtils.leftPad(text, text.length() + subLevel * 5, "\t-   "));
				for (JsonSubTypes.Type type : subtype.value()) {
					text = "subtype:" + type.name();
					System.err.println(StringUtils.leftPad(text, text.length() + (subLevel + 1) * 5, "\t-   "));
					inspect((subLevel + 1), type.value(), type.name());
				}
			}
		}
	}
}
