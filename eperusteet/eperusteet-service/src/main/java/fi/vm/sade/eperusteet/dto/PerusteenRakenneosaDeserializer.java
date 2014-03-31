package fi.vm.sade.eperusteet.dto;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;

public class PerusteenRakenneosaDeserializer extends JsonDeserializer<AbstractRakenneosaDto> {

	private static final Logger LOG = LoggerFactory.getLogger(PerusteenRakenneosaDeserializer.class);

	@Override
	public AbstractRakenneosaDto deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
		Map<String, BeanPropertyDefinition> definitionMap = getPropertyDefinitionsFor(RakenteenLehtiDto.class, ctxt);
		ObjectMapper mapper = (ObjectMapper) jp.getCodec();
		LOG.debug("start parsing");

		if (jp.getCurrentToken() == JsonToken.START_OBJECT) {
			Boolean isLeaf = Boolean.TRUE;

			Map<String, Object> props = new HashMap<>();
			while (jp.nextToken() != JsonToken.END_OBJECT) {
				LOG.debug("token: {}", jp.getCurrentToken());
				String propKey = jp.getCurrentName();
				LOG.debug("prop name: {}", propKey);
				if (jp.getCurrentToken().isStructStart()) {
					if (!definitionMap.containsKey(propKey)) {
						definitionMap = getPropertyDefinitionsFor(RakenteenHaaraDto.class, ctxt);
						isLeaf = Boolean.FALSE;
					}
					LOG.debug("field class: {}", definitionMap.get(propKey).getField().getRawType());
					Object prop = mapper.readValue(jp, definitionMap.get(propKey).getField().getRawType());
					LOG.debug("found prop: {}", prop);
					props.put(propKey, prop);

				}
			}
			AbstractRakenneosaDto completeDto = null;
			if (Boolean.TRUE.equals(isLeaf)) {
				completeDto = new RakenteenLehtiDto();

			} else if (Boolean.FALSE.equals(isLeaf)) {
				completeDto = new RakenteenHaaraDto();
			}

			for (Entry<String, Object> entry : props.entrySet()) {
				LOG.debug("field: {}, field value: {}, annotated method: {}", entry.getKey(), entry.getValue(), definitionMap.get(entry.getKey()).getSetter());
				definitionMap.get(entry.getKey()).getSetter().setValue(completeDto, entry.getValue());
			}

			return completeDto;
		}
		return null;
	}

	private Map<String, BeanPropertyDefinition> getPropertyDefinitionsFor(Class<?> beanClass, DeserializationContext ctxt) {
		Map<String, BeanPropertyDefinition> definitionMap = new HashMap<>();
		List<BeanPropertyDefinition> propertyDefinitions = ctxt.getConfig().introspect(ctxt.constructType(beanClass)).findProperties();
		for (BeanPropertyDefinition def : propertyDefinitions) {
			LOG.debug("name: {}, setter: {}", def.getName(), def.getSetter());
			definitionMap.put(def.getName(), def);
		}

		return definitionMap;
	}
}
