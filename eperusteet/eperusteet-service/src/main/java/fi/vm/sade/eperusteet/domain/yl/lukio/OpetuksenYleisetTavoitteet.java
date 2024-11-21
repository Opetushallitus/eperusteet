package fi.vm.sade.eperusteet.domain.yl.lukio;

import fi.vm.sade.eperusteet.domain.PerusteenOsa;
import fi.vm.sade.eperusteet.domain.PerusteenOsaViite;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.annotation.RelatesToPeruste;
import fi.vm.sade.eperusteet.domain.validation.ValidHtml;
import fi.vm.sade.eperusteet.dto.Reference;
import fi.vm.sade.eperusteet.dto.peruste.NavigationType;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Audited
@Table(name = "yl_lukiokoulutuksen_opetuksen_yleiset_tavoitteet", schema = "public")
public class OpetuksenYleisetTavoitteet extends PerusteenOsa {

    @Column(name = "tunniste", nullable = false, unique = true, updatable = false)
    @Getter
    private UUID uuidTunniste = UUID.randomUUID();

    @Getter
    @Setter
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @ValidHtml(whitelist = ValidHtml.WhitelistType.MINIMAL)
    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(name = "otsikko_id", nullable = true)
    private TekstiPalanen otsikko;

    @Getter
    @Setter
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @ValidHtml
    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(name = "kuvaus_id")
    private TekstiPalanen kuvaus;

    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST})
    @Getter
    @Setter
    @JoinColumn(name="viite_id", nullable = false)
    private PerusteenOsaViite viite = new PerusteenOsaViite();

    @RelatesToPeruste
    @Getter
    @Setter
    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(name = "sisalto_id", nullable = false)
    private LukiokoulutuksenPerusteenSisalto sisalto;

    public OpetuksenYleisetTavoitteet kloonaa() {
        OpetuksenYleisetTavoitteet klooni = new OpetuksenYleisetTavoitteet();
        klooni.setKuvaus(this.getKuvaus());
        klooni.setOtsikko(this.getOtsikko());
        return klooni;
    }

    @Override
    public PerusteenOsa copy() {
        return kloonaa();
    }

    @Override
    public Reference getReference() {
        return new Reference(getId());
    }

    @Override
    public NavigationType getNavigationType() {
        return NavigationType.opetuksenyleisettavoitteet;
    }
}
