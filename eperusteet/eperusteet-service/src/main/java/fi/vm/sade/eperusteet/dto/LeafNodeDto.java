package fi.vm.sade.eperusteet.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LeafNodeDto extends AbstractNodeDto {

	private EntityReference tutkinnonOsa;
}
