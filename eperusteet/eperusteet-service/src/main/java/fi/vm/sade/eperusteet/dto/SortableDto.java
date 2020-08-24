package fi.vm.sade.eperusteet.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SortableDto implements Sortable {
    private Long id;
    private Integer jarjestys;
}
