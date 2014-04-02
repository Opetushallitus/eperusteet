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
package fi.vm.sade.eperusteet.domain;

import com.fasterxml.jackson.annotation.JsonTypeName;
import fi.vm.sade.eperusteet.dto.EntityReference;
import fi.vm.sade.eperusteet.domain.validation.ValidHtml;
import java.io.Serializable;
import java.util.Objects;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

/**
 *
 * @author jhyoty
 */
@Entity
@Table(name = "tutkinnonosa")
@JsonTypeName("tutkinnonosa")
@Audited
public class TutkinnonOsa extends PerusteenOsa implements Serializable {

    private static final long serialVersionUID = 1L;

    @ValidHtml
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen tavoitteet;

    @ValidHtml
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen ammattitaitovaatimukset;

    @ValidHtml
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen ammattitaidonOsoittamistavat;

    @ValidHtml
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen osaamisala;

    @Column
    private Long opintoluokitus;

    @Column
    private String koodiUri;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    //Hibernate bug: orphanRemoval ei toimi jos fetchMode = Lazy
    private Arviointi arviointi;

	@Override
	public EntityReference getReference() {
		return new EntityReference(getId());
	}

    public TekstiPalanen getTavoitteet() {
        return tavoitteet;
    }

    public void setTavoitteet(TekstiPalanen tavoitteet) {
        this.tavoitteet = tavoitteet;
    }

    public TekstiPalanen getAmmattitaitovaatimukset() {
        return ammattitaitovaatimukset;
    }

    public void setAmmattitaitovaatimukset(TekstiPalanen ammattitaitovaatimukset) {
        this.ammattitaitovaatimukset = ammattitaitovaatimukset;
    }

    public TekstiPalanen getAmmattitaidonOsoittamistavat() {
        return ammattitaidonOsoittamistavat;
    }

    public void setAmmattitaidonOsoittamistavat(TekstiPalanen ammattitaidonOsoittamistavat) {
        this.ammattitaidonOsoittamistavat = ammattitaidonOsoittamistavat;
    }

    public TekstiPalanen getOsaamisala() {
        return osaamisala;
    }

    public void setOsaamisala(TekstiPalanen osaamisala) {
        this.osaamisala = osaamisala;
    }

    public Long getOpintoluokitus() {
        return opintoluokitus;
    }

    public void setOpintoluokitus(Long opintoluokitus) {
        this.opintoluokitus = opintoluokitus;
    }

    public String getKoodiUri() {
        return koodiUri;
    }

    public void setKoodiUri(String koodiUri) {
        this.koodiUri = koodiUri;
    }

    public Arviointi getArviointi() {
        return arviointi;
    }

    public void setArviointi(Arviointi arviointi) {
        if (!Objects.equals(this.arviointi, arviointi)) {
            this.arviointi = arviointi;
        }
    }

    @Override
    public void mergeState(PerusteenOsa perusteenOsa) {
        super.mergeState(perusteenOsa);
        if (perusteenOsa instanceof TutkinnonOsa) {
            TutkinnonOsa other = (TutkinnonOsa) perusteenOsa;
            this.setArviointi(other.getArviointi());
            this.setAmmattitaitovaatimukset(other.getAmmattitaitovaatimukset());
            this.setAmmattitaidonOsoittamistavat(other.getAmmattitaidonOsoittamistavat());
            this.setTavoitteet(other.getTavoitteet());
            this.setKoodiUri(other.getKoodiUri());
            this.setOpintoluokitus(other.getOpintoluokitus());
            this.setOsaamisala(other.getOsaamisala());
        } else {
            throw new IllegalArgumentException("Oletettiin " + this.getClass().getSimpleName() + ", oli "
                + perusteenOsa.getClass().getSimpleName());
        }
    }

}
