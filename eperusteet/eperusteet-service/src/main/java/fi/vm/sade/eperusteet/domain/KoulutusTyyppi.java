package fi.vm.sade.eperusteet.domain;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Arrays;
import java.util.List;

public enum KoulutusTyyppi {
    PERUSTUTKINTO("koulutustyyppi_1"),
    LUKIOKOULUTUS("koulutustyyppi_2"),
    TELMA("koulutustyyppi_5"),
    LISAOPETUS("koulutustyyppi_6"),
    AMMATTITUTKINTO("koulutustyyppi_11"),
    ERIKOISAMMATTITUTKINTO("koulutustyyppi_12"),
    AIKUISTENLUKIOKOULUTUS("koulutustyyppi_14"),
    ESIOPETUS("koulutustyyppi_15"),
    PERUSOPETUS("koulutustyyppi_16"),
    AIKUISTENPERUSOPETUS("koulutustyyppi_17"),
    VALMA("koulutustyyppi_18"),
    VARHAISKASVATUS("koulutustyyppi_20"),
    PERUSOPETUSVALMISTAVA("koulutustyyppi_22"),
    LUKIOVALMISTAVAKOULUTUS("koulutustyyppi_23"),
    TPO("koulutustyyppi_999907"),
    VAPAASIVISTYSTYO("koulutustyyppi_10"),
    MAAHANMUUTTAJIENKOTOUTUMISKOULUTUS("koulutustyyppi_30"),
    VAPAASIVISTYSTYOLUKUTAITO("koulutustyyppi_35"),
    TUTKINTOONVALMENTAVA("koulutustyyppi_40"),
    MUU_KOULUTUS("koulutustyyppi_muu");

    private final String tyyppi;

    KoulutusTyyppi(String tyyppi) {
        this.tyyppi = tyyppi;
    }

    @Override
    public String toString() {
        return tyyppi;
    }

    @JsonCreator
    public static KoulutusTyyppi of(String tila) {
        for (KoulutusTyyppi s : values()) {
            if (s.tyyppi.equalsIgnoreCase(tila)) {
                return s;
            }
        }
        throw new IllegalArgumentException(tila + " ei ole kelvollinen KoulutusTyyppi");
    }

    public boolean isOneOf(KoulutusTyyppi... tyypit) {
        for (KoulutusTyyppi toinen : tyypit) {
            if (toinen.toString().equals(this.tyyppi)) {
                return true;
            }
        }
        return false;
    }

    public boolean isValmaTelma() {
        return isOneOf(VALMA, TELMA);
    }

    public boolean isYksinkertainen() {
        return isOneOf(LISAOPETUS, ESIOPETUS, VARHAISKASVATUS, PERUSOPETUSVALMISTAVA);
    }

    public boolean isAmmatillinen() {
        return isOneOf(AMMATTITUTKINTO, ERIKOISAMMATTITUTKINTO, PERUSTUTKINTO);
    }

    public boolean isVapaaSivistystyo() {
        return isOneOf(VAPAASIVISTYSTYO, VAPAASIVISTYSTYOLUKUTAITO);
    }

    public static List<String> ammatilliset() {
        return Arrays.asList(AMMATTITUTKINTO.toString(), ERIKOISAMMATTITUTKINTO.toString(), PERUSTUTKINTO.toString());
    }

    public static List<String> valmaTelma() {
        return Arrays.asList(VALMA.toString(), TELMA.toString());
    }

    public static List<String> amosaaAppKoulutustyypit() {
        return Arrays.asList(
                PERUSTUTKINTO.toString(),
                TELMA.toString(),
                AMMATTITUTKINTO.toString(),
                ERIKOISAMMATTITUTKINTO.toString(),
                VALMA.toString(),
                VAPAASIVISTYSTYO.toString(),
                MAAHANMUUTTAJIENKOTOUTUMISKOULUTUS.toString(),
                VAPAASIVISTYSTYOLUKUTAITO.toString(),
                TUTKINTOONVALMENTAVA.toString()
        );
    }
}
