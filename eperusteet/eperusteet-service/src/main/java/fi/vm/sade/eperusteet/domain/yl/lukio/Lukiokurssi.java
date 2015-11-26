/*
 * Copyright (c) 2013 The Finnish Board of Education - Opetushallitus
 *
 * This program is free software: Licensed under the EUPL, Version 1.1 or - as
 * soon as they will be approved by the European Commission - subsequent versions
 * of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at: http://ec.europa.eu/idabc/eupl
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * European Union Public Licence for more details.
 */
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

import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

/**
 * User: tommiratamaa
 * Date: 9.9.15
 * Time: 10.22
 */
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
