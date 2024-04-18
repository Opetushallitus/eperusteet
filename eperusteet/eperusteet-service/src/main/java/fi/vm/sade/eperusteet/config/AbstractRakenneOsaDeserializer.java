/*
 * Copyright (c) 2013 The Finnish Board of Education - Opetushallitus
 *
 * This program is free software: Licensed under the EUPL, Version 1.1 or - as
 * soon as they will be approved by the European Commission - subsequent versions
 * of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at: http://ec.europa.eu/idabc/eupl
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * European Union Public Licence for more details.
 */
package fi.vm.sade.eperusteet.config;

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
