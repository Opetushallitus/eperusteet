package fi.vm.sade.eperusteet.service.impl.validators;

import fi.vm.sade.eperusteet.domain.*;
import fi.vm.sade.eperusteet.domain.lops2019.Koodillinen;
import fi.vm.sade.eperusteet.domain.lops2019.oppiaineet.Lops2019Arviointi;
import fi.vm.sade.eperusteet.domain.lops2019.oppiaineet.Lops2019Oppiaine;
import fi.vm.sade.eperusteet.domain.lops2019.oppiaineet.Lops2019OppiaineLaajaAlainenOsaaminen;
import fi.vm.sade.eperusteet.domain.lops2019.oppiaineet.Lops2019OppiaineTavoitealue;
import fi.vm.sade.eperusteet.domain.lops2019.oppiaineet.Lops2019OppiaineTavoitteet;
import fi.vm.sade.eperusteet.domain.lops2019.oppiaineet.Lops2019Tehtava;
import fi.vm.sade.eperusteet.domain.lops2019.oppiaineet.moduuli.Lops2019Moduuli;
import fi.vm.sade.eperusteet.domain.lops2019.oppiaineet.moduuli.Lops2019ModuuliTavoite;
import fi.vm.sade.eperusteet.domain.yl.Nimetty;
import fi.vm.sade.eperusteet.dto.ValidointiKategoria;
import fi.vm.sade.eperusteet.dto.peruste.NavigationNodeDto;
import fi.vm.sade.eperusteet.dto.peruste.NavigationType;
import fi.vm.sade.eperusteet.dto.tutkinnonosa.TutkinnonOsaDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.KoodiDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.repository.PerusteprojektiRepository;
import fi.vm.sade.eperusteet.service.KoodistoClient;
import fi.vm.sade.eperusteet.service.PerusteService;
import fi.vm.sade.eperusteet.service.TutkintonimikeKoodiService;
import fi.vm.sade.eperusteet.service.Validator;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.service.util.Pair;
import fi.vm.sade.eperusteet.service.util.Validointi;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Stream;

import static fi.vm.sade.eperusteet.domain.TekstiPalanen.tarkistaTekstipalanen;

@Component
@Slf4j
@Transactional
public class ValidatorLops2019 implements Validator {

    @Autowired
    @Dto
    private DtoMapper mapper;

    @Autowired
    private PerusteprojektiRepository repository;

    @Autowired
    private PerusteService perusteService;

    @Autowired
    private TutkintonimikeKoodiService tutkintonimikeKoodiService;

    @Autowired
    private KoodistoClient koodistoService;

    @Autowired
    private Validators validators;

    @Getter
    class ValidationHelper<T extends Koodillinen & Nimetty> {
        private Validointi validointi;
        private String kohde;
        private Map<String, Pair<T, NavigationNodeDto>> osat = new HashMap<>();

        public ValidationHelper(String kohde, Validointi validointi) {
            this.kohde = kohde;
            this.validointi = validointi;
        }

        void add(T koodillinen, NavigationNodeDto navigationNodeDto) {
            if (koodillinen.getKoodi() != null && koodillinen.getKoodi().getUri() != null) {
                if (osat.containsKey(koodillinen.getKoodi().getUri())) {
                    validointi.virhe(kohde + "-koodi-moneen-kertaan", navigationNodeDto);
                    validointi.virhe(kohde + "-koodi-moneen-kertaan", osat.get(koodillinen.getKoodi().getUri()).getSecond());
                }
                else {
                    osat.put(koodillinen.getKoodi().getUri(), Pair.of(koodillinen, navigationNodeDto));
                }
            }
        }
    }

    @Override
    public List<Validointi> validate(Long perusteprojektiId, ProjektiTila targetTila) {
        List<Validointi> validoinnit = new ArrayList<>();

        Validointi lukioValidointi = new Validointi(ValidointiKategoria.RAKENNE);
        Validointi kieliValidointi = new Validointi(ValidointiKategoria.KIELISISALTO);
        Perusteprojekti projekti = repository.findById(perusteprojektiId).orElse(null);

        if (projekti == null) {
            throw new BusinessRuleViolationException("Projektia ei ole olemassa id:llä: " + perusteprojektiId);
        }

        ValidationHelper<Lops2019Oppiaine> oppiaineHelper = new ValidationHelper<>("oppiaine", lukioValidointi);
        ValidationHelper<Lops2019Moduuli> moduuliHelper = new ValidationHelper<>("moduuli", lukioValidointi);

        projekti.getPeruste().getLops2019Sisalto().getOppiaineet().stream()
                .map(oa -> Stream.concat(Stream.of(oa), oa.getOppimaarat().stream()))
                .flatMap(x -> x)
                .forEach(oa -> {
                    validateOppiaineSisalto(oa, projekti.getPeruste().getKielet(), kieliValidointi);

                    oppiaineHelper.add(oa, oppiaineNavigationNodeDto(oa));
                    validators.validKoodi(oa, lukioValidointi, oppiaineNavigationNodeDto(oa),"oppiaine", "oppiaineetjaoppimaaratlops2021");
                    oa.getModuulit()
                            .forEach(moduuli -> {
                                validateModuuliSisalto(moduuli, projekti.getPeruste().getKielet(), kieliValidointi);

                                moduuliHelper.add(moduuli, moduuliNavigationNodeDto(moduuli));
                                validators.validKoodi(moduuli, lukioValidointi, moduuliNavigationNodeDto(moduuli),  "moduuli", "moduulikoodistolops2021");
                                BigDecimal laajuus = moduuli.getLaajuus();
                                if (laajuus == null
                                        || laajuus.compareTo(BigDecimal.valueOf(1)) < 0
                                        || laajuus.compareTo(BigDecimal.valueOf(4)) > 0) {
                                    // Jos laajuutta ei ole määritetty tai se ei ole välillä 1-4
                                    lukioValidointi.virhe("moduuli-laajuus-puuttuu", moduuliNavigationNodeDto(moduuli));
                                }
                            });
                });

       validoinnit.add(lukioValidointi);
       validoinnit.add(kieliValidointi);
       return validoinnit;
    }

    private void validateOppiaineSisalto(Lops2019Oppiaine oppiaine, Set<Kieli> vaaditutKielet, Validointi validointi) {
        Map<String, String> virheellisetKielet = new HashMap<>();
        tarkistaTekstipalanen("peruste-validointi-oppiaine-sisalto",
                Optional.ofNullable(oppiaine).map(Lops2019Oppiaine::getTehtava).map(Lops2019Tehtava::getKuvaus).orElse(null), vaaditutKielet, virheellisetKielet);
        tarkistaTekstipalanen("peruste-validointi-oppiaine-sisalto",
                Optional.ofNullable(oppiaine).map(Lops2019Oppiaine::getArviointi).map(Lops2019Arviointi::getKuvaus).orElse(null), vaaditutKielet, virheellisetKielet);
        tarkistaTekstipalanen("peruste-validointi-oppiaine-sisalto",
                Optional.ofNullable(oppiaine).map(Lops2019Oppiaine::getTavoitteet).map(Lops2019OppiaineTavoitteet::getKuvaus).orElse(null), vaaditutKielet, virheellisetKielet);
        tarkistaTekstipalanen("peruste-validointi-oppiaine-sisalto",
                Optional.ofNullable(oppiaine).map(Lops2019Oppiaine::getLaajaAlaisetOsaamiset).map(Lops2019OppiaineLaajaAlainenOsaaminen::getKuvaus).orElse(null), vaaditutKielet, virheellisetKielet);
        tarkistaTekstipalanen("peruste-validointi-oppiaine-sisalto",
                oppiaine.getPakollisetModuulitKuvaus(), vaaditutKielet, virheellisetKielet);
        tarkistaTekstipalanen("peruste-validointi-oppiaine-sisalto",
                oppiaine.getValinnaisetModuulitKuvaus(), vaaditutKielet, virheellisetKielet);

        if (!ObjectUtils.isEmpty(oppiaine.getTavoitteet())) {
          oppiaine.getTavoitteet().getTavoitealueet().forEach(ta -> {
              tarkistaTekstipalanen("peruste-validointi-oppiaine-sisalto",
                      ta.getNimi(), vaaditutKielet, virheellisetKielet);
              tarkistaTekstipalanen("peruste-validointi-oppiaine-sisalto",
                      ta.getKohde(), vaaditutKielet, virheellisetKielet);
              Optional.ofNullable(ta).map(Lops2019OppiaineTavoitealue::getTavoitteet).orElse(null).forEach(tavoite -> {
                  tarkistaTekstipalanen("peruste-validointi-oppiaine-sisalto",
                          tavoite, vaaditutKielet, virheellisetKielet);
              });
          });    
        }

        for (Map.Entry<String, String> entry : virheellisetKielet.entrySet()) {
            validointi.virhe(entry.getKey(), oppiaineNavigationNodeDto(oppiaine));
        }
    }

    private void validateModuuliSisalto(Lops2019Moduuli moduuli, Set<Kieli> vaaditutKielet, Validointi validointi) {
        Map<String, String> virheellisetKielet = new HashMap<>();
        tarkistaTekstipalanen("peruste-validointi-moduuli-sisalto",
                moduuli.getKuvaus(), vaaditutKielet, virheellisetKielet);

        tarkistaTekstipalanen("peruste-validointi-moduuli-sisalto",
                Optional.ofNullable(moduuli).map(Lops2019Moduuli::getTavoitteet).map(Lops2019ModuuliTavoite::getKohde).orElse(null), vaaditutKielet, virheellisetKielet);
        if (!ObjectUtils.isEmpty(moduuli.getTavoitteet().getTavoitteet())) {
            moduuli.getTavoitteet().getTavoitteet().forEach(tavoite -> {
                tarkistaTekstipalanen("peruste-validointi-moduuli-sisalto",
                        tavoite, vaaditutKielet, virheellisetKielet);
            });
        }

        if (!ObjectUtils.isEmpty(moduuli.getSisallot())) {
            moduuli.getSisallot().forEach(sisalto -> {
                tarkistaTekstipalanen("peruste-validointi-moduuli-sisalto",
                        sisalto.getKohde(), vaaditutKielet, virheellisetKielet);

                if (!ObjectUtils.isEmpty(sisalto.getSisallot())) {
                    sisalto.getSisallot().forEach(alaSisalto -> {
                        tarkistaTekstipalanen("peruste-validointi-moduuli-sisalto",
                                alaSisalto, vaaditutKielet, virheellisetKielet);
                    });
                }
            });
        }


        for (Map.Entry<String, String> entry : virheellisetKielet.entrySet()) {
            validointi.virhe(entry.getKey(), moduuliNavigationNodeDto(moduuli));
        }
    }

    private NavigationNodeDto oppiaineNavigationNodeDto(Lops2019Oppiaine oa) {
        LokalisoituTekstiDto nimi = oa.getKoodi() != null ? mapper.map(oa.getKoodi(), KoodiDto.class).getNimi() : mapper.map(oa.getNimi(), LokalisoituTekstiDto.class);
        return NavigationNodeDto
                .of(NavigationType.oppiaine, nimi, oa.getId())
                .koodi(mapper.map(oa.getKoodi(), KoodiDto.class))
                .meta("koodi", mapper.map(oa.getKoodi(), KoodiDto.class));
    }

    private NavigationNodeDto moduuliNavigationNodeDto(Lops2019Moduuli m) {
        return NavigationNodeDto.of(
                        NavigationType.moduuli,
                        mapper.map(m.getNimi(), LokalisoituTekstiDto.class),
                        m.getId())
                .koodi(mapper.map(m.getKoodi(), fi.vm.sade.eperusteet.dto.tutkinnonrakenne.KoodiDto.class))
                .meta("oppiaine", m.getOppiaine() != null ? m.getOppiaine().getId() : null)
                .meta("koodi", mapper.map(m.getKoodi(), KoodiDto.class))
                .meta("pakollinen", m.getPakollinen());
    }

    @Override
    public boolean applicableKoulutustyyppi(KoulutusTyyppi tyyppi) {
        return KoulutusTyyppi.LUKIOKOULUTUS.equals(tyyppi);
    }

    @Override
    public boolean applicableToteutus(KoulutustyyppiToteutus toteutus) {
        return KoulutustyyppiToteutus.LOPS2019.equals(toteutus);
    }
}
