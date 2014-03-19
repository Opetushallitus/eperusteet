package fi.vm.sade.eperusteet.domain.audit;

import java.io.Serializable;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Revision implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private Long id;
	private Date date;
	
	public Revision(Long id, Date date) {
		this.id = id;
		this.date = date;
	}
}
