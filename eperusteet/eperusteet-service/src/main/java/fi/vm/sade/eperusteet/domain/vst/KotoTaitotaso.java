package fi.vm.sade.eperusteet.domain.vst;

import fi.vm.sade.eperusteet.domain.AbstractAuditedEntity;
import fi.vm.sade.eperusteet.domain.Koodi;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.validation.ValidHtml;
import fi.vm.sade.eperusteet.domain.validation.ValidKoodisto;
import fi.vm.sade.eperusteet.dto.koodisto.KoodistoUriArvo;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

@Entity
@Table(name = "koto_taitotaso")
@Audited
@Getter
@Setter
@NoArgsConstructor
public class KotoTaitotaso extends AbstractAuditedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @ValidKoodisto(koodisto = KoodistoUriArvo.KOTOUTUMISKOULUTUSTAVOITTEET)
    private Koodi nimi;

    @ValidHtml
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen tavoitteet;

    @Deprecated //Tulee poistumaan kunhan uudet koton kentät on toteutettu
    @ValidHtml
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen kielenkayttotarkoitus;

    @Deprecated //Tulee poistumaan kunhan uudet koton kentät on toteutettu
    @ValidHtml
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen aihealueet;

    @Deprecated //Tulee poistumaan kunhan uudet koton kentät on toteutettu
    @ValidHtml
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen viestintataidot;

    @Deprecated //Tulee poistumaan kunhan uudet koton kentät on toteutettu
    @ValidHtml
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen opiskelijantaidot;

    @ValidHtml
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen opiskelijanTyoelamataidot;

    private Integer tyoelamaOpintoMinimiLaajuus;
    private Integer tyoelamaOpintoMaksimiLaajuus;

    @ValidHtml
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen suullinenVastaanottaminen;

    @ValidHtml
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen suullinenTuottaminen;

    @ValidHtml
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen vuorovaikutusJaMediaatio;

    public KotoTaitotaso(KotoTaitotaso other) {
        this.nimi = other.getNimi();
        this.tavoitteet = other.getTavoitteet();
        this.kielenkayttotarkoitus = other.getKielenkayttotarkoitus();
        this.aihealueet = other.getAihealueet();
        this.viestintataidot = other.getViestintataidot();
        this.opiskelijantaidot = other.getOpiskelijantaidot();
        this.opiskelijanTyoelamataidot = other.getOpiskelijanTyoelamataidot();
        this.tyoelamaOpintoMinimiLaajuus = other.getTyoelamaOpintoMinimiLaajuus();
        this.tyoelamaOpintoMaksimiLaajuus = other.getTyoelamaOpintoMaksimiLaajuus();
        this.suullinenVastaanottaminen = other.getSuullinenVastaanottaminen();
        this.suullinenTuottaminen = other.getSuullinenTuottaminen();
        this.vuorovaikutusJaMediaatio = other.getVuorovaikutusJaMediaatio();
    }
}
