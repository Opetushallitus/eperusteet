package fi.vm.sade.eperusteet.service;

import fi.vm.sade.eperusteet.domain.*;
import fi.vm.sade.eperusteet.domain.kios.KieliJaKaantajaTutkintoPerusteenSisalto;
import fi.vm.sade.eperusteet.dto.kios.*;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaViiteDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.repository.PerusteRepository;
import fi.vm.sade.eperusteet.repository.PerusteenOsaViiteRepository;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.service.test.AbstractIntegrationTest;
import fi.vm.sade.eperusteet.service.test.util.PerusteprojektiTestUtils;
import jakarta.persistence.EntityManager;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

@Transactional
@DirtiesContext
public class KieliKaantajaTutkintoIT extends AbstractIntegrationTest {

    @Autowired
    private PerusteenOsaService perusteenOsaService;

    @Autowired
    private PerusteenOsaViiteService perusteenOsaViiteService;

    @Autowired
    private PerusteRepository perusteRepository;

    @Autowired
    private PerusteenOsaViiteRepository perusteenOsaViiteRepository;

    @Autowired
    private PerusteprojektiTestUtils testUtils;

    @Autowired
    @Dto
    private DtoMapper mapper;

    @Autowired
    private EntityManager em;

    private Peruste peruste;
    private PerusteenOsaViite sisaltoViite;

    @Before
    public void setUp() {
        // Create a peruste with KIOS tyyppi
        PerusteprojektiDto projektiDto = testUtils.createPerusteprojekti(pp -> {
            pp.setTyyppi(PerusteTyyppi.KIELI_KAANTAJA_TUTKINTO);
        });

        peruste = perusteRepository.findOne(projektiDto.getPeruste().getIdLong());
        sisaltoViite = peruste.getKieliJaKaantajaTutkintoPerusteenSisalto().getSisalto();
    }

    @Test
    public void testCreateKaantajaTaito() {
        // Create KaantajaTaitoDto
        KaantajaTaitoDto kaantajaTaitoDto = new KaantajaTaitoDto();
        kaantajaTaitoDto.setNimi(LokalisoituTekstiDto.of("Kääntäjän taito"));
        kaantajaTaitoDto.setKuvaus(LokalisoituTekstiDto.of("Kääntäjän taidon kuvaus"));
        kaantajaTaitoDto.setValiotsikko(LokalisoituTekstiDto.of("Väliotsikko"));

        // Create kohdealueet
        List<KaantajaTaitoKohdealueDto> kohdealueet = new ArrayList<>();
        KaantajaTaitoKohdealueDto kohdealue = new KaantajaTaitoKohdealueDto();
        kohdealue.setKohdealueOtsikko(LokalisoituTekstiDto.of("Kohdealue 1"));
        kohdealue.setTutkintovaatimukset(new ArrayList<>());
        kohdealue.getTutkintovaatimukset().add(LokalisoituTekstiDto.of("Vaatimus 1"));
        kohdealue.getTutkintovaatimukset().add(LokalisoituTekstiDto.of("Vaatimus 2"));
        kohdealue.setArviointikriteerit(new ArrayList<>());
        kohdealue.getArviointikriteerit().add(LokalisoituTekstiDto.of("Kriteeri 1"));
        kohdealueet.add(kohdealue);

        kaantajaTaitoDto.setKohdealueet(kohdealueet);

        // Add to peruste
        PerusteenOsaViiteDto.Matala viiteDto = new PerusteenOsaViiteDto.Matala();
        viiteDto.setPerusteenOsa(kaantajaTaitoDto);
        PerusteenOsaViiteDto.Matala added = perusteenOsaViiteService.addSisalto(peruste.getId(), sisaltoViite.getId(), viiteDto);

        assertNotNull(added);
        assertNotNull(added.getId());
        assertThat(added.getPerusteenOsa()).isInstanceOf(KaantajaTaitoDto.class);

        // Verify creation
        KaantajaTaitoDto created = (KaantajaTaitoDto) perusteenOsaService.get(added.getPerusteenOsa().getId());
        assertNotNull(created);
        assertEquals("Kääntäjän taito", created.getNimi().get(Kieli.FI));
        assertEquals("Kääntäjän taidon kuvaus", created.getKuvaus().get(Kieli.FI));
        assertEquals("Väliotsikko", created.getValiotsikko().get(Kieli.FI));
        assertThat(created.getKohdealueet()).hasSize(1);
        assertEquals("Kohdealue 1", created.getKohdealueet().get(0).getKohdealueOtsikko().get(Kieli.FI));
        assertThat(created.getKohdealueet().get(0).getTutkintovaatimukset()).hasSize(2);
        assertThat(created.getKohdealueet().get(0).getArviointikriteerit()).hasSize(1);
    }

    @Test
    public void testUpdateKaantajaTaito() {
        // Create
        KaantajaTaitoDto kaantajaTaitoDto = new KaantajaTaitoDto();
        kaantajaTaitoDto.setNimi(LokalisoituTekstiDto.of("Original name"));
        kaantajaTaitoDto.setKuvaus(LokalisoituTekstiDto.of("Original description"));

        PerusteenOsaViiteDto.Matala viiteDto = new PerusteenOsaViiteDto.Matala();
        viiteDto.setPerusteenOsa(kaantajaTaitoDto);
        PerusteenOsaViiteDto.Matala added = perusteenOsaViiteService.addSisalto(peruste.getId(), sisaltoViite.getId(), viiteDto);

        // Update
        KaantajaTaitoDto toUpdate = (KaantajaTaitoDto) perusteenOsaService.get(added.getPerusteenOsa().getId());
        toUpdate.setNimi(LokalisoituTekstiDto.of("Updated name"));
        toUpdate.setKuvaus(LokalisoituTekstiDto.of("Updated description"));
        toUpdate.setValiotsikko(LokalisoituTekstiDto.of("New väliotsikko"));

        perusteenOsaService.lock(toUpdate.getId());
        KaantajaTaitoDto updated = perusteenOsaService.update(toUpdate);

        // Verify update
        KaantajaTaitoDto retrieved = (KaantajaTaitoDto) perusteenOsaService.get(updated.getId());
        assertEquals("Updated name", retrieved.getNimi().get(Kieli.FI));
        assertEquals("Updated description", retrieved.getKuvaus().get(Kieli.FI));
        assertEquals("New väliotsikko", retrieved.getValiotsikko().get(Kieli.FI));
    }

    @Test
    public void testDeleteKaantajaTaito() {
        // Create
        KaantajaTaitoDto kaantajaTaitoDto = new KaantajaTaitoDto();
        kaantajaTaitoDto.setNimi(LokalisoituTekstiDto.of("To be deleted"));

        PerusteenOsaViiteDto.Matala viiteDto = new PerusteenOsaViiteDto.Matala();
        viiteDto.setPerusteenOsa(kaantajaTaitoDto);
        PerusteenOsaViiteDto.Matala added = perusteenOsaViiteService.addSisalto(peruste.getId(), sisaltoViite.getId(), viiteDto);

        Long id = added.getPerusteenOsa().getId();
        Long viiteId = added.getId();
        // Verify it exists
        assertNotNull(perusteenOsaService.get(id));

        // Delete via viite
        perusteenOsaViiteService.removeSisalto(peruste.getId(), viiteId);

        // Verify deletion - viite should be removed
        assertNull(perusteenOsaViiteRepository.findById(viiteId).orElse(null));
    }

    @Test
    public void testCreateKaantajaTaitotasoasteikko() {
        // Create KaantajaTaitotasoasteikkoDto
        KaantajaTaitotasoasteikkoDto taitotasoasteikkoDto = new KaantajaTaitotasoasteikkoDto();
        taitotasoasteikkoDto.setNimi(LokalisoituTekstiDto.of("Taitotasoasteikko"));
        taitotasoasteikkoDto.setKuvaus(LokalisoituTekstiDto.of("Taitotasoasteikon kuvaus"));

        // Create kategoriat
        List<TaitotasoasteikkoKategoriaDto> kategoriat = new ArrayList<>();
        
        TaitotasoasteikkoKategoriaDto kategoria1 = new TaitotasoasteikkoKategoriaDto();
        kategoria1.setOtsikko(LokalisoituTekstiDto.of("Kategoria 1"));
        
        List<TaitotasoasteikkoKategoriaTaitotasoDto> taitotasot1 = new ArrayList<>();
        TaitotasoasteikkoKategoriaTaitotasoDto taitotaso1 = new TaitotasoasteikkoKategoriaTaitotasoDto();
        taitotaso1.setOtsikko(LokalisoituTekstiDto.of("Taso A"));
        taitotaso1.setKuvaus(LokalisoituTekstiDto.of("Tason A kuvaus"));
        taitotasot1.add(taitotaso1);
        
        TaitotasoasteikkoKategoriaTaitotasoDto taitotaso2 = new TaitotasoasteikkoKategoriaTaitotasoDto();
        taitotaso2.setOtsikko(LokalisoituTekstiDto.of("Taso B"));
        taitotaso2.setKuvaus(LokalisoituTekstiDto.of("Tason B kuvaus"));
        taitotasot1.add(taitotaso2);
        
        kategoria1.setTaitotasoasteikkoKategoriaTaitotasot(taitotasot1);
        kategoriat.add(kategoria1);

        taitotasoasteikkoDto.setTaitotasoasteikkoKategoriat(kategoriat);

        // Add to peruste
        PerusteenOsaViiteDto.Matala viiteDto = new PerusteenOsaViiteDto.Matala();
        viiteDto.setPerusteenOsa(taitotasoasteikkoDto);
        PerusteenOsaViiteDto.Matala added = perusteenOsaViiteService.addSisalto(peruste.getId(), sisaltoViite.getId(), viiteDto);

        assertNotNull(added);
        assertNotNull(added.getId());
        assertThat(added.getPerusteenOsa()).isInstanceOf(KaantajaTaitotasoasteikkoDto.class);

        // Verify creation
        KaantajaTaitotasoasteikkoDto created = (KaantajaTaitotasoasteikkoDto) perusteenOsaService.get(added.getPerusteenOsa().getId());
        assertNotNull(created);
        assertEquals("Taitotasoasteikko", created.getNimi().get(Kieli.FI));
        assertEquals("Taitotasoasteikon kuvaus", created.getKuvaus().get(Kieli.FI));
        assertThat(created.getTaitotasoasteikkoKategoriat()).hasSize(1);
        assertEquals("Kategoria 1", created.getTaitotasoasteikkoKategoriat().get(0).getOtsikko().get(Kieli.FI));
        assertThat(created.getTaitotasoasteikkoKategoriat().get(0).getTaitotasoasteikkoKategoriaTaitotasot()).hasSize(2);
        assertEquals("Taso A", created.getTaitotasoasteikkoKategoriat().get(0).getTaitotasoasteikkoKategoriaTaitotasot().get(0).getOtsikko().get(Kieli.FI));
    }

    @Test
    public void testUpdateKaantajaTaitotasoasteikko() {
        // Create
        KaantajaTaitotasoasteikkoDto taitotasoasteikkoDto = new KaantajaTaitotasoasteikkoDto();
        taitotasoasteikkoDto.setNimi(LokalisoituTekstiDto.of("Original taitotasoasteikko"));
        taitotasoasteikkoDto.setKuvaus(LokalisoituTekstiDto.of("Original description"));
        taitotasoasteikkoDto.setTaitotasoasteikkoKategoriat(new ArrayList<>());

        PerusteenOsaViiteDto.Matala viiteDto = new PerusteenOsaViiteDto.Matala();
        viiteDto.setPerusteenOsa(taitotasoasteikkoDto);
        PerusteenOsaViiteDto.Matala added = perusteenOsaViiteService.addSisalto(peruste.getId(), sisaltoViite.getId(), viiteDto);

        // Update
        KaantajaTaitotasoasteikkoDto toUpdate = (KaantajaTaitotasoasteikkoDto) perusteenOsaService.get(added.getPerusteenOsa().getId());
        toUpdate.setNimi(LokalisoituTekstiDto.of("Updated taitotasoasteikko"));
        toUpdate.setKuvaus(LokalisoituTekstiDto.of("Updated description"));

        // Add a kategoria
        TaitotasoasteikkoKategoriaDto newKategoria = new TaitotasoasteikkoKategoriaDto();
        newKategoria.setOtsikko(LokalisoituTekstiDto.of("New kategoria"));
        newKategoria.setTaitotasoasteikkoKategoriaTaitotasot(new ArrayList<>());
        toUpdate.getTaitotasoasteikkoKategoriat().add(newKategoria);

        perusteenOsaService.lock(toUpdate.getId());
        KaantajaTaitotasoasteikkoDto updated = perusteenOsaService.update(toUpdate);

        // Verify update
        KaantajaTaitotasoasteikkoDto retrieved = (KaantajaTaitotasoasteikkoDto) perusteenOsaService.get(updated.getId());
        assertEquals("Updated taitotasoasteikko", retrieved.getNimi().get(Kieli.FI));
        assertEquals("Updated description", retrieved.getKuvaus().get(Kieli.FI));
        assertThat(retrieved.getTaitotasoasteikkoKategoriat()).hasSize(1);
        assertEquals("New kategoria", retrieved.getTaitotasoasteikkoKategoriat().get(0).getOtsikko().get(Kieli.FI));
    }

    @Test
    public void testDeleteKaantajaTaitotasoasteikko() {
        // Create
        KaantajaTaitotasoasteikkoDto taitotasoasteikkoDto = new KaantajaTaitotasoasteikkoDto();
        taitotasoasteikkoDto.setNimi(LokalisoituTekstiDto.of("To be deleted"));
        taitotasoasteikkoDto.setTaitotasoasteikkoKategoriat(new ArrayList<>());

        PerusteenOsaViiteDto.Matala viiteDto = new PerusteenOsaViiteDto.Matala();
        viiteDto.setPerusteenOsa(taitotasoasteikkoDto);
        PerusteenOsaViiteDto.Matala added = perusteenOsaViiteService.addSisalto(peruste.getId(), sisaltoViite.getId(), viiteDto);

        Long id = added.getPerusteenOsa().getId();
        Long viiteId = added.getId();

        // Verify it exists
        assertNotNull(perusteenOsaService.get(id));

        // Delete via viite
        perusteenOsaViiteService.removeSisalto(peruste.getId(), viiteId);

        // Verify deletion - viite should be removed
        assertNull(perusteenOsaViiteRepository.findById(viiteId).orElse(null));
    }

    @Test
    public void testKaantajaTaitoWithMultipleKohdealueet() {
        // Create KaantajaTaito with multiple kohdealueet
        KaantajaTaitoDto kaantajaTaitoDto = new KaantajaTaitoDto();
        kaantajaTaitoDto.setNimi(LokalisoituTekstiDto.of("Multi kohdealue test"));

        List<KaantajaTaitoKohdealueDto> kohdealueet = new ArrayList<>();
        
        for (int i = 1; i <= 3; i++) {
            KaantajaTaitoKohdealueDto kohdealue = new KaantajaTaitoKohdealueDto();
            kohdealue.setKohdealueOtsikko(LokalisoituTekstiDto.of("Kohdealue " + i));
            kohdealue.setTutkintovaatimukset(new ArrayList<>());
            kohdealue.getTutkintovaatimukset().add(LokalisoituTekstiDto.of("Vaatimus " + i));
            kohdealue.setArviointikriteerit(new ArrayList<>());
            kohdealue.getArviointikriteerit().add(LokalisoituTekstiDto.of("Kriteeri " + i));
            kohdealueet.add(kohdealue);
        }

        kaantajaTaitoDto.setKohdealueet(kohdealueet);

        // Add to peruste
        PerusteenOsaViiteDto.Matala viiteDto = new PerusteenOsaViiteDto.Matala();
        viiteDto.setPerusteenOsa(kaantajaTaitoDto);
        PerusteenOsaViiteDto.Matala added = perusteenOsaViiteService.addSisalto(peruste.getId(), sisaltoViite.getId(), viiteDto);

        // Verify
        KaantajaTaitoDto created = (KaantajaTaitoDto) perusteenOsaService.get(added.getPerusteenOsa().getId());
        assertThat(created.getKohdealueet()).hasSize(3);
        
        for (int i = 0; i < 3; i++) {
            assertEquals("Kohdealue " + (i + 1), created.getKohdealueet().get(i).getKohdealueOtsikko().get(Kieli.FI));
            assertThat(created.getKohdealueet().get(i).getTutkintovaatimukset()).hasSize(1);
            assertThat(created.getKohdealueet().get(i).getArviointikriteerit()).hasSize(1);
        }
    }

    @Test
    public void testKaantajaTaitotasoasteikkoWithMultipleKategoriatAndTaitotasot() {
        // Create taitotasoasteikko with multiple kategoriat and taitotasot
        KaantajaTaitotasoasteikkoDto taitotasoasteikkoDto = new KaantajaTaitotasoasteikkoDto();
        taitotasoasteikkoDto.setNimi(LokalisoituTekstiDto.of("Multi kategoria test"));

        List<TaitotasoasteikkoKategoriaDto> kategoriat = new ArrayList<>();
        
        for (int i = 1; i <= 2; i++) {
            TaitotasoasteikkoKategoriaDto kategoria = new TaitotasoasteikkoKategoriaDto();
            kategoria.setOtsikko(LokalisoituTekstiDto.of("Kategoria " + i));
            
            List<TaitotasoasteikkoKategoriaTaitotasoDto> taitotasot = new ArrayList<>();
            for (int j = 1; j <= 3; j++) {
                TaitotasoasteikkoKategoriaTaitotasoDto taitotaso = new TaitotasoasteikkoKategoriaTaitotasoDto();
                taitotaso.setOtsikko(LokalisoituTekstiDto.of("Taso " + i + "." + j));
                taitotaso.setKuvaus(LokalisoituTekstiDto.of("Kuvaus " + i + "." + j));
                taitotasot.add(taitotaso);
            }
            
            kategoria.setTaitotasoasteikkoKategoriaTaitotasot(taitotasot);
            kategoriat.add(kategoria);
        }

        taitotasoasteikkoDto.setTaitotasoasteikkoKategoriat(kategoriat);

        // Add to peruste
        PerusteenOsaViiteDto.Matala viiteDto = new PerusteenOsaViiteDto.Matala();
        viiteDto.setPerusteenOsa(taitotasoasteikkoDto);
        PerusteenOsaViiteDto.Matala added = perusteenOsaViiteService.addSisalto(peruste.getId(), sisaltoViite.getId(), viiteDto);

        // Verify
        KaantajaTaitotasoasteikkoDto created = (KaantajaTaitotasoasteikkoDto) perusteenOsaService.get(added.getPerusteenOsa().getId());
        assertThat(created.getTaitotasoasteikkoKategoriat()).hasSize(2);
        
        for (int i = 0; i < 2; i++) {
            TaitotasoasteikkoKategoriaDto kategoria = created.getTaitotasoasteikkoKategoriat().get(i);
            assertEquals("Kategoria " + (i + 1), kategoria.getOtsikko().get(Kieli.FI));
            assertThat(kategoria.getTaitotasoasteikkoKategoriaTaitotasot()).hasSize(3);
            
            for (int j = 0; j < 3; j++) {
                TaitotasoasteikkoKategoriaTaitotasoDto taitotaso = kategoria.getTaitotasoasteikkoKategoriaTaitotasot().get(j);
                assertEquals("Taso " + (i + 1) + "." + (j + 1), taitotaso.getOtsikko().get(Kieli.FI));
                assertEquals("Kuvaus " + (i + 1) + "." + (j + 1), taitotaso.getKuvaus().get(Kieli.FI));
            }
        }
    }
}