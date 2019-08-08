package fi.vm.sade.eperusteet.service.impl;

import com.google.common.collect.Lists;
import fi.vm.sade.eperusteet.domain.*;
import fi.vm.sade.eperusteet.domain.tutkinnonosa.Ammattitaitovaatimukset2019;
import fi.vm.sade.eperusteet.domain.tutkinnonosa.Ammattitaitovaatimus2019;
import fi.vm.sade.eperusteet.domain.tutkinnonosa.Ammattitaitovaatimus2019Kohdealue;
import fi.vm.sade.eperusteet.domain.tutkinnonosa.TutkinnonOsa;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.TutkinnonOsaViite;
import fi.vm.sade.eperusteet.dto.TilaUpdateStatus;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.repository.PerusteprojektiRepository;
import fi.vm.sade.eperusteet.service.KoodistoClient;
import fi.vm.sade.eperusteet.service.PerusteService;
import fi.vm.sade.eperusteet.service.TutkintonimikeKoodiService;
import fi.vm.sade.eperusteet.service.Validator;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@Transactional
@Slf4j
public class ValidatorAmmattitaitovaatimukset implements Validator {

    @Autowired
    @Dto
    private DtoMapper mapper;

    @Autowired
    private PerusteprojektiRepository perusteprojektiRepository;

    @Autowired
    private PerusteService perusteService;

    @Autowired
    private TutkintonimikeKoodiService tutkintonimikeKoodiService;

    @Autowired
    private KoodistoClient koodistoService;

    @Override
    public TilaUpdateStatus validate(Long perusteprojektiId, ProjektiTila targetTila) {
        TilaUpdateStatus status = new TilaUpdateStatus();
        Perusteprojekti projekti = perusteprojektiRepository.findOne(perusteprojektiId);
        Peruste peruste = projekti.getPeruste();
        peruste.getSuoritustavat().stream()
                .map(Suoritustapa::getTutkinnonOsat)
                .flatMap(Collection::stream)
                .map(TutkinnonOsaViite::getTutkinnonOsa)
                .filter(tosa -> tosa.getAmmattitaitovaatimukset2019() != null)
                .forEach(tosa -> {
                    Ammattitaitovaatimukset2019 av = tosa.getAmmattitaitovaatimukset2019();
                    List<Ammattitaitovaatimus2019> vaatimukset = new ArrayList<>(av.getVaatimukset());
                    vaatimukset.addAll(av.getKohdealueet().stream()
                            .map(Ammattitaitovaatimus2019Kohdealue::getVaatimukset)
                            .flatMap(Collection::stream)
                            .collect(Collectors.toList()));
                    vaatimukset.forEach(v -> {
                        Koodi koodi = v.getKoodi();
                        if (koodi == null) {
                            status.setVaihtoOk(false);
                            status.addStatus(
                                "tutkinnon-osan-kooditon-ammattitaitovaatimus",
                                Suoritustapakoodi.REFORMI,
                                Lists.newArrayList(
                                        mapper.map(tosa.getNimi(), LokalisoituTekstiDto.class),
                                        mapper.map(v.getVaatimus(), LokalisoituTekstiDto.class)));
                        }
                    });
                });
        return status;
    }

    @Override
    public boolean applicableKoulutustyyppi(KoulutusTyyppi tyyppi) {
        return tyyppi.isAmmatillinen();
    }

    @Override
    public boolean applicableTila(ProjektiTila tila) {
        return ProjektiTila.JULKAISTU.equals(tila);
    }

    @Override
    public boolean applicablePerustetyyppi(PerusteTyyppi tyyppi) {
        return PerusteTyyppi.NORMAALI.equals(tyyppi);
    }
}
