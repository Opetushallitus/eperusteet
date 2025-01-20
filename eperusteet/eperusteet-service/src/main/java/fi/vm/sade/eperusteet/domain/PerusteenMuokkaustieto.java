package fi.vm.sade.eperusteet.domain;

import fi.vm.sade.eperusteet.domain.validation.ValidHtml;
import fi.vm.sade.eperusteet.domain.yl.PerusteenMuokkaustietoLisaparametrit;
import fi.vm.sade.eperusteet.dto.peruste.NavigationType;
import java.io.Serializable;
import java.util.Date;
import java.util.Set;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Immutable;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "perusteen_muokkaustieto")
public class PerusteenMuokkaustieto implements Serializable {

    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Id
    private Long id;

    @ValidHtml(whitelist = ValidHtml.WhitelistType.SIMPLIFIED)
    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.LAZY)
    private TekstiPalanen nimi;

    @Enumerated(value = EnumType.STRING)
    @NotNull
    private MuokkausTapahtuma tapahtuma;

    @Column(name = "peruste_id")
    private Long perusteId;

    @Enumerated(value = EnumType.STRING)
    @NotNull
    private NavigationType kohde;

    @Column(updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date luotu;

    private String muokkaaja;

    @Column(name = "kohde_id")
    private Long kohdeId;

    private String lisatieto;

    @NotNull
    private boolean poistettu = false;

    @Immutable
    @CollectionTable(name = "perusteen_muokkaustieto_lisaparametrit")
    @ElementCollection(fetch = FetchType.EAGER)
    private Set<PerusteenMuokkaustietoLisaparametrit> lisaparametrit;
}
