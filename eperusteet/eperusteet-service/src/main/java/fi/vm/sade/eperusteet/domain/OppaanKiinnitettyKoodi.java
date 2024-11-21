package fi.vm.sade.eperusteet.domain;

import fi.vm.sade.eperusteet.domain.validation.ValidKoodisto;
import fi.vm.sade.eperusteet.dto.koodisto.KoodistoUriArvo;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

@Entity
@Audited
@Getter
@Setter
@Table(name = "opas_kiinnitetty_koodi")
public class OppaanKiinnitettyKoodi extends AbstractAuditedReferenceableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @NotNull
    private OpasSisalto opasSisalto;

    @Enumerated(EnumType.STRING)
    @NotNull
    private KiinnitettyKoodiTyyppi kiinnitettyKoodiTyyppi;

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.LAZY)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @ValidKoodisto(koodisto = {KoodistoUriArvo.TUTKINNONOSAT, KoodistoUriArvo.OSAAMISALA, KoodistoUriArvo.OPPIAINEETJAOPPIMAARATLOPS2021, KoodistoUriArvo.OPINTOKOKONAISUUSNIMET, KoodistoUriArvo.KOULUTUKSENOSATTUVA})
    private Koodi koodi;

    public OppaanKiinnitettyKoodi copy(OpasSisalto opasSisalto) {
        OppaanKiinnitettyKoodi oppaanKiinnitettyKoodi = new OppaanKiinnitettyKoodi();
        oppaanKiinnitettyKoodi.setOpasSisalto(opasSisalto);
        oppaanKiinnitettyKoodi.setKiinnitettyKoodiTyyppi(kiinnitettyKoodiTyyppi);
        oppaanKiinnitettyKoodi.setKoodi(koodi);
        return oppaanKiinnitettyKoodi;
    }
}
