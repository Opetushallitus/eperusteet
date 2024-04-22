package fi.vm.sade.eperusteet.domain.tutkinnonosa;

import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.validation.ValidHtml;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

@Entity
@Table(name = "osaalue_valmatelma")
@Audited
public class ValmaTelmaSisalto implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Getter
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @Getter
    @Setter
    @JoinColumn(name = "osaamisenarviointi_id")
    private OsaamisenArviointi osaamisenarviointi;

    @Getter
    @Setter
    @ValidHtml(whitelist = ValidHtml.WhitelistType.SIMPLIFIED)
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen osaamisenarviointiTekstina;

    @OneToMany(fetch = FetchType.LAZY, cascade = {CascadeType.ALL})
    @JoinTable(name = "valmatelma_osaamisentavoite_osaalue_valmatelma",
            joinColumns = @JoinColumn(name = "valmatelmasisalto_id"),
            inverseJoinColumns = @JoinColumn(name = "osaamisentavoite_id"))
    @Getter
    @Setter
    @OrderColumn(name = "jarjestys")
    private List<OsaamisenTavoite> osaamistavoite = new ArrayList<>();

    public ValmaTelmaSisalto() {
    }

    public ValmaTelmaSisalto(ValmaTelmaSisalto other) {
        this.osaamisenarviointiTekstina = other.getOsaamisenarviointiTekstina();
        this.osaamisenarviointi = new OsaamisenArviointi(other.getOsaamisenarviointi());
        if (other.getOsaamistavoite() != null) {
            this.osaamistavoite = other.getOsaamistavoite().stream()
                    .map(ot -> new OsaamisenTavoite(ot))
                    .collect(Collectors.toList());
        }
    }

}
