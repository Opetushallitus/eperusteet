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

import fi.vm.sade.eperusteet.dto.koodisto.*;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.KoodiDto;
import fi.vm.sade.eperusteet.service.KoodistoClient;
import static fi.vm.sade.eperusteet.service.test.util.TestUtils.lt;
import static fi.vm.sade.eperusteet.service.test.util.TestUtils.uniikkiString;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 *
 * @author nkala
 */
@Service
@Profile("test")
public class KoodistoClientMock implements KoodistoClient {

    @Override
    public KoodistoKoodiDto get(String koodisto, String koodi) {
        KoodistoKoodiDto result = new KoodistoKoodiDto();
        KoodistoDto koodistoDto = new KoodistoDto();
        koodistoDto.setKoodistoUri(koodisto);
        result.setKoodisto(koodistoDto);
        result.setKoodiUri(koodi);
        return result;
    }

    @Override
    public KoodistoKoodiLaajaDto getAllByVersio(String koodi, String versio) {
        KoodistoKoodiLaajaDto result = new KoodistoKoodiLaajaDto();
        result.setKoodiArvo(koodi);
        result.setVersio(versio);

        // Lisätään nqf koodi
        KoodiElementti[] elementit = new KoodiElementti[1];
        KoodiElementti elementti = new KoodiElementti();

        elementti.setCodeElementUri("nqf_4");
        elementti.setCodeElementValue("4");
        elementti.setCodeElementVersion(1L);
        elementti.setPassive(false);

        KoodistoMetadataDto[] parentMetadatas = new KoodistoMetadataDto[3];
        parentMetadatas[0] = KoodistoMetadataDto.of("nqf", "SV", "Nationell referensram för examina (nqf)");
        parentMetadatas[1] = KoodistoMetadataDto.of("nqf", "EN", "National Qualifications Framework (nqf)");
        parentMetadatas[2] = KoodistoMetadataDto.of("nqf", "FI", "Kansallinen tutkintojen viitekehys (nqf)");
        elementti.setParentMetadata(parentMetadatas);

        elementit[0] = elementti;
        result.setIncludesCodeElements(elementit);
        return result;
    }

    @Override
    public KoodistoKoodiDto getLatest(String koodi) {
        KoodistoKoodiDto result = get("", koodi);
        result.setVersio("1");
        return result;
    }

    @Override
    public KoodistoKoodiDto get(String koodistoUri, String koodiUri, Long versio) {
        KoodistoKoodiDto result = get(koodistoUri, koodiUri);
        result.setVersio(versio.toString());
        return result;
    }

    @Override
    public KoodiDto getKoodi(String koodisto, String koodiUri) {
        KoodiDto result = new KoodiDto();
        result.setKoodisto(koodisto);
        result.setUri(koodiUri);
        result.setNimi(lt(uniikkiString()).asMap());
        return result;
    }

    @Override
    public KoodiDto getKoodi(String koodisto, String koodiUri, Long versio) {
        KoodiDto result = getKoodi(koodisto, koodiUri);
        result.setVersio(versio);
        return result;
    }

    @Override
    public void addNimiAndUri(KoodiDto koodi) {
        koodi.setNimi(lt(uniikkiString()).asMap());
    }

    @Override
    public List<KoodistoKoodiDto> getAll(String koodisto) {
        return Collections.emptyList();
    }

    @Override
    public Stream<KoodistoKoodiDto> filterBy(String koodisto, String haku) {
        return Stream.empty();
    }

    @Override
    public List<KoodistoKoodiDto> getAlarelaatio(String koodi) {
        return Collections.emptyList();
    }

    @Override
    public List<KoodistoKoodiDto> getYlarelaatio(String koodi) {
        return Collections.emptyList();
    }

    @Override
    public List<KoodistoKoodiDto> getRinnasteiset(String koodi) {
        return Collections.emptyList();
    }

}
