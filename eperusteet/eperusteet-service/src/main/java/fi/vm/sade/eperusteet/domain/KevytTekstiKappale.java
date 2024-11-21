package fi.vm.sade.eperusteet.domain;

import fi.vm.sade.eperusteet.domain.validation.ValidHtml;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import java.io.Serializable;

@Entity
@Table(name = "kevyttekstikappale")
@Audited
public class KevytTekstiKappale extends AbstractAuditedEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Getter
    @Setter
    private Long id;

    @ValidHtml(whitelist = ValidHtml.WhitelistType.MINIMAL)
    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.LAZY)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @Getter
    @Setter
    private TekstiPalanen nimi;

    @ValidHtml
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @Getter
    @Setter
    private TekstiPalanen teksti;

    @Getter
    @Setter
    private Integer jnro;

    public KevytTekstiKappale() {
    }

    public KevytTekstiKappale(KevytTekstiKappale other) {
        copyState(other);
    }

    public KevytTekstiKappale copy() { return new KevytTekstiKappale(this); }

    private void copyState(KevytTekstiKappale other) {
        this.setNimi(other.getNimi());
        this.setTeksti(other.getTeksti());
        this.setJnro(other.getJnro());
    }

    public static KevytTekstiKappale getCopy(KevytTekstiKappale other) {
        if (other == null) {
            return null;
        }
        else {
            return new KevytTekstiKappale(other);
        }
    }

}
