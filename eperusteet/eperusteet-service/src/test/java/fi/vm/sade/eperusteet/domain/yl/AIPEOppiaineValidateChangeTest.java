package fi.vm.sade.eperusteet.domain.yl;

import fi.vm.sade.eperusteet.domain.Koodi;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class AIPEOppiaineValidateChangeTest {

    @Test
    public void kurssienJarjestystaVoiMuuttaa() {
        AIPEOppiaine nykyinen = oppiaine(
                kurssi(1L, 0, "oppiaineetaipe_1"),
                kurssi(2L, 1, "oppiaineetaipe_2"));
        AIPEOppiaine paivitetty = oppiaine(
                kurssi(2L, 0, "oppiaineetaipe_2"),
                kurssi(1L, 1, "oppiaineetaipe_1"));

        assertThatCode(() -> AIPEOppiaine.validateChange(nykyinen, paivitetty))
                .doesNotThrowAnyException();
    }

    @Test
    public void kurssinKoodiaEiVoiMuuttaa() {
        AIPEOppiaine nykyinen = oppiaine(
                kurssi(1L, 0, "oppiaineetaipe_1"),
                kurssi(2L, 1, "oppiaineetaipe_2"));
        AIPEOppiaine paivitetty = oppiaine(
                kurssi(2L, 0, "oppiaineetaipe_vaihtunut"),
                kurssi(1L, 1, "oppiaineetaipe_1"));

        assertThatThrownBy(() -> AIPEOppiaine.validateChange(nykyinen, paivitetty))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessage("koodia-ei-voi-muuttaa");
    }

    private static AIPEOppiaine oppiaine(AIPEKurssi... kurssit) {
        AIPEOppiaine oppiaine = new AIPEOppiaine();
        oppiaine.addKurssit(List.of(kurssit));
        return oppiaine;
    }

    private static AIPEKurssi kurssi(Long id, int jarjestys, String uri) {
        Koodi koodi = new Koodi();
        koodi.setUri(uri);
        koodi.setKoodisto("oppiaineetaipe");

        AIPEKurssi kurssi = new AIPEKurssi();
        kurssi.setId(id);
        kurssi.setJarjestys(jarjestys);
        kurssi.setKoodi(koodi);
        return kurssi;
    }
}
