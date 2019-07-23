package fi.vm.sade.eperusteet.domain;


import fi.vm.sade.eperusteet.domain.arviointi.ArviointiAsteikko;
import fi.vm.sade.eperusteet.domain.validation.ValidHtml;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Audited
@Table(name = "geneerinenarviointiasteikko")
@Entity
public class GeneerinenArviointiasteikko extends AbstractAuditedReferenceableEntity implements Copyable<GeneerinenArviointiasteikko> {

    @ValidHtml(whitelist = ValidHtml.WhitelistType.MINIMAL)
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @Getter
    @Setter
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @NotNull(groups = { Peruste.Valmis.class, Peruste.ValmisPohja.class, Peruste.ValmisPohja.class })
    private TekstiPalanen nimi;

    @ValidHtml(whitelist = ValidHtml.WhitelistType.MINIMAL)
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @Getter
    @Setter
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @NotNull(groups = { Peruste.Valmis.class, Peruste.ValmisPohja.class, Peruste.ValmisPohja.class })
    private TekstiPalanen kohde;

    @ManyToOne(fetch = FetchType.EAGER)
    @NotNull
    @Getter
    @Setter
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private ArviointiAsteikko arviointiAsteikko;

    @OneToMany(cascade = CascadeType.ALL)
    @Getter
    @Setter
    private Set<GeneerisenOsaamistasonKriteeri> osaamistasonKriteerit = new HashSet<>();

    @Getter
    private boolean julkaistu;

    public void setJulkaistu(boolean julkaistu) {
        if (!this.julkaistu) {
            this.julkaistu = julkaistu;
        }
    }

    @Override
    public GeneerinenArviointiasteikko copy(boolean deep) {
        GeneerinenArviointiasteikko uusi = new GeneerinenArviointiasteikko();
        uusi.setJulkaistu(false);
        uusi.setArviointiAsteikko(this.arviointiAsteikko);
        uusi.setNimi(this.getNimi());
        uusi.setKohde(this.getKohde());
        uusi.setOsaamistasonKriteerit(this.getOsaamistasonKriteerit().stream()
            .map(GeneerisenOsaamistasonKriteeri::copy)
            .collect(Collectors.toSet()));
        return uusi;
    }
}
