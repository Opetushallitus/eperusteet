package fi.vm.sade.eperusteet.domain;

import fi.vm.sade.eperusteet.dto.peruste.NavigationType;
import fi.vm.sade.eperusteet.service.util.SecurityUtil;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HistoriaTapahtumaAuditointitiedoilla implements HistoriaTapahtuma {

    private Date luotu;
    private Date muokattu;
    private String luoja;
    private String muokkaaja;

    private Long id;
    private TekstiPalanen nimi;
    private NavigationType navigationType;

    public HistoriaTapahtumaAuditointitiedoilla(HistoriaTapahtuma historiaTapahtuma) {
        this.luotu = new Date();
        this.muokattu = new Date();
        this.luoja = SecurityUtil.getAuthenticatedPrincipal().getName();
        this.muokkaaja = SecurityUtil.getAuthenticatedPrincipal().getName();

        this.id = historiaTapahtuma.getId();
        this.nimi = historiaTapahtuma.getNimi();
        this.navigationType = historiaTapahtuma.getNavigationType();
    }

    public HistoriaTapahtumaAuditointitiedoilla(Long id, TekstiPalanen nimi, NavigationType navigationType) {
        this.luotu = new Date();
        this.muokattu = new Date();

        this.id = id;
        this.nimi = nimi;
        this.navigationType = navigationType;
    }
}
