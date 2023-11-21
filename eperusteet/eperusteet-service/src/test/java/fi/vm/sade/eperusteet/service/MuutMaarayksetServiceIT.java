package fi.vm.sade.eperusteet.service;

import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.dto.MuuMaaraysDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.service.test.AbstractIntegrationTest;
import org.assertj.core.util.Maps;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;

import static org.assertj.core.api.Assertions.*;

@DirtiesContext
public class MuutMaarayksetServiceIT extends AbstractIntegrationTest {

    @Autowired
    private MuutMaarayksetService muutMaarayksetService;

    @Test
    public void testCRUD() {
        assertThat(muutMaarayksetService.getMaaraykset()).isEmpty();

        muutMaarayksetService.addMaarays(MuuMaaraysDto.builder()
                .nimi(LokalisoituTekstiDto.of("maarays1"))
                .url(Maps.newHashMap(Kieli.FI, "url1"))
                .build());

        MuuMaaraysDto muuMaaraysDto = muutMaarayksetService.addMaarays(MuuMaaraysDto.builder()
                .nimi(LokalisoituTekstiDto.of("maarays2"))
                .url(Maps.newHashMap(Kieli.SV, "url2"))
                .build());

        assertThat(muutMaarayksetService.getMaaraykset()).hasSize(2);
        assertThat(muutMaarayksetService.getMaaraykset()).extracting("nimi").extracting("tekstit")
                .containsExactlyInAnyOrder(Maps.newHashMap(Kieli.FI, "maarays1"), Maps.newHashMap(Kieli.FI, "maarays2"));

        assertThat(muutMaarayksetService.getMaaraykset()).extracting("url")
                .containsExactlyInAnyOrder(Maps.newHashMap(Kieli.FI, "url1"), Maps.newHashMap(Kieli.SV, "url2"));

        muuMaaraysDto.setNimi(LokalisoituTekstiDto.of("maarays2muokattu"));
        muutMaarayksetService.updateMaarays(muuMaaraysDto);

        assertThat(muutMaarayksetService.getMaaraykset()).extracting("nimi").extracting("tekstit")
                .containsExactlyInAnyOrder(Maps.newHashMap(Kieli.FI, "maarays1"), Maps.newHashMap(Kieli.FI, "maarays2muokattu"));

        muutMaarayksetService.deleteMaarays(muuMaaraysDto.getId());

        assertThat(muutMaarayksetService.getMaaraykset()).hasSize(1);
        assertThat(muutMaarayksetService.getMaaraykset()).extracting("nimi").extracting("tekstit")
                .containsExactlyInAnyOrder(Maps.newHashMap(Kieli.FI, "maarays1"));
    }

}
