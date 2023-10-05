package fi.vm.sade.eperusteet.domain.maarays;

import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.validation.ValidHtml;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.sql.Blob;
import java.util.UUID;

@Entity
@Setter
@Getter
@Table(name = "maarays_liite")
@AllArgsConstructor
@NoArgsConstructor
public class MaaraysLiite implements Serializable {

    @Id
    @Column(updatable = false)
    private UUID id;

    @ValidHtml(whitelist = ValidHtml.WhitelistType.MINIMAL)
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private TekstiPalanen nimi;

    @Size(max = 1024)
    private String tiedostonimi;

    @NotNull
    @Enumerated(EnumType.STRING)
    private MaaraysLiiteTyyppi tyyppi;

    @Basic(fetch = FetchType.LAZY, optional = false)
    @Column(updatable = false, nullable = false)
    @Lob
    @NotNull
    private Blob data;

}
