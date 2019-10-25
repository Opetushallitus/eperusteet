package fi.vm.sade.eperusteet.dto.peruste;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class NavigationNodeDto {
    private Long key;
    private String label;
    private NavigationType type;
    private List<NavigationNodeDto> children = new ArrayList<>();
    private List<NavigationNodeDto> path = new ArrayList<>();
}
