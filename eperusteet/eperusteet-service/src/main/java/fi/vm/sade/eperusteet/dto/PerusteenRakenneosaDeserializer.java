package fi.vm.sade.eperusteet.dto;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;

public class PerusteenRakenneosaDeserializer extends JsonDeserializer<AbstractRakenneosaDto> {

	private static final Logger LOG = LoggerFactory.getLogger(PerusteenRakenneosaDeserializer.class);
	
	@SuppressWarnings("serial")
	private static final Map<String, Class<?>> propertyClassMap = new HashMap<String, Class<?>>() {{
		put("otsikko", LokalisoituTekstiDto.class);
		put("saannot", SaannostoDto.class);
		put("osat", List.class);
		put("_perusteenOsa", EntityReference.class);
	}};
	
	@Override
	public AbstractRakenneosaDto deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
		LOG.debug("{}", ctxt.getConfig().introspect(ctxt.constructType(RakenteenLehtiDto.class)).findProperties());
		
		ObjectMapper mapper = (ObjectMapper) jp.getCodec();
		LOG.debug("alku");
		if (jp.getCurrentToken() == JsonToken.START_OBJECT) {
			Boolean isLeaf = null;
			
			Map<String, Object> props = new HashMap<>();
			while (jp.nextToken() != JsonToken.END_OBJECT) {
				LOG.debug("{}", jp.getCurrentToken());
				String propKey = jp.getCurrentName();
				LOG.debug(propKey);
				if(jp.getCurrentToken().isStructStart()) {
					if("osat".equals(propKey)) {
						isLeaf = Boolean.FALSE;
					} else if("_perusteenOsa".equals(propKey)) {
						isLeaf = Boolean.TRUE;
					}
					
					Object prop = mapper.readValue(jp, propertyClassMap.get(propKey));
					LOG.debug("{}", prop);
					props.put(propKey, prop);
					
				} 
			}
			
			if(Boolean.TRUE.equals(isLeaf)) {
				RakenteenLehtiDto dto = new RakenteenLehtiDto();
				dto.setOtsikko((LokalisoituTekstiDto) props.get("otsikko"));
				dto.setSaannot((SaannostoDto) props.get("saannot"));
				dto.setPerusteenOsa((EntityReference) props.get("_perusteenOsa"));
				return dto;
			} else if(Boolean.FALSE.equals(isLeaf)) {
				RakenteenHaaraDto dto = new RakenteenHaaraDto();
				dto.setOtsikko((LokalisoituTekstiDto) props.get("otsikko"));
				dto.setSaannot((SaannostoDto) props.get("saannot"));
				dto.setOsat((List<AbstractRakenneosaDto>) props.get("osat"));
				return dto;
			}
		}
		return null;

	}

	
}
