package fi.vm.sade.eperusteet.dto.yl;

import com.google.common.collect.Sets;
import fi.vm.sade.eperusteet.dto.ReferenceableDto;
import fi.vm.sade.eperusteet.dto.Reference;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OpetuksenTavoiteDto implements ReferenceableDto {

    private Long id;
    private UUID tunniste;
    private Optional<LokalisoituTekstiDto> tavoite;
    private Set<Reference> sisaltoalueet;
    private Set<Reference> laajattavoitteet;
    private Set<Reference> kohdealueet;
    private Set<TavoitteenArviointiDto> arvioinninkohteet;
    private Optional<LokalisoituTekstiDto> arvioinninOtsikko;
    private Optional<LokalisoituTekstiDto> arvioinninKuvaus;
    private Optional<LokalisoituTekstiDto> arvioinninOsaamisenKuvaus;
    private Optional<LokalisoituTekstiDto> tavoitteistaJohdetutOppimisenTavoitteet;
    private Optional<LokalisoituTekstiDto> vapaaTeksti;
    private List<OppiaineenTavoitteenOpetuksenTavoiteDto> oppiaineenTavoitteenOpetuksenTavoitteet = new ArrayList<>();

    public Set<TavoitteenArviointiDto> getArvioinninkohteet() {
        if (arvioinninkohteet == null) {
            return Sets.newLinkedHashSet();
        }

        return arvioinninkohteet.stream()
                .sorted(Comparator.comparing(arviointi -> arviointi.getArvosana().orElse(0)))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
