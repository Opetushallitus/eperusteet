package fi.vm.sade.eperusteet.domain.yl;

import fi.vm.sade.eperusteet.dto.peruste.NavigationType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Getter
@Embeddable
@AllArgsConstructor
@NoArgsConstructor
public class PerusteenMuokkaustietoLisaparametrit implements Serializable {

    @Enumerated(value = EnumType.STRING)
    @NotNull
    private NavigationType kohde;

    @Column(name = "kohde_id")
    @NotNull
    private Long kohdeId;

}
