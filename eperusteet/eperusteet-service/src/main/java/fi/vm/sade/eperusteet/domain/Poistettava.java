package fi.vm.sade.eperusteet.domain;

public interface Poistettava {
    Long getId();

    TekstiPalanen getNimi();

    PoistetunTyyppi getPoistetunTyyppi();
}
