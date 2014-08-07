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

package fi.vm.sade.eperusteet.service;

import com.google.code.docbook4j.Docbook4JException;
import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.dto.DokumenttiDto;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

/**
 *
 * @author jussini
 */
public interface DokumenttiService {

    public byte[] generateFor(DokumenttiDto dto) throws IOException,
            TransformerException, ParserConfigurationException,
            Docbook4JException;

    public void setStarted(DokumenttiDto dto);
    public void generateWithDto(DokumenttiDto dto);
    public DokumenttiDto createDtoFor(final long id, Kieli kieli);

    public byte[] get(Long id);
    public DokumenttiDto query(Long id);
    public DokumenttiDto findLatest(Long id, Kieli kieli);
}
