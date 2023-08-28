package fi.vm.sade.eperusteet.domain;

public enum PoikkeamismaaraysTyyppi {

    EI_TARVITA_OHJETTA("ei_tarvita_ohjetta"),
    EI_VOI_POIKETA("ei_voi_poiketa"),
    KOULUTUSVIENTILIITE("koulutusvientiliite");

    private String tyyppi;

    PoikkeamismaaraysTyyppi(String tyyppi) {
        this.tyyppi = tyyppi;
    }

    @Override
    public String toString() {
        return tyyppi;
    }
}
