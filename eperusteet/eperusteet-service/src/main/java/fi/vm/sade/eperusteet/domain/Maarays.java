package fi.vm.sade.eperusteet.domain;

import fi.vm.sade.eperusteet.domain.validation.ValidHtml;
import fi.vm.sade.eperusteet.service.util.SecurityUtil;
import java.util.Date;
import java.util.Map;
import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "maarays")
public class Maarays {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ValidHtml(whitelist = ValidHtml.WhitelistType.MINIMAL)
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private TekstiPalanen nimi;

    @CollectionTable(name = "maarays_url")
    @ElementCollection(fetch = FetchType.EAGER)
    private Map<Kieli, String> url;

    @Temporal(TemporalType.TIMESTAMP)
    private Date muokattu;

    private String muokkaaja;

    @PrePersist
    @PreUpdate
    protected void preupdate() {
        muokattu = new Date();
        muokkaaja = SecurityUtil.getAuthenticatedPrincipal().getName();
    }
}
