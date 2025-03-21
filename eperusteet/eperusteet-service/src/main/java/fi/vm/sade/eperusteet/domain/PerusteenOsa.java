package fi.vm.sade.eperusteet.domain;

import fi.vm.sade.eperusteet.domain.annotation.RelatesToPeruste;
import fi.vm.sade.eperusteet.domain.validation.ValidHtml;
import fi.vm.sade.eperusteet.domain.validation.ValidHtml.WhitelistType;
import static fi.vm.sade.eperusteet.service.util.Util.refXnor;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.hibernate.envers.RelationTargetAuditMode;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Audited
@Table(name = "perusteenosa")
public abstract class PerusteenOsa
        extends AbstractAuditedEntity
        implements Serializable, Mergeable<PerusteenOsa>, WithPerusteTila, ReferenceableEntity, HistoriaTapahtuma, Poistettava {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ValidHtml(whitelist = WhitelistType.MINIMAL)
    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.LAZY)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen nimi;

    @Getter
    @Enumerated(value = EnumType.STRING)
    @NotNull
    private PerusteTila tila = PerusteTila.LUONNOS;

    @Getter
    @Setter
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private Boolean valmis;

    @Getter
    @Setter
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private Boolean kaannettava;

    @Getter
    @Setter
    @Enumerated(value = EnumType.STRING)
    private PerusteenOsaTunniste tunniste;

    @RelatesToPeruste
    @NotAudited
    @Getter
    @OneToMany(mappedBy = "perusteenOsa", fetch = FetchType.LAZY)
    private Set<PerusteenOsaViite> viitteet = new HashSet<>();

    public PerusteenOsa() {
        //JPA
    }

    //copy constuctor
    public PerusteenOsa(PerusteenOsa other) {
        copyState(other);
    }

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public TekstiPalanen getNimi() {
        return nimi;
    }

    public void setNimi(TekstiPalanen nimi) {
        this.nimi = nimi;
    }

    @Override
    public void asetaTila(PerusteTila tila) {
        this.tila = tila;
    }

    public void palautaLuonnokseksi() {
        this.tila = PerusteTila.LUONNOS;
    }

    public boolean structureEquals(PerusteenOsa other) {
        boolean result = getNimi() == null || refXnor(getNimi(), other.getNimi());
        result &= refXnor(getTunniste(), other.getTunniste());
        return result;
    }

    @Override
    public void mergeState(PerusteenOsa updated) {
        if (this.getClass().isAssignableFrom(updated.getClass()) && getId() == null || !getId().equals(updated.getId())) {
            throw new IllegalArgumentException("Vain kahden saman entiteetin tilan voi yhdistää");
        }
        copyState(updated);
    }

    public abstract PerusteenOsa copy();

    // Hibernate proxy korjaussarja
    public Class<? extends PerusteenOsa> getType() {
        return this.getClass();
    }

    private void copyState(PerusteenOsa other) {
        this.nimi = other.getNimi();
        this.valmis = other.getValmis();
        this.kaannettava = other.getKaannettava();
        if (this instanceof TekstiKappale && other instanceof TekstiKappale) {
            TekstiKappale tk = (TekstiKappale) this;
            TekstiKappale otherTk = (TekstiKappale) other;
            tk.setLiite(otherTk.isLiite());
        }
    }

    @Override
    public PoistetunTyyppi getPoistetunTyyppi() {
        return null;
    }

}

