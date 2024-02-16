package fi.vm.sade.eperusteet.domain;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Koulutustyyppi ei enää yksilöi toteutusta ja toteutus voi olla jaettu eri koulutustyyppien välillä.
 */
public enum KoulutustyyppiToteutus {
    YKSINKERTAINEN("yksinkertainen"), // Sisältää ainoastaan tekstikappaleita
    PERUSOPETUS("perusopetus"),
    LOPS("lops"),
    AMMATILLINEN("ammatillinen"),
    TPO("taiteenperusopetus"),
    LOPS2019("lops2019"),
    VAPAASIVISTYSTYO("vapaasivistystyo"),
    TUTKINTOONVALMENTAVA("tutkintoonvalmentava"),
    KOTOUTUMISKOULUTUS("kotoutumiskoulutus");

    private final String tyyppi;

    KoulutustyyppiToteutus(String tyyppi) {
        this.tyyppi = tyyppi;
    }

    @JsonCreator
    public static KoulutustyyppiToteutus of(String tila) {
        for (KoulutustyyppiToteutus s : values()) {
            if (s.tyyppi.equalsIgnoreCase(tila)) {
                return s;
            }
        }
        throw new IllegalArgumentException(tila + " ei ole kelvollinen toteutus");
    }

    @Override
    public String toString() {
        return tyyppi;
    }

}
