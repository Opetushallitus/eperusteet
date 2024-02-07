package fi.vm.sade.eperusteet.resource.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.AbstractRakenneOsaDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.RakenneModuuliDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.RakenneOsaDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class AbstractRakenneOsaDeserializer extends StdDeserializer<AbstractRakenneOsaDto> {

    public AbstractRakenneOsaDeserializer() {
        super(AbstractRakenneOsaDto.class);
    }

    @Override
    public AbstractRakenneOsaDto deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        final TreeNode tree = jp.readValueAsTree();
        final ObjectCodec codec = jp.getCodec();
        TreeNode erikoisuus = tree.get("erikoisuus");
        TreeNode tosaviite = tree.get("_tutkinnonOsaViite");
        TreeNode osat = tree.get("osat");

        if (tosaviite != null || erikoisuus != null) {
            return codec.treeToValue(tree, RakenneOsaDto.class);
        }
        if (osat != null) {
            return codec.treeToValue(tree, RakenneModuuliDto.class);
        }
        throw new JsonMappingException(jp, "Tuntematon rakenneosan", jp.getCurrentLocation());
    }

    private static final Logger LOG = LoggerFactory.getLogger(AbstractRakenneOsaDeserializer.class);
}
