package fi.vm.sade.eperusteet.service;

import fi.vm.sade.eperusteet.domain.*;
import fi.vm.sade.eperusteet.domain.yl.lukio.LukiokurssiTyyppi;
import fi.vm.sade.eperusteet.dto.peruste.PerusteVersionDto;
import fi.vm.sade.eperusteet.dto.yl.lukio.*;
import fi.vm.sade.eperusteet.dto.yl.lukio.julkinen.LukioOppiainePuuDto;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.service.test.AbstractIntegrationTest;
import fi.vm.sade.eperusteet.service.util.OppiaineUtil.Reference;
import fi.vm.sade.eperusteet.service.yl.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;

import static fi.vm.sade.eperusteet.service.util.OppiaineUtil.oppiaine;
import static fi.vm.sade.eperusteet.service.util.PerusteTekstiUtil.*;
import static fi.vm.sade.eperusteet.service.yl.OppiaineOpetuksenSisaltoTyyppi.LUKIOKOULUTUS;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toSet;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

@DirtiesContext
@Ignore
public class KurssiServiceIT extends AbstractIntegrationTest {

    @Autowired
    private KurssiService kurssiService;

    @Autowired
    private PerusteService perusteService;

    @Autowired
    private OppiaineService oppiaineService;

    @Autowired
    @LockCtx(KurssiLockContext.class)
    private LockService<KurssiLockContext> lukioKurssiLockService;

    @Autowired
    @LockCtx(LukioOpetussuunnitelmaRakenneLockContext.class)
    private LockService<LukioOpetussuunnitelmaRakenneLockContext> lukioRakenneLockService;

    @Autowired
    private LukiokoulutuksenPerusteenSisaltoService sisaltoService;

    @Dto
    @Autowired
    private DtoMapper dtoMapper;

    private Long perusteId;
    private Reference<Long> suomiRef = new Reference<>();
    private Reference<Long> saameRef = new Reference<>();

    @Before
    public void setup() {
        Peruste peruste = perusteService.luoPerusteRunko(KoulutusTyyppi.LUKIOKOULUTUS, null, LaajuusYksikko.OPINTOVIIKKO, PerusteTyyppi.NORMAALI);
        perusteId = peruste.getId();
        oppiaine(teksti(
                fi("Äidinkieli ja kirjallisuus"),
                sv("Finska")
        )).maara(oppiaine(teksti(fi("Suomi"))).as(suomiRef))
            .maara(oppiaine(teksti(fi("Saame"))).as(saameRef))
        .luo(oppiaineService, perusteId, LUKIOKOULUTUS);
    }


    @Test
    public void testLukiokurssi() {
        PerusteVersionDto versionDto = perusteService.getPerusteVersion(perusteId);
        long id = kurssiService.createLukiokurssi(perusteId, LukioKurssiLuontiDto.builder()
                .tyyppi(LukiokurssiTyyppi.PAKOLLINEN)
                .oppiaineet(asList(
                        new KurssinOppiaineDto(suomiRef.getId(), 1)
                ))
                .nimi(teksti(fi("Äidinkielen perusteet"), sv("Finska ett")))
                .koodiArvo("AI1")
                .build());

        // FIXME: globalversioning pois
        //assertNotEquals(perusteService.getPerusteVersion(perusteId).getAikaleima(), versionDto.getAikaleima());

        LukiokurssiTarkasteleDto dto = kurssiService.getLukiokurssiTarkasteleDtoById(perusteId, id);
        assertNotNull(dto);
        assertEquals("AI1", dto.getKoodiArvo());
        assertEquals("Äidinkielen perusteet", dto.getNimi().get(Kieli.FI));
        assertEquals(1, dto.getOppiaineet().size());
        assertEquals(new Integer(1), dto.getOppiaineet().get(0).getJarjestys());
        assertEquals(suomiRef.getId(), dto.getOppiaineet().get(0).getOppiaineId());


        LukiokurssiMuokkausDto muokkausDto = new LukiokurssiMuokkausDto();
        muokkausDto.setId(dto.getId());
        muokkausDto.setTyyppi(dto.getTyyppi());
        muokkausDto.setNimi(dto.getNimi());
        muokkausDto.setKoodiArvo(dto.getKoodiArvo());
        muokkausDto.setKoodiArvo("ARVO");
        lukioKurssiLockService.lock(new KurssiLockContext(perusteId, dto.getId()));
        versionDto = perusteService.getPerusteVersion(perusteId);
        assertEquals(perusteService.getPerusteVersion(perusteId).getAikaleima(), versionDto.getAikaleima());
        versionDto = perusteService.getPerusteVersion(perusteId);
        kurssiService.updateLukiokurssi(perusteId, muokkausDto);
        assertNotEquals(perusteService.getPerusteVersion(perusteId).getAikaleima(), versionDto.getAikaleima());

        LukiokurssiOppaineMuokkausDto liitosDto = new LukiokurssiOppaineMuokkausDto();
        liitosDto.setId(dto.getId());
        liitosDto.getOppiaineet().addAll(dto.getOppiaineet());
        liitosDto.getOppiaineet().add(new KurssinOppiaineDto(saameRef.getId(), 1));
        lukioRakenneLockService.lock(new LukioOpetussuunnitelmaRakenneLockContext(perusteId));
        versionDto = perusteService.getPerusteVersion(perusteId);
        kurssiService.updateLukiokurssiOppiaineRelations(perusteId, liitosDto);
        assertNotEquals(perusteService.getPerusteVersion(perusteId).getAikaleima(), versionDto.getAikaleima());
        List<LukiokurssiListausDto> list = kurssiService.findLukiokurssitByPerusteId(perusteId);
        assertEquals(1, list.size());
        assertEquals("Äidinkielen perusteet", list.get(0).getNimi().get(Kieli.FI));
        assertEquals("ARVO", list.get(0).getKoodiArvo());
        assertEquals(2, list.get(0).getOppiaineet().size());
        assertTrue(list.get(0).getOppiaineet().stream().map(KurssinOppiaineDto::getOppiaineId)
                .collect(toSet()).containsAll(asList(suomiRef.getId(), saameRef.getId())));

        LukioOppiainePuuDto tree = sisaltoService.getOppiaineTreeStructure(perusteId);
        Assert.assertNotNull(tree);
        assertEquals(perusteId, tree.getPerusteId());
        assertEquals(1, tree.getOppiaineet().size());
        assertEquals(2, tree.getOppiaineet().get(0).getOppimaarat().size());
        assertEquals(1, tree.getOppiaineet().get(0).getOppimaarat().get(0).getKurssit().size());
    }
}
