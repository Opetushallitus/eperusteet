package fi.vm.sade.eperusteet.domain;

import fi.vm.sade.eperusteet.domain.validation.ValidHtml;
import fi.vm.sade.eperusteet.domain.validation.ValidateDateRange;
import java.util.Date;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

@Entity
@Table(name = "perusteprojekti")
@ValidateDateRange(start="toimikausiAlku", end="toimikausiLoppu")
@Audited
public class Perusteprojekti extends AbstractAuditedEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Getter
    @Setter
    private Long id;

    @Getter
    @Setter
    @NotNull(message="Nimi ei voi olla tyhjä")
    private String nimi;

    @OneToOne(fetch = FetchType.LAZY, cascade={CascadeType.MERGE, CascadeType.PERSIST})
    @Getter
    @Setter
    private Peruste peruste;

    @Getter
    @Setter
    @Column(unique = true)
    private Diaarinumero diaarinumero;

    @Temporal(TemporalType.TIMESTAMP)
    @Getter
    @Setter
    private Date paatosPvm;

    @Temporal(TemporalType.TIMESTAMP)
    @Getter
    @Setter
    @Column(name="toimikausi_alku")
    private Date toimikausiAlku;

    @Temporal(TemporalType.TIMESTAMP)
    @Getter
    @Setter
    @Column(name="toimikausi_loppu")
    private Date toimikausiLoppu;

    @Getter
    @Setter
    private String tehtavaluokka;

    @Getter
    @Setter
    @Column(name = "ryhmaoid")
    private String ryhmaOid;

    @Getter
    @Setter
    @NotNull
    private boolean esikatseltavissa = false;

    @Getter
    @Setter
    private String tehtava;

    @Getter
    @Setter
    private String yhteistyotaho;

    @Getter
    @Setter
    @Enumerated(EnumType.STRING)
    @NotNull
    private ProjektiTila tila = ProjektiTila.LAADINTA;

    @Getter
    @Setter
    @Enumerated(EnumType.STRING)
    @Column(name = "projekti_kuvaus")
    private ProjektiKuvaus projektiKuvaus = ProjektiKuvaus.PERUSTEEN_KORJAUS;

    @ValidHtml(whitelist = ValidHtml.WhitelistType.SIMPLIFIED)
    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.LAZY)
    @Getter
    @Setter
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @Deprecated
    private TekstiPalanen kuvaus;

    public Perusteprojekti() {
    }

    public Perusteprojekti(String nimi, Peruste peruste, Diaarinumero diaarinumero, Date paatosPvm, Date toimikausiAlku, Date toimikausiLoppu, String tehtavaluokka, String ryhmaOid, String tehtava, String yhteistyotaho) {
        this.nimi = nimi;
        this.peruste = peruste;
        this.diaarinumero = diaarinumero;
        this.paatosPvm = paatosPvm;
        this.toimikausiAlku = toimikausiAlku;
        this.toimikausiLoppu = toimikausiLoppu;
        this.tehtavaluokka = tehtavaluokka;
        this.ryhmaOid = ryhmaOid;
        this.tehtava = tehtava;
        this.yhteistyotaho = yhteistyotaho;
    }
}
