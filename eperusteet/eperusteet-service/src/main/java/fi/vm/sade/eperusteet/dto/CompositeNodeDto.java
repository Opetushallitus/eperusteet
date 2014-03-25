package fi.vm.sade.eperusteet.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CompositeNodeDto extends AbstractNodeDto{

	private List<AbstractNodeDto> osat;
}
