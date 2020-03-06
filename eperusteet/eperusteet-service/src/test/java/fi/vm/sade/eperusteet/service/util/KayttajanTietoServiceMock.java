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
package fi.vm.sade.eperusteet.service.util;

import fi.vm.sade.eperusteet.dto.kayttaja.KayttajanProjektitiedotDto;
import fi.vm.sade.eperusteet.dto.kayttaja.KayttajanTietoDto;
import fi.vm.sade.eperusteet.service.KayttajanTietoService;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

/**
 *
 * @author jhyoty
 */
@Service
@Profile("test")
public class KayttajanTietoServiceMock implements KayttajanTietoService {

    @Override
    public KayttajanTietoDto haeKirjautaunutKayttaja() {
        return hae(null);
    }

    @Override
    public KayttajanTietoDto hae(String oid) {
        return null;
    }

    @Override
    public Future<KayttajanTietoDto> haeAsync(String oid) {
        return new AsyncResult<>(null);
    }

    @Override
    public KayttajanProjektitiedotDto haePerusteprojekti(String oid, Long projektiId) {
        return null;
    }

    @Override
    public List<KayttajanProjektitiedotDto> haePerusteprojektit(String oid) {
        return Collections.emptyList();
    }

    @Override
    public List<KayttajanTietoDto> haeKayttajatiedot(List<String> oid) {
        return null;
    }
}
