package fi.vm.sade.eperusteet.domain;

import fi.vm.sade.eperusteet.domain.validation.ValidHtml;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

/**
 *
 * @author mikkom
 */
@Entity
@Table(name="tiedote")
@Audited
public class Tiedote extends AbstractAuditedEntity {
    @Getter
    @Setter
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Getter
    @Setter
    private boolean julkinen;

    @Getter
    @Setter
    private boolean yleinen; // Jos tiedoite on tarkoitettu etusivulle

    @Getter
    @Setter
    @ValidHtml(whitelist = ValidHtml.WhitelistType.MINIMAL)
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen otsikko;

    @Getter
    @Setter
    @ValidHtml(whitelist = ValidHtml.WhitelistType.NORMAL)
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen sisalto;

    @Getter
    @Setter
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @OneToOne(fetch = FetchType.LAZY)
    private Perusteprojekti perusteprojekti;
}
