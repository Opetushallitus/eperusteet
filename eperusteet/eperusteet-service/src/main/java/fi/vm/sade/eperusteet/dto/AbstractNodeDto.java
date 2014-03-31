package fi.vm.sade.eperusteet.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class AbstractNodeDto {

	private LokalisoituTekstiDto otsikko;
	private LokalisoituTekstiDto kuvaus;
	private SaannostoDto saannot;
}
