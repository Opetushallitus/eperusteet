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

import fi.vm.sade.eperusteet.dto.KommenttiDto;
import java.util.List;

/**
 *
 * @author nkala
 */
public interface KommenttiService {
    public List<KommenttiDto> getAllByPerusteenOsa(Long id, Long perusteeonOsaId);
    public List<KommenttiDto> getAllByPerusteenOsa(Long perusteenOsaId);
    public List<KommenttiDto> getAllBySuoritustapa(Long id, String suoritustapa);
    public List<KommenttiDto> getAllByPerusteprojekti(Long id);
    public List<KommenttiDto> getAllByParent(Long id);
    public List<KommenttiDto> getAllByYlin(Long id);
    public KommenttiDto get(Long kommenttiId);
    public KommenttiDto add(final KommenttiDto kommenttidto);
    public KommenttiDto update(Long kommenttiId, final KommenttiDto kommenttidto);
    public void delete(Long kommenttiId);
    public void deleteReally(Long kommenttiId);
}
