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

package fi.vm.sade.eperusteet.dto.peruste;

import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.KoodiDto;
import fi.vm.sade.eperusteet.dto.util.EntityReference;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author nkala
 */
@Getter
@Setter
public class KVLiiteDto {
    private Long id;
    private LokalisoituTekstiDto suorittaneenOsaaminen;
    private LokalisoituTekstiDto tyotehtavatJoissaVoiToimia;
    private LokalisoituTekstiDto tutkinnonVirallinenAsema;
    private LokalisoituTekstiDto tutkintotodistuksenAntaja;
    private EntityReference arvosanaAsteikko;
    private LokalisoituTekstiDto jatkoopintoKelpoisuus;
    private LokalisoituTekstiDto kansainvalisetSopimukset;
    private LokalisoituTekstiDto saadosPerusta;
    private LokalisoituTekstiDto pohjakoulutusvaatimukset;
    private LokalisoituTekstiDto lisatietoja;
    private LokalisoituTekstiDto tutkintotodistuksenSaaminen;
    private KoodiDto tutkinnonTaso;
    private LokalisoituTekstiDto tutkinnostaPaattavaViranomainen;
}
