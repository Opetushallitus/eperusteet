package fi.vm.sade.eperusteet.service.util;

import fi.vm.sade.eperusteet.domain.KoodiRelaatioTyyppi;
import fi.vm.sade.eperusteet.dto.koodisto.*;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.KoodiDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.service.KoodistoClient;
import fi.vm.sade.eperusteet.utils.client.OphClientHelper;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

import static fi.vm.sade.eperusteet.service.test.util.TestUtils.createMockAmmattitaitovaatimuksetKoodistoKoodit;
import static fi.vm.sade.eperusteet.service.test.util.TestUtils.lt;
import static fi.vm.sade.eperusteet.service.test.util.TestUtils.uniikkiString;

@Slf4j
@Service
@Profile({"test", "docker"})
public class KoodistoClientMock implements KoodistoClient {

    @Autowired
    OphClientHelper mockedOphClientHelper;

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

        // Lisätään nqf, eqf, isced koodit
        List<KoodiElementti> koodiElementit = Arrays.asList("nqf_4", "eqf_1", "isced2011koulutusastetaso1_5").stream().map(code -> {
            KoodiElementti elementti = new KoodiElementti();
            elementti.setCodeElementUri(code);
            elementti.setCodeElementValue("4");
            elementti.setCodeElementVersion(1L);
            elementti.setPassive(false);

            KoodistoMetadataDto[] parentMetadatas = new KoodistoMetadataDto[3];
            parentMetadatas[0] = KoodistoMetadataDto.of(code, "SV", "Nationell referensram för examina (" + code + ")");
            parentMetadatas[1] = KoodistoMetadataDto.of(code, "EN", "National Qualifications Framework (" + code + ")");
            parentMetadatas[2] = KoodistoMetadataDto.of(code, "FI", "Kansallinen tutkintojen viitekehys (" + code + ")");
            elementti.setParentMetadata(parentMetadatas);
            return elementti;
        }).collect(Collectors.toList());

        KoodiElementti[] elementit = new KoodiElementti[koodiElementit.size()];
        result.setIncludesCodeElements(koodiElementit.toArray(elementit));
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
        result.setNimi(lt(uniikkiString()));
        return result;
    }

    @Override
    public KoodiDto getKoodi(String koodisto, String koodiUri, Long versio) {
        KoodiDto result = getKoodi(koodisto, koodiUri);
        result.setVersio(versio);
        return result;
    }

    @Override
    public void addNimiAndArvo(KoodiDto koodi) {
        if (koodi != null) {
            koodi.setNimi(lt(uniikkiString()));
            if (koodi.getUri() != null) {
                String[] s = koodi.getUri().split("_");
                koodi.setArvo(s[s.length - 1]);
            }
        }
    }

    @Override
    public List<KoodistoKoodiDto> getAll(String koodisto) {
        return Optional.ofNullable(createMockAmmattitaitovaatimuksetKoodistoKoodit().get(koodisto)).orElse(Collections.emptyList());
    }

    @Override
    public List<KoodistoKoodiDto> filterBy(String koodisto, String haku) {
        return Collections.emptyList();
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

    @Override
    public KoodistoKoodiDto addKoodi(KoodistoKoodiDto koodi) {
        return koodi;
    }

    @Override
    public KoodistoKoodiDto updateKoodi(KoodistoKoodiDto koodi) {
        return koodi;
    }

    @Override
    public KoodistoKoodiDto addKoodiNimella(String koodistonimi, LokalisoituTekstiDto koodinimi) {
        return null;
    }

    @Override
    public KoodistoKoodiDto addKoodiNimellaPakotaUusiKoodiArvo(String koodistonimi, LokalisoituTekstiDto koodinimi) {
        return null;
    }

    @Override
    public KoodistoKoodiDto addKoodiNimella(String koodistonimi, LokalisoituTekstiDto koodinimi, int koodiArvoLength) {
        return null;
    }

    @Override
    public KoodistoKoodiDto addKoodiNimella(String koodistonimi, LokalisoituTekstiDto koodinimi, long seuraavaKoodi) {
        return KoodistoKoodiDto.builder()
                .koodisto(KoodistoDto.of(koodistonimi))
                .koodiUri(koodistonimi + "_" + seuraavaKoodi)
                .build();
    }

    @Override
    public long nextKoodiId(String koodistonimi) {
        return 0;
    }

    @Override
    public Collection<Long> nextKoodiId(String koodistonimi, int count) {
        return IntStream.range(0, count).boxed().map(operand -> new Long(operand)).collect(Collectors.toList());
    }

    @Override
    public Collection<Long> nextKoodiId(String koodistonimi, int count, int koodiArvoLength) {
        return IntStream.range(0, count).boxed().map(operand -> new Long(operand)).collect(Collectors.toList());
    }

    @Override
    public void addKoodirelaatio(String parentKoodi, String lapsiKoodi, KoodiRelaatioTyyppi koodiRelaatioTyyppi) {
        log.debug("koodirelaatio" + parentKoodi + lapsiKoodi);
        mockedOphClientHelper.post("", "koodirelaatio" + parentKoodi + lapsiKoodi);
    }

    @Override
    public void addKoodirelaatiot(String parentKoodi, List<String> lapsiKoodit, KoodiRelaatioTyyppi koodiRelaatioTyyppi) {
        log.debug("koodirelaatio" + parentKoodi + lapsiKoodit);
        mockedOphClientHelper.post("", "koodirelaatio" + parentKoodi + lapsiKoodit);
    }

    @Override
    public void addKoodistoRelaatio(String parentKoodi, String lapsiKoodi, KoodiRelaatioTyyppi koodiRelaatioTyyppi) {
        log.debug("koodistorelaatio" + parentKoodi + lapsiKoodi);
        mockedOphClientHelper.post("", "koodistorelaatio" + parentKoodi + lapsiKoodi);
    }
}
