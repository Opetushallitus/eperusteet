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

import fi.vm.sade.eperusteet.domain.tutkinnonosa.TutkinnonOsaTyyppi;
import fi.vm.sade.eperusteet.dto.AmattitaitovaatimusKoodiDto;
import fi.vm.sade.eperusteet.dto.arviointi.ArvioinninKohdealueDto;
import fi.vm.sade.eperusteet.dto.arviointi.ArviointiDto;
import fi.vm.sade.eperusteet.dto.tutkinnonosa.OsaAlueDto;
import fi.vm.sade.eperusteet.dto.tutkinnonosa.OsaAlueKokonaanDto;
import fi.vm.sade.eperusteet.dto.tutkinnonosa.OsaamistavoiteLaajaDto;
import fi.vm.sade.eperusteet.dto.tutkinnonosa.TutkinnonOsaDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.TutkinnonOsaViiteDto;
import fi.vm.sade.eperusteet.dto.util.TutkinnonOsaViiteUpdateDto;
import fi.vm.sade.eperusteet.dto.util.UpdateDto;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.IfProfileValue;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@DirtiesContext
public class TutkinnonosaIT extends AbstractPerusteprojektiTest {

    @Before
    public void setup() {
        super.setup();
    }

    @Test
    @IfProfileValue(name = "db-it-tests")
    public void testOsaAlueArviointi() {

        // Luo tutkinnon osa
        TutkinnonOsaViiteDto viiteDto = uusiTutkinnonOsa(new TutkinnonOsaViiteDto());

        // Lukitaan tutkinnon osa
        tutkinnonOsaViiteService.lockPerusteenOsa(viiteDto.getId());

        // Luodaan muutos DTO
        TutkinnonOsaViiteUpdateDto tutkinnonOsaViiteUpdateDto = new TutkinnonOsaViiteUpdateDto();
        tutkinnonOsaViiteUpdateDto.setDto(viiteDto);
        tutkinnonOsaViiteUpdateDto.setMetadata(new UpdateDto.MetaData("muutos 1"));

        // Aseta tutkinnon osan sisältö
        TutkinnonOsaDto tutkinnonOsaDto = new TutkinnonOsaDto();
        tutkinnonOsaDto.setId(viiteDto.getTutkinnonOsa().getIdLong());
        tutkinnonOsaDto.setTyyppi(TutkinnonOsaTyyppi.REFORMI_TUTKE2);

        ArrayList<OsaAlueDto> osaAlueet = new ArrayList<>();
        osaAlueet.add(new OsaAlueDto());
        tutkinnonOsaDto.setOsaAlueet(osaAlueet);

        viiteDto.setTutkinnonOsaDto(tutkinnonOsaDto);

        // Päivitä tutkinnon osa ja luo osa-alue
        viiteDto = paivitaTutkinnonOsa(tutkinnonOsaViiteUpdateDto);


        // Aseta osa-alueelle arvoinnin kohdealue
        ArvioinninKohdealueDto arvioinninKohdealueDto = new ArvioinninKohdealueDto();

        ArrayList<ArvioinninKohdealueDto> arvioinninKohdealueet = new ArrayList<>();
        arvioinninKohdealueet.add(arvioinninKohdealueDto);

        ArviointiDto arviointiDto = new ArviointiDto();
        arviointiDto.setArvioinninKohdealueet(arvioinninKohdealueet);

        OsaamistavoiteLaajaDto osaamistavoiteLaajaDto = new OsaamistavoiteLaajaDto();
        osaamistavoiteLaajaDto.setArviointi(arviointiDto);

        ArrayList<OsaamistavoiteLaajaDto> osaamistavoitteet = new ArrayList<>();
        osaamistavoitteet.add(osaamistavoiteLaajaDto);

        OsaAlueKokonaanDto osaAlueDto = new OsaAlueKokonaanDto();
        osaAlueDto.setOsaamistavoitteet(osaamistavoitteet);

        // Päivitä osa-alue
        osaAlueDto = paivitaOsaAlue(viiteDto,
                viiteDto.getTutkinnonOsaDto().getOsaAlueet().get(0), osaAlueDto);

        // Tarkistetaan, että koodi on kunnossa
        assertThat(osaAlueDto.getOsaamistavoitteet()).hasSize(1);
        assertThat(osaAlueDto.getOsaamistavoitteet().get(0).getArviointi().getArvioinninKohdealueet()).hasSize(1);
        AmattitaitovaatimusKoodiDto koodi = osaAlueDto.getOsaamistavoitteet().get(0)
                .getArviointi().getArvioinninKohdealueet().stream()
                .map(ArvioinninKohdealueDto::getKoodi)
                .findAny()
                .orElse(null);

        assertThat(koodi).isNotNull();
        assertThat(koodi).hasNoNullFieldsOrProperties();

        // Päivitä osa-alue uudelleen
        osaAlueDto = paivitaOsaAlue(viiteDto,
                viiteDto.getTutkinnonOsaDto().getOsaAlueet().get(0), osaAlueDto);

        // Tarkistetaan, että koodi ei muutu
        assertThat(osaAlueDto.getOsaamistavoitteet()).hasSize(1);
        assertThat(osaAlueDto.getOsaamistavoitteet().get(0).getArviointi().getArvioinninKohdealueet()).hasSize(1);
        AmattitaitovaatimusKoodiDto uusiKoodi = osaAlueDto.getOsaamistavoitteet().get(0)
                .getArviointi().getArvioinninKohdealueet().stream()
                .map(ArvioinninKohdealueDto::getKoodi)
                .findAny()
                .orElse(null);

        assertThat(uusiKoodi).isNotNull();
        assertThat(uusiKoodi).hasNoNullFieldsOrProperties();

        assertThat(uusiKoodi.getId()).isNotEqualTo(koodi.getId());
        assertThat(uusiKoodi.getArvo()).isEqualTo(koodi.getArvo());

        // Vatautetaan tutkinnon osa
        tutkinnonOsaViiteService.unlockPerusteenOsa(viiteDto.getId());
    }

    @Test
    @IfProfileValue(name = "db-it-tests")
    public void testArviointi() {

        // Luo tutkinnon osa
        TutkinnonOsaViiteDto viiteDto = uusiTutkinnonOsa(new TutkinnonOsaViiteDto());

        // Lukitaan tutkinnon osa
        tutkinnonOsaViiteService.lockPerusteenOsa(viiteDto.getId());

        // Luodaan muutos DTO
        TutkinnonOsaViiteUpdateDto tutkinnonOsaViiteUpdateDto = new TutkinnonOsaViiteUpdateDto();
        tutkinnonOsaViiteUpdateDto.setDto(viiteDto);
        tutkinnonOsaViiteUpdateDto.setMetadata(new UpdateDto.MetaData("muutos 1"));

        TutkinnonOsaDto tutkinnonOsaDto = new TutkinnonOsaDto();
        tutkinnonOsaDto.setId(viiteDto.getTutkinnonOsa().getIdLong());
        tutkinnonOsaDto.setTyyppi(TutkinnonOsaTyyppi.NORMAALI);

        // Aseta arviointi ja sen kohdealueet
        ArvioinninKohdealueDto arvioinninKohdealueDto = new ArvioinninKohdealueDto();

        ArrayList<ArvioinninKohdealueDto> arvioinninKohdealueet = new ArrayList<>();
        arvioinninKohdealueet.add(arvioinninKohdealueDto);

        ArviointiDto arviointiDto = new ArviointiDto();
        arviointiDto.setArvioinninKohdealueet(arvioinninKohdealueet);

        tutkinnonOsaDto.setArviointi(arviointiDto);

        viiteDto.setTutkinnonOsaDto(tutkinnonOsaDto);

        // Päivitä tutkinnon osa ja luo osa-alue
        viiteDto = paivitaTutkinnonOsa(tutkinnonOsaViiteUpdateDto);

        // Tarkistetaan, että koodi on kunnossa
        assertThat(viiteDto.getTutkinnonOsaDto().getArviointi().getArvioinninKohdealueet()).hasSize(1);
        AmattitaitovaatimusKoodiDto koodi = viiteDto.getTutkinnonOsaDto()
                .getArviointi().getArvioinninKohdealueet().stream()
                .map(ArvioinninKohdealueDto::getKoodi)
                .findAny()
                .orElse(null);

        assertThat(koodi).isNotNull();
        assertThat(koodi).hasNoNullFieldsOrProperties();

        // Päivitä tutkinnon osa uudelleen
        viiteDto = paivitaTutkinnonOsa(tutkinnonOsaViiteUpdateDto);

        // Tarkistetaan, että koodi ei muutu
        assertThat(viiteDto.getTutkinnonOsaDto().getArviointi().getArvioinninKohdealueet()).hasSize(1);
        AmattitaitovaatimusKoodiDto uusiKoodi = viiteDto.getTutkinnonOsaDto()
                .getArviointi().getArvioinninKohdealueet().stream()
                .map(ArvioinninKohdealueDto::getKoodi)
                .findAny()
                .orElse(null);

        assertThat(uusiKoodi).isNotNull();
        assertThat(uusiKoodi).hasNoNullFieldsOrProperties();

        assertThat(uusiKoodi.getId()).isNotEqualTo(koodi.getId());
        assertThat(uusiKoodi.getArvo()).isEqualTo(koodi.getArvo());

        // Vatautetaan tutkinnon osa
        tutkinnonOsaViiteService.unlockPerusteenOsa(viiteDto.getId());
    }
}
