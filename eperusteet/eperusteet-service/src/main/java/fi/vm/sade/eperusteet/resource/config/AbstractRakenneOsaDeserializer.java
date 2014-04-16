package fi.vm.sade.eperusteet.resource.config;

import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.AbstractRakenneOsaDto;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.RakenneModuuliDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.RakenneOsaDto;

public class AbstractRakenneOsaDeserializer extends StdDeserializer<AbstractRakenneOsaDto> {

    public AbstractRakenneOsaDeserializer() {
        super(AbstractRakenneOsaDto.class);
    }

    @Override
    public AbstractRakenneOsaDto deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException,
        JsonProcessingException {
        final TreeNode tree = jp.readValueAsTree();
        final ObjectCodec codec = jp.getCodec();
        if (tree.get("_tutkinnonOsa") != null) {
            return codec.treeToValue(tree, RakenneOsaDto.class);
        }
        if (tree.get("osat") != null) {
            return codec.treeToValue(tree, RakenneModuuliDto.class);
        }
        throw new JsonMappingException("Tuntematon rakenneosan", jp.getCurrentLocation());
    }

    private static final Logger LOG = LoggerFactory.getLogger(AbstractRakenneOsaDeserializer.class);
}
