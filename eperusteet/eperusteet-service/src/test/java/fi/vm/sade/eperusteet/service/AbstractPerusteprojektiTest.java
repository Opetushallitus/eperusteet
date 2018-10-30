package fi.vm.sade.eperusteet.service;


import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.Perusteprojekti;
import fi.vm.sade.eperusteet.domain.Suoritustapa;
import fi.vm.sade.eperusteet.domain.Suoritustapakoodi;
import fi.vm.sade.eperusteet.domain.tutkinnonosa.TutkinnonOsaTyyppi;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.RakenneModuuli;
import fi.vm.sade.eperusteet.dto.peruste.PerusteDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaViiteDto;
import fi.vm.sade.eperusteet.dto.peruste.TekstiKappaleDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiDto;
import fi.vm.sade.eperusteet.dto.tutkinnonosa.TutkinnonOsaDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.KoodiDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.RakenneModuuliDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.TutkinnonOsaViiteDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.repository.PerusteprojektiRepository;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.service.test.AbstractIntegrationTest;
import fi.vm.sade.eperusteet.service.test.util.PerusteprojektiTestUtils;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

@DirtiesContext
@Transactional
public class AbstractPerusteprojektiTest extends AbstractIntegrationTest {


    @Autowired
    protected PerusteprojektiService perusteprojektiService;

    @Autowired
    protected PerusteprojektiTestUtils testUtils;

    @Autowired
    protected PerusteService perusteService;

    @Autowired
    protected PerusteenOsaService perusteenOsaService;

    @Autowired
    protected PerusteprojektiRepository perusteprojektiRepository;

    @Autowired
    protected EntityManager em;

    @Autowired
    @Dto
    protected DtoMapper mapper;

    @Autowired
    @LockCtx(TutkinnonRakenneLockContext.class)
    protected LockService<TutkinnonRakenneLockContext> lockService;

    protected Perusteprojekti projekti;
    protected Peruste peruste;
    protected Suoritustapa suoritustapa;
    protected RakenneModuuli rakenne;

    @Before
    public void setup() {
        PerusteprojektiDto projektiDto = testUtils.createPerusteprojekti();
        projekti = perusteprojektiRepository.findOne(projektiDto.getId());
        peruste = projekti.getPeruste();
        suoritustapa = peruste.getSuoritustapa(Suoritustapakoodi.REFORMI);
        rakenne = suoritustapa.getRakenne();
    }

    protected RakenneModuuliDto getRakenneDto() {
        return perusteService.getTutkinnonRakenne(peruste.getId(), suoritustapa.getSuoritustapakoodi(), null);
    }

    protected PerusteDto update(PerusteDto perusteDto) {
        return perusteService.update(perusteDto.getId(), perusteDto);
    }

    protected RakenneModuuliDto update(RakenneModuuliDto rakenne) {
        final TutkinnonRakenneLockContext ctx = TutkinnonRakenneLockContext.of(peruste.getId(), Suoritustapakoodi.REFORMI);
        lockService.lock(ctx);
        RakenneModuuliDto updated = perusteService.updateTutkinnonRakenne(peruste.getId(), suoritustapa.getSuoritustapakoodi(), rakenne);
        return updated;
    }

    protected TutkinnonOsaViiteDto uusiTutkinnonOsa() {
        TutkinnonOsaViiteDto result = perusteService.addTutkinnonOsa(peruste.getId(), suoritustapa.getSuoritustapakoodi(), TutkinnonOsaViiteDto.builder()
                .tyyppi(TutkinnonOsaTyyppi.NORMAALI)
                .tutkinnonOsaDto(TutkinnonOsaDto.builder()
                        .build())
                .build());
        return result;
    }

    protected PerusteenOsaViiteDto.Matala uusiTekstiKappale(LokalisoituTekstiDto nimi, LokalisoituTekstiDto teksti, KoodiDto osaamisala) {
        PerusteenOsaViiteDto.Matala pov = new PerusteenOsaViiteDto.Matala();
        TekstiKappaleDto tk = new TekstiKappaleDto();
        pov.setPerusteenOsa(tk);
        PerusteenOsaViiteDto.Matala result = perusteService.addSisaltoUUSI(peruste.getId(), suoritustapa.getSuoritustapakoodi(), pov);

        tk = (TekstiKappaleDto) result.getPerusteenOsa();
        tk.setNimi(nimi);
        tk.setTeksti(teksti);
        tk.setOsaamisala(osaamisala);

        perusteenOsaService.lock(tk.getId());
        perusteenOsaService.update(tk);

        return result;
    }

}
