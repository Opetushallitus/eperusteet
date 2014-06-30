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

import fi.vm.sade.eperusteet.domain.Tila;
import fi.vm.sade.eperusteet.dto.TilaUpdateStatus;
import fi.vm.sade.eperusteet.dto.PerusteprojektiDto;
import fi.vm.sade.eperusteet.dto.PerusteprojektiLuontiDto;
import java.util.Set;

/**
 *
 * @author harrik
 */
public interface PerusteprojektiService {
    
    PerusteprojektiDto get(final Long id);
    
    PerusteprojektiDto save(PerusteprojektiLuontiDto perusteprojektiDto);
    
    PerusteprojektiDto update(final Long id, PerusteprojektiDto perusteprojektiDto);
    
    Set<Tila> getTilat(final Long id);
    
    TilaUpdateStatus updateTila(final Long id, Tila tila);
}
