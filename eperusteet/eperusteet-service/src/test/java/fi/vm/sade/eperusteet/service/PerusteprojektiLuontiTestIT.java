package fi.vm.sade.eperusteet.service;

import fi.vm.sade.eperusteet.domain.*;
import fi.vm.sade.eperusteet.dto.peruste.PerusteDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteHakuDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteKaikkiDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteQuery;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiLuontiDto;
import fi.vm.sade.eperusteet.repository.KoulutusRepository;
import fi.vm.sade.eperusteet.repository.PerusteRepository;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.service.test.AbstractIntegrationTest;
import fi.vm.sade.eperusteet.service.test.util.PerusteprojektiTestUtils;
import fi.vm.sade.eperusteet.service.test.util.TestUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;

@DirtiesContext
@Transactional
public class PerusteprojektiLuontiTestIT extends AbstractIntegrationTest {

    @Autowired
    private PerusteService perusteService;

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
    private PerusteprojektiTestUtils ppTestUtils;

    @Test
    @Rollback
    public void testPerusteprojektiHakuNimella() {
        PerusteprojektiDto projekti = ppTestUtils.createPeruste();
        PerusteDto perusteDto = ppTestUtils.editPeruste(projekti.getPeruste().getIdLong(), (PerusteDto peruste) -> {
            peruste.setNimi(TestUtils.lt("zäääää"));
            peruste.getNimi().getTekstit().put(Kieli.SV, "ååå");
            peruste.setVoimassaoloAlkaa(new GregorianCalendar(Calendar.getInstance().get(Calendar.YEAR) - 2, Calendar.MARCH, 12).getTime());
        });
        ppTestUtils.julkaise(projekti.getId());

        PerusteprojektiDto projekti2 = ppTestUtils.createPeruste();
        PerusteDto perusteDto2 = ppTestUtils.editPeruste(projekti2.getPeruste().getIdLong(), (PerusteDto peruste) -> {
            peruste.setNimi(TestUtils.lt("xäöäöäöä"));
            peruste.setVoimassaoloAlkaa(new GregorianCalendar(Calendar.getInstance().get(Calendar.YEAR) - 2, Calendar.MARCH, 12).getTime());
        });
        ppTestUtils.julkaise(projekti2.getId());

        PerusteQuery pquery = new PerusteQuery();
        Page<PerusteHakuDto> haku = perusteService.findJulkinenBy(new PageRequest(0, 10), pquery);
        assertThat(haku.getTotalElements()).isEqualTo(2);

        pquery.setNimi("x");
        haku = perusteService.findJulkinenBy(new PageRequest(0, 10), pquery);
        assertThat(haku.getTotalElements()).isEqualTo(1);

        pquery.setNimi("äää");
        haku = perusteService.findJulkinenBy(new PageRequest(0, 10), pquery);
        assertThat(haku.getTotalElements()).isEqualTo(1);
        assertThat(haku.getContent().iterator().next().getId()).isEqualTo(perusteDto.getId());

        pquery.setNimi("ö");
        haku = perusteService.findJulkinenBy(new PageRequest(0, 10), pquery);
        assertThat(haku.getTotalElements()).isEqualTo(1);
        assertThat(haku.getContent().iterator().next().getId()).isEqualTo(perusteDto2.getId());

        pquery.setNimi("å");
        haku = perusteService.findJulkinenBy(new PageRequest(0, 10), pquery);
        assertThat(haku.getTotalElements()).isEqualTo(1);
        assertThat(haku.getContent().iterator().next().getId()).isEqualTo(perusteDto.getId());
    }

    @Test
    @Rollback
    public void testLuonnissaOlevatEiHakuun() {
        PerusteprojektiDto perusteprojekti = ppTestUtils.createPeruste((PerusteprojektiLuontiDto pp) -> {
        });
        PerusteDto perusteDto = ppTestUtils.editPeruste(perusteprojekti.getPeruste().getIdLong(), (PerusteDto peruste) -> {
        });
        PerusteQuery pquery = new PerusteQuery();
        Page<PerusteHakuDto> perusteet = perusteService.findJulkinenBy(new PageRequest(0, 10), pquery);
        assertThat(perusteet.getTotalElements()).isEqualTo(0);
        ppTestUtils.asetaTila(perusteprojekti.getId(), ProjektiTila.VIIMEISTELY);
        perusteet = perusteService.findJulkinenBy(new PageRequest(0, 10), pquery);
        assertThat(perusteet.getTotalElements()).isEqualTo(0);
        ppTestUtils.asetaTila(perusteprojekti.getId(), ProjektiTila.VALMIS);
        perusteet = perusteService.findJulkinenBy(new PageRequest(0, 10), pquery);
        assertThat(perusteet.getTotalElements()).isEqualTo(0);
    }


    @Test
    @Rollback
    public void testAmosaaJaettuPohja() {
        PerusteKaikkiDto pohja = perusteService.getAmosaaYhteinenPohja();
        assertThat(pohja).isNull();

        PerusteprojektiDto amosaaPohja1 = ppTestUtils.createPeruste((PerusteprojektiLuontiDto ppl) -> {
            ppl.setTyyppi(PerusteTyyppi.AMOSAA_YHTEINEN);
        });
        PerusteDto perusteDto = ppTestUtils.editPeruste(amosaaPohja1.getPeruste().getIdLong(), (PerusteDto peruste) -> {
            peruste.setVoimassaoloAlkaa(new GregorianCalendar(Calendar.getInstance().get(Calendar.YEAR) - 2, Calendar.MARCH, 12).getTime());
        });
        ppTestUtils.asetaTila(amosaaPohja1.getId(), ProjektiTila.VIIMEISTELY);
        ppTestUtils.asetaTila(amosaaPohja1.getId(), ProjektiTila.VALMIS);
        ppTestUtils.asetaTila(amosaaPohja1.getId(), ProjektiTila.JULKAISTU);

        // Amosaa yhteiset eivät tule julkiseen hakuun
        PerusteQuery pquery = new PerusteQuery();
        Page<PerusteHakuDto> perusteet = perusteService.findJulkinenBy(new PageRequest(0, 10), pquery);
        assertThat(perusteet.getTotalElements()).isEqualTo(0);

        pohja = perusteService.getAmosaaYhteinenPohja();
        assertThat(pohja).isNotNull().hasFieldOrPropertyWithValue("id", perusteDto.getId());

        PerusteprojektiDto amosaaPohja2 = ppTestUtils.createPeruste((PerusteprojektiLuontiDto ppl) -> {
            ppl.setTyyppi(PerusteTyyppi.AMOSAA_YHTEINEN);
        });
        PerusteDto perusteDto2 = ppTestUtils.editPeruste(amosaaPohja2.getPeruste().getIdLong(), (PerusteDto peruste) -> {
            peruste.setVoimassaoloAlkaa(new GregorianCalendar(Calendar.getInstance().get(Calendar.YEAR) - 1, Calendar.MARCH, 12).getTime());
        });
        ppTestUtils.asetaTila(amosaaPohja2.getId(), ProjektiTila.VIIMEISTELY);
        ppTestUtils.asetaTila(amosaaPohja2.getId(), ProjektiTila.VALMIS);
        ppTestUtils.asetaTila(amosaaPohja2.getId(), ProjektiTila.JULKAISTU);

        perusteet = perusteService.findJulkinenBy(new PageRequest(0, 10), pquery);
        assertThat(perusteet.getTotalElements()).isEqualTo(0);
        PerusteKaikkiDto amosaaYhteinen = perusteService.getAmosaaYhteinenPohja();
        assertThat(amosaaYhteinen)
                .isNotNull()
                .hasFieldOrPropertyWithValue("id", perusteDto2.getId());

        // Kaikki tulevat sisäiseen hakuun
        pquery = new PerusteQuery();
        perusteet = perusteService.findByInternal(new PageRequest(0, 10), pquery);
        assertThat(perusteet.getTotalElements())
                .isEqualTo(2);
        assertThat(perusteet.getContent().stream().map(PerusteHakuDto::getId))
                .contains(perusteDto.getId(), perusteDto2.getId());
    }

}
