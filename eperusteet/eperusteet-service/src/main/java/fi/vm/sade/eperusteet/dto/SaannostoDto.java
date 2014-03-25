package fi.vm.sade.eperusteet.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SaannostoDto {

	private String tyyppi;
	private String maara;
	private String yksikko;
	
	public SaannostoDto() {}
	
	public SaannostoDto(String tyyppi, String maara, String yksikko) {
		super();
		this.tyyppi = tyyppi;
		this.maara = maara;
		this.yksikko = yksikko;
	}
	
	
}
