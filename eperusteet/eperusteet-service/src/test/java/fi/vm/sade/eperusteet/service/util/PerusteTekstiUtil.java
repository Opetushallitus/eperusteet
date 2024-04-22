package fi.vm.sade.eperusteet.service.util;

import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.domain.PerusteTila;
import fi.vm.sade.eperusteet.domain.PerusteenOsaTunniste;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaViiteDto.Laaja;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaViiteDto.Matala;
import fi.vm.sade.eperusteet.dto.peruste.TekstiKappaleDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import lombok.Getter;

import static fi.vm.sade.eperusteet.domain.Kieli.*;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toMap;

public class PerusteTekstiUtil {

    private PerusteTekstiUtil() {
    }

    @Getter
    public static class KieliTeksti {
        private final Kieli kieli;
        private final String teksti;

        public KieliTeksti(Kieli kieli, String teksti) {
            this.kieli = kieli;
            this.teksti = teksti;
        }
    }

    public static KieliTeksti lokalisoitu(Kieli kieli, String value) {
        return new KieliTeksti(kieli, value);
    }

    public static KieliTeksti fi(String value) {
        return lokalisoitu(FI, value);
    }

    public static KieliTeksti en(String value) {
        return lokalisoitu(EN, value);
    }

    public static KieliTeksti sv(String value) {
        return lokalisoitu(SV, value);
    }

    public static TekstiKappaleDto kappale(LokalisoituTekstiDto nimi) {
        return kappale(nimi, PerusteTila.LUONNOS);
    }

    public static TekstiKappaleDto kappale(LokalisoituTekstiDto nimi, PerusteTila tila) {
        return kappale(nimi, tila, null);
    }

    public static TekstiKappaleDto kappale(LokalisoituTekstiDto nimi, PerusteTila tila, PerusteenOsaTunniste tunniste) {
        return new TekstiKappaleDto(nimi, tila, tunniste);
    }

    public static Matala matalaViite(TekstiKappaleDto kappale) {
        return new Matala(kappale);
    }

    public static Matala perusteOsa(KieliTeksti ... tekstit) {
        return matalaViite(kappale(teksti(tekstit)));
    }

    public static Laaja puu(TekstiKappaleDto nimi, Laaja ... osat) {
        Laaja laaja = new Laaja();
        laaja.setPerusteenOsa(nimi);
        laaja.setLapset(asList(osat));
        return laaja;
    }

    public static LokalisoituTekstiDto teksti(KieliTeksti ... tekstit) {
        return new LokalisoituTekstiDto(null, asList(tekstit).stream().collect(toMap(KieliTeksti::getKieli, KieliTeksti::getTeksti)));
    }
}
