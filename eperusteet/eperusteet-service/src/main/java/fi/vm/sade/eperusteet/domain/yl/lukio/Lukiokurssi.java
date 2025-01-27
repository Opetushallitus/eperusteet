package fi.vm.sade.eperusteet.domain.yl.lukio;

import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.annotation.RelatesToPeruste;
import fi.vm.sade.eperusteet.domain.validation.ValidHtml;
import fi.vm.sade.eperusteet.domain.validation.ValidHtml.WhitelistType;
import fi.vm.sade.eperusteet.domain.yl.Kurssi;
import fi.vm.sade.eperusteet.domain.yl.TekstiOsa;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

@Entity
@Audited
@PrimaryKeyJoinColumn(name = "id")
@Table(name = "yl_lukiokurssi", schema = "public")
public class Lukiokurssi extends Kurssi {
    public static Predicate<Lukiokurssi> inPeruste(long perusteId) {
        return kurssi -> LukioOpetussuunnitelmaRakenne.inPeruste(perusteId).test(kurssi.getOpetussuunnitelma());
    }

    @Getter
    @Setter
    @NotNull
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private LukiokurssiTyyppi tyyppi;

    @Getter
    @Setter
    @ValidHtml(whitelist = WhitelistType.MINIMAL)
    @JoinColumn(name = "lokalisoitava_koodi_id", nullable = true)
    @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen lokalisoituKoodi;

    @Getter
    @Setter
    @RelatesToPeruste
    @JoinColumn(name = "rakenne_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private LukioOpetussuunnitelmaRakenne opetussuunnitelma;

    @Getter
    @Setter
    @Valid
    @JoinColumn(name = "tavoitteet_id", nullable = true)
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private TekstiOsa tavoitteet;

    @Getter
    @Setter
    @Valid
    @JoinColumn(name = "keskeinen_sisalto_id", nullable = true)
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private TekstiOsa keskeinenSisalto;

    @Getter
    @Setter
    @Valid
    @JoinColumn(name = "tavoitteet_ja_keskeinen_sisalto_id", nullable = true)
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private TekstiOsa tavoitteetJaKeskeinenSisalto;

    @Getter
    @Setter
    @Valid
    @JoinColumn(name = "arviointi_id")
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private TekstiOsa arviointi;

    @Getter
    @Audited
    @OneToMany(mappedBy = "kurssi", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<OppiaineLukiokurssi> oppiaineet = new HashSet<>(0);

    public Lukiokurssi kloonaa(LukioOpetussuunnitelmaRakenne rakenne) {
        Lukiokurssi kopio = new Lukiokurssi();
        kopio.tyyppi = this.tyyppi;
        kopio.nimi = this.nimi;
        kopio.koodiArvo = this.koodiArvo;
        kopio.koodiUri = this.koodiUri;
        kopio.lokalisoituKoodi = this.lokalisoituKoodi;
        kopio.kuvaus = this.kuvaus;
        kopio.tavoitteet = this.tavoitteet;
        kopio.keskeinenSisalto = this.keskeinenSisalto;
        kopio.tavoitteetJaKeskeinenSisalto = this.tavoitteetJaKeskeinenSisalto;
        return kopio;
    }
}
