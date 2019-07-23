package fi.vm.sade.eperusteet.service.impl.validators;

import fi.vm.sade.eperusteet.domain.*;
import fi.vm.sade.eperusteet.domain.lops2019.Koodillinen;
import fi.vm.sade.eperusteet.domain.lops2019.oppiaineet.Lops2019Oppiaine;
import fi.vm.sade.eperusteet.domain.lops2019.oppiaineet.moduuli.Lops2019Moduuli;
import fi.vm.sade.eperusteet.domain.yl.Nimetty;
import fi.vm.sade.eperusteet.dto.TilaUpdateStatus;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.repository.PerusteprojektiRepository;
import fi.vm.sade.eperusteet.service.KoodistoClient;
import fi.vm.sade.eperusteet.service.PerusteService;
import fi.vm.sade.eperusteet.service.TutkintonimikeKoodiService;
import fi.vm.sade.eperusteet.service.Validator;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
        private TilaUpdateStatus status;
        private String kohde;
        private Map<String, T> osat = new HashMap<>();

        public ValidationHelper(String kohde, TilaUpdateStatus status) {
            this.kohde = kohde;
            this.status = status;
        }

        void add(T koodillinen) {
            if (koodillinen.getKoodi() != null && koodillinen.getKoodi().getUri() != null) {
                if (osat.containsKey(koodillinen.getKoodi().getUri())) {
                    List<LokalisoituTekstiDto> nimet = new ArrayList<>();
                    nimet.add(mapper.map(koodillinen.getNimi(), LokalisoituTekstiDto.class));
                    nimet.add(mapper.map(osat.get(koodillinen.getKoodi().getUri()).getNimi(), LokalisoituTekstiDto.class));
                    status.addStatus(
                            kohde + "-koodi-moneen-kertaan",
                            Suoritustapakoodi.LUKIOKOULUTUS2019,
                            nimet);
                    status.setVaihtoOk(false);
                }
                else {
                    osat.put(koodillinen.getKoodi().getUri(), koodillinen);
                }
            }
        }
    }

    @Override
    public TilaUpdateStatus validate(Long perusteprojektiId, ProjektiTila targetTila) {
        TilaUpdateStatus status = new TilaUpdateStatus();
        status.setVaihtoOk(true);

        Perusteprojekti projekti = repository.findOne(perusteprojektiId);

        if (projekti == null) {
            throw new BusinessRuleViolationException("Projektia ei ole olemassa id:llä: " + perusteprojektiId);
        }

        ValidationHelper<Lops2019Oppiaine> oppiaineHelper = new ValidationHelper<>("oppiaine", status);
        ValidationHelper<Lops2019Moduuli> moduuliHelper = new ValidationHelper<>("moduuli", status);

        projekti.getPeruste().getLops2019Sisalto().getOppiaineet().stream()
                .map(oa -> Stream.concat(Stream.of(oa), oa.getOppimaarat().stream()))
                .flatMap(x -> x)
                .forEach(oa -> {
                    oppiaineHelper.add(oa);
                    validators.validKoodi(oa, status, "oppiaine", "oppiaineetjaoppimaaratlops2021");
                    oa.getModuulit()
                            .forEach(moduuli -> {
                                moduuliHelper.add(moduuli);
                                validators.validKoodi(moduuli, status, "moduuli", "moduulikoodistolops2021");
                            });
                });

        return status;
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
