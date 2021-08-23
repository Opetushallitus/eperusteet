package fi.vm.sade.eperusteet.service;

import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.domain.Koulutus;
import fi.vm.sade.eperusteet.dto.KevytTekstiKappaleDto;
import fi.vm.sade.eperusteet.dto.MaaraysDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.service.test.AbstractIntegrationTest;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.assertj.core.util.Maps;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;

import static org.assertj.core.api.Assertions.*;

@DirtiesContext
public class MaaraysServiceIT extends AbstractIntegrationTest {

    @Autowired
    private MaaraysService maaraysService;

    @Test
    public void testCRUD() {
        assertThat(maaraysService.getMaaraykset()).isEmpty();

        maaraysService.addMaarays(MaaraysDto.builder()
                .nimi(LokalisoituTekstiDto.of("maarays1"))
                .url(Maps.newHashMap(Kieli.FI, "url1"))
                .build());

        MaaraysDto maaraysDto = maaraysService.addMaarays(MaaraysDto.builder()
                .nimi(LokalisoituTekstiDto.of("maarays2"))
                .url(Maps.newHashMap(Kieli.SV, "url2"))
                .build());

        assertThat(maaraysService.getMaaraykset()).hasSize(2);
        assertThat(maaraysService.getMaaraykset()).extracting("nimi").extracting("tekstit")
                .containsExactlyInAnyOrder(Maps.newHashMap(Kieli.FI, "maarays1"), Maps.newHashMap(Kieli.FI, "maarays2"));

        assertThat(maaraysService.getMaaraykset()).extracting("url")
                .containsExactlyInAnyOrder(Maps.newHashMap(Kieli.FI, "url1"), Maps.newHashMap(Kieli.SV, "url2"));

        maaraysDto.setNimi(LokalisoituTekstiDto.of("maarays2muokattu"));
        maaraysService.updateMaarays(maaraysDto);

        assertThat(maaraysService.getMaaraykset()).extracting("nimi").extracting("tekstit")
                .containsExactlyInAnyOrder(Maps.newHashMap(Kieli.FI, "maarays1"), Maps.newHashMap(Kieli.FI, "maarays2muokattu"));

        maaraysService.deleteMaarays(maaraysDto.getId());

        assertThat(maaraysService.getMaaraykset()).hasSize(1);
        assertThat(maaraysService.getMaaraykset()).extracting("nimi").extracting("tekstit")
                .containsExactlyInAnyOrder(Maps.newHashMap(Kieli.FI, "maarays1"));
    }

}
