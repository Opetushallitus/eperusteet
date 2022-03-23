package fi.vm.sade.eperusteet.domain.koto;

import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.vst.KotoLaajaAlainenOsaaminen;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class KotoLaajaAlainenOsaaminenTest {

    /**
     * Tarkastellaan rakenteen yhteneväistyyttä. Yleiskuvauksen arvo voi olla eri mutta structureEquals
     * palauttaa false jos toisessa rakenteessa on arvo ja toisessa ei
     */
    @Test
    public void testKotoLaajaAlainenYleiskuvausStructurEquals() {
        KotoLaajaAlainenOsaaminen koto = new KotoLaajaAlainenOsaaminen();
        koto.setYleiskuvaus(TekstiPalanen.of(Kieli.FI, "joku yleiskuvaus"));

        KotoLaajaAlainenOsaaminen updatedKoto = new KotoLaajaAlainenOsaaminen();
        updatedKoto.setYleiskuvaus(TekstiPalanen.of(Kieli.FI, "päivitetty yleiskuvaus"));

        boolean areEqual = koto.structureEquals(updatedKoto);
        assertThat(areEqual)
                .withFailMessage("Jos molemissa on arvo, rakenteet pitäisi olla yhtenevät")
                .isTrue();

        updatedKoto.setYleiskuvaus(null);

        boolean areEqual2 = koto.structureEquals(updatedKoto);
        assertThat(areEqual2)
                .withFailMessage("Rakenteet eivät yhteneväiset jos toinen null")
                .isFalse();
    }
}
