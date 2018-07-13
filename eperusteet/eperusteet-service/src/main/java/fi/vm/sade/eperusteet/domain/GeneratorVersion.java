package fi.vm.sade.eperusteet.domain;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * @author isaul
 */
public enum GeneratorVersion {
    VANHA("vanha"),
    UUSI("uusi"),
    KVLIITE("kvliite");

    private final String version;

    GeneratorVersion(String version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return version;
    }

    @JsonCreator
    public static GeneratorVersion of(String version) {
        for (GeneratorVersion s : values()) {
            if (s.version.equalsIgnoreCase(version)) {
                return s;
            }
        }

        throw new IllegalArgumentException(version + " ei ole kelvollinen GeneratorVersion");
    }
}
