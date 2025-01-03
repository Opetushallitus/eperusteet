package fi.vm.sade.eperusteet.dto.peruste;

import fi.vm.sade.eperusteet.domain.PerusteTyyppi;
import fi.vm.sade.eperusteet.domain.ProjektiTila;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PerusteprojektiQueryDto {
    private int sivu = 0;
    private int sivukoko = 25;
    private String nimi;
    private List<PerusteTyyppi> tyyppi = new ArrayList<>();
    private Set<ProjektiTila> tila;
    private Set<String> koulutustyyppi;
    private String jarjestysTapa;
    private Boolean jarjestysOrder;
    private Set<Long> perusteet;
    private boolean tuleva;
    private boolean voimassaolo;
    private boolean siirtyma;
    private boolean poistunut;
}
