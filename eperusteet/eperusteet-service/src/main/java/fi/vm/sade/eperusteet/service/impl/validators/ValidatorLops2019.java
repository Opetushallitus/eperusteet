package fi.vm.sade.eperusteet.service.impl.validators;

import fi.vm.sade.eperusteet.domain.*;
import fi.vm.sade.eperusteet.domain.lops2019.Koodillinen;
import fi.vm.sade.eperusteet.domain.lops2019.oppiaineet.Lops2019Oppiaine;
import fi.vm.sade.eperusteet.domain.lops2019.oppiaineet.moduuli.Lops2019Moduuli;
import fi.vm.sade.eperusteet.domain.yl.Nimetty;
import fi.vm.sade.eperusteet.dto.ValidointiKategoria;
import fi.vm.sade.eperusteet.dto.peruste.NavigationNodeDto;
import fi.vm.sade.eperusteet.dto.peruste.NavigationType;
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

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Stream;

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
        Perusteprojekti projekti = repository.findOne(perusteprojektiId);

        if (projekti == null) {
            throw new BusinessRuleViolationException("Projektia ei ole olemassa id:llä: " + perusteprojektiId);
        }

        ValidationHelper<Lops2019Oppiaine> oppiaineHelper = new ValidationHelper<>("oppiaine", lukioValidointi);
        ValidationHelper<Lops2019Moduuli> moduuliHelper = new ValidationHelper<>("moduuli", lukioValidointi);

        projekti.getPeruste().getLops2019Sisalto().getOppiaineet().stream()
                .map(oa -> Stream.concat(Stream.of(oa), oa.getOppimaarat().stream()))
                .flatMap(x -> x)
                .forEach(oa -> {
                    oppiaineHelper.add(oa, oppiaineNavigationNodeDto(oa));
                    validators.validKoodi(oa, lukioValidointi, oppiaineNavigationNodeDto(oa),"oppiaine", "oppiaineetjaoppimaaratlops2021");
                    oa.getModuulit()
                            .forEach(moduuli -> {
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

        return validoinnit;
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
