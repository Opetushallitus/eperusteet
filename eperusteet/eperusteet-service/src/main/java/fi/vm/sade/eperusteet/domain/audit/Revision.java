package fi.vm.sade.eperusteet.domain.audit;

import java.io.Serializable;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Revision implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private Integer number;
	private Date date;
	
	public Revision(Integer number, Date date) {
		this.number = number;
		this.date = date;
	}
}
