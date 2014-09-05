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
package fi.vm.sade.eperusteet.resource.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaDto;
import fi.vm.sade.eperusteet.dto.util.PerusteenOsaUpdateDto;
import fi.vm.sade.eperusteet.dto.util.UpdateDto;
import java.io.IOException;

public class PerusteenOsaUpdateDtoDeserializer extends StdDeserializer<PerusteenOsaUpdateDto> {

    public PerusteenOsaUpdateDtoDeserializer() {
        super(PerusteenOsaUpdateDto.class);
    }

    @Override
    public PerusteenOsaUpdateDto deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException,
        JsonProcessingException {
        final TreeNode tree = jp.readValueAsTree();
        final ObjectCodec codec = jp.getCodec();
        PerusteenOsaUpdateDto dto = new PerusteenOsaUpdateDto();
        if (tree.get("metadata") != null) {
            dto.setMetadata(codec.treeToValue(tree.get("metadata"), UpdateDto.MetaData.class));
        }
        dto.setDto(codec.treeToValue(((ObjectNode)tree).without("metadata"), PerusteenOsaDto.class));
        return dto;
    }

}
