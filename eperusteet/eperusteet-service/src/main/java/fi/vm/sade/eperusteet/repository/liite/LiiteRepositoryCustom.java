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
package fi.vm.sade.eperusteet.repository.liite;

import fi.vm.sade.eperusteet.domain.liite.Liite;
import fi.vm.sade.eperusteet.domain.liite.LiiteTyyppi;

import java.io.InputStream;
import java.util.UUID;

/**
 *
 * @author jhyoty
 */
public interface LiiteRepositoryCustom {
    Liite add(LiiteTyyppi tyyppi, String mime, String nimi, long length, InputStream is);
    Liite add(LiiteTyyppi tyyppi, String mime, String nimi, byte[] bytes);
    Liite add(UUID uuid, LiiteTyyppi tyyppi, String mime, String nimi, byte[] bytes);
}
