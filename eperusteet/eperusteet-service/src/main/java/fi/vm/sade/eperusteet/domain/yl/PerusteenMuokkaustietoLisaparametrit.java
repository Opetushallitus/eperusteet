package fi.vm.sade.eperusteet.domain.yl;

import fi.vm.sade.eperusteet.dto.peruste.NavigationType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;
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
