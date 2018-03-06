package fi.vm.sade.eperusteet.service;

import fi.vm.sade.eperusteet.domain.*;
import fi.vm.sade.eperusteet.domain.tutkinnonosa.OsaamisenTavoite;
import fi.vm.sade.eperusteet.domain.tutkinnonosa.TutkinnonOsa;
import fi.vm.sade.eperusteet.domain.tutkinnonosa.ValmaTelmaSisalto;
import fi.vm.sade.eperusteet.domain.yl.TpoOpetuksenSisalto;
import fi.vm.sade.eperusteet.dto.KevytTekstiKappaleDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteKaikkiDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaViiteDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiDto;
import fi.vm.sade.eperusteet.dto.tutkinnonosa.TutkinnonOsaKaikkiDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.KoodiDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.RakenneModuuliDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.dto.yl.TaiteenalaDto;
import fi.vm.sade.eperusteet.repository.KoulutusRepository;
import fi.vm.sade.eperusteet.repository.PerusteRepository;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.service.test.AbstractIntegrationTest;
import fi.vm.sade.eperusteet.service.test.util.PerusteprojektiTestUtils;
import fi.vm.sade.eperusteet.service.yl.TpoOpetuksenSisaltoService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@DirtiesContext
@Transactional
public class PerusteJulkisetTestIT extends AbstractIntegrationTest {

    @Autowired
    private PerusteService perusteService;

    @Autowired
    private PerusteenOsaService perusteenOsaService;

    @Autowired
    private PerusteprojektiService perusteprojektiService;

    @Autowired
    private PerusteRepository repo;

    @Autowired
    private PlatformTransactionManager manager;

    @Autowired
    private KoulutusRepository koulutusRepository;

    @Autowired
    @LockCtx(TutkinnonRakenneLockContext.class)
    private LockService<TutkinnonRakenneLockContext> lockService;

    @Autowired
    @Dto
    private DtoMapper mapper;

    @Autowired
    public PerusteprojektiTestUtils ppTestUtils;


    // EP-1326
    @Test
    @Rollback
    public void testTutkinnonOsaKaikkiMapping() {
        TutkinnonOsa tosa = new TutkinnonOsa();
        ValmaTelmaSisalto valmaTelmaSisalto = new ValmaTelmaSisalto();
        List<OsaamisenTavoite> tavoitteet = new ArrayList<>();
        OsaamisenTavoite tavoite = new OsaamisenTavoite();
        tavoite.setNimi(TekstiPalanen.of(Kieli.FI, "nimi"));
        tavoite.setKohde(TekstiPalanen.of(Kieli.FI, "kohde"));
        tavoite.setSelite(TekstiPalanen.of(Kieli.FI, "selite"));
        tavoite.setTavoitteet(Stream.of(
                TekstiPalanen.of(Kieli.FI, "tavoite 1"),
                TekstiPalanen.of(Kieli.FI, "tavoite 2"))
                .collect(Collectors.toList()));
        tavoitteet.add(tavoite);
        tavoitteet.add(tavoite);
        valmaTelmaSisalto.setOsaamistavoite(tavoitteet);
        tosa.setValmaTelmaSisalto(valmaTelmaSisalto);
        TutkinnonOsaKaikkiDto tosaKaikkiDto = mapper.map(tosa, TutkinnonOsaKaikkiDto.class);
        assertThat(tosaKaikkiDto.getValmaTelmaSisalto().getOsaamistavoite().get(0))
                .extracting(
                        t -> t.getNimi().get(Kieli.FI),
                        t -> t.getKohde().get(Kieli.FI),
                        t -> t.getSelite().get(Kieli.FI),
                        t -> t.getTavoitteet().stream()
                                .map(LokalisoituTekstiDto::getTekstit)
                                .map(x -> x.get(Kieli.FI))
                                .collect(Collectors.joining(", ")))
                .containsExactly(
                        "nimi",
                        "kohde",
                        "selite",
                        "tavoite 1, tavoite 2");
    }

    @Test
    @Rollback
    public void testValmaTelmaExport() {
        PerusteprojektiDto pp = ppTestUtils.createPerusteprojekti(ppl -> {
            ppl.setKoulutustyyppi(KoulutusTyyppi.VALMA.toString());
            ppl.setReforminMukainen(true);
        });

        PerusteDto perusteDto = ppTestUtils.initPeruste(pp.getPeruste().getIdLong());

        ppTestUtils.asetaMuodostumiset(perusteDto.getId());

        ppTestUtils.julkaise(pp.getId());
    }

    @Test
    @Rollback
    public void testPerusteKaikkiTPO() {
        PerusteprojektiDto pp = ppTestUtils.createPerusteprojekti(ppl -> {
            ppl.setKoulutustyyppi(KoulutusTyyppi.TPO.toString());
        });

        PerusteDto perusteDto = ppTestUtils.initPeruste(pp.getPeruste().getIdLong());

        TaiteenalaDto taiteenalaDto = new TaiteenalaDto();
        taiteenalaDto.setAikuistenOpetus(KevytTekstiKappaleDto.of("aikuisten opetus nimi", "aikuisten opetus teksti"));
        taiteenalaDto.setKasvatus(KevytTekstiKappaleDto.of("kasvatus nimi", "kasvatus teksti"));
        taiteenalaDto.setYhteisetOpinnot(KevytTekstiKappaleDto.of("yhteiset nimi", "yhteiset teksti"));
        taiteenalaDto.setOppimisenArviointiOpetuksessa(KevytTekstiKappaleDto.of("arviointi nimi", "arviointi teksti"));
        taiteenalaDto.setTeemaopinnot(KevytTekstiKappaleDto.of("teemaopinnot nimi", "teemaopinnot teksti"));
        taiteenalaDto.setKoodi(KoodiDto.of("taiteenalat", "x"));
        PerusteenOsaViiteDto.Matala taiteenalaviite = new PerusteenOsaViiteDto.Matala();
        taiteenalaviite.setPerusteenOsa(taiteenalaDto);
        taiteenalaviite = perusteService.addSisaltoUUSI(perusteDto.getId(), Suoritustapakoodi.TPO, taiteenalaviite);
        taiteenalaDto = (TaiteenalaDto)taiteenalaviite.getPerusteenOsa();
        taiteenalaDto.setTeksti(LokalisoituTekstiDto.of("teksti"));
        perusteenOsaService.lock(taiteenalaDto.getId());
        taiteenalaDto = perusteenOsaService.update(taiteenalaDto);

        ppTestUtils.julkaise(pp.getId());

        PerusteKaikkiDto tpoPeruste = perusteService.getKokoSisalto(perusteDto.getId());
        assertThat(tpoPeruste.getTpoOpetuksenSisalto().getSisalto().getLapset()).hasSize(1);
        assertThat((TaiteenalaDto)tpoPeruste.getTpoOpetuksenSisalto().getSisalto().getLapset().get(0).getPerusteenOsa())
                .extracting(
                        p -> p.getKoodi().getUri(),
                        p -> p.getTeemaopinnot().getNimi().get(Kieli.FI),
                        p -> p.getAikuistenOpetus().getTeksti().get(Kieli.FI),
                        p -> p.getTeksti().get(Kieli.FI))
                .containsExactly("taiteenalat_x", "teemaopinnot nimi", "aikuisten opetus teksti", "teksti");

    }

}
