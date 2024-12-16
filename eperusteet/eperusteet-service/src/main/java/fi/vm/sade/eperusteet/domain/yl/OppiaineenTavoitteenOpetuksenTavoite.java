package fi.vm.sade.eperusteet.domain.yl;

import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.validation.ValidHtml;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

@Entity
@Table(name = "yl_oppiaineen_tavoitteen_opetuksen_tavoite")
@Getter
@Setter
public class OppiaineenTavoitteenOpetuksenTavoite implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @NotNull
    @ValidHtml(whitelist = ValidHtml.WhitelistType.SIMPLIFIED)
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.EAGER)
    private TekstiPalanen tavoite;
}
