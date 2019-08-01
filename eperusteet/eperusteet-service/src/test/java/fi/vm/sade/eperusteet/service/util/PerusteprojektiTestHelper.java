package fi.vm.sade.eperusteet.service.util;

import fi.vm.sade.eperusteet.domain.KoulutusTyyppi;
import fi.vm.sade.eperusteet.domain.KoulutustyyppiToteutus;
import fi.vm.sade.eperusteet.domain.LaajuusYksikko;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiDto;
import fi.vm.sade.eperusteet.repository.PerusteRepository;
import fi.vm.sade.eperusteet.repository.PerusteprojektiRepository;
import fi.vm.sade.eperusteet.service.*;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.service.test.util.PerusteprojektiTestUtils;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

@Component
@Transactional
@Scope("prototype")
public class PerusteprojektiTestHelper {

    @Autowired
    private PerusteprojektiService perusteprojektiService;

    @Autowired
    private PerusteprojektiTestUtils testUtils;

    @Autowired
    private PerusteService perusteService;

    @Autowired
    private PerusteenOsaService perusteenOsaService;

    @Autowired
    private PerusteprojektiRepository perusteprojektiRepository;

    @Autowired
    private PerusteRepository perusteRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    @Dto
    private DtoMapper mapper;

    @Autowired
    @LockCtx(TutkinnonRakenneLockContext.class)
    private LockService<TutkinnonRakenneLockContext> lockService;

    @Getter
    private Long projektiId;

    @Getter
    private Long perusteId;

    public void setup(KoulutusTyyppi koulutusTyyppi, KoulutustyyppiToteutus toteutus) {
        PerusteprojektiDto projektiDto = testUtils.createPerusteprojekti(projekti -> {
            projekti.setToteutus(toteutus);
            projekti.setKoulutustyyppi(koulutusTyyppi.toString());
        });
        projektiId = projektiDto.getId();
        perusteId = projektiDto.getPeruste().getIdLong();
    }

}
