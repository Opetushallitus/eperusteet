package fi.vm.sade.eperusteet.resource.util;

import java.util.ArrayList;
import java.util.Collections;

import fi.vm.sade.eperusteet.dto.AbstractNodeDto;
import fi.vm.sade.eperusteet.dto.CompositeNodeDto;
import fi.vm.sade.eperusteet.dto.EntityReference;
import fi.vm.sade.eperusteet.dto.LeafNodeDto;
import fi.vm.sade.eperusteet.dto.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.dto.SaannostoDto;

public final class RakenneUtil {

	public static CompositeNodeDto getStaticRakenne() {
		CompositeNodeDto parent = new CompositeNodeDto();
		parent.setOtsikko(new LokalisoituTekstiDto(Collections.singletonMap("fi", "Joku perustutkinto")));
		parent.setSaannot(new SaannostoDto("laajuus", "120", "ov"));
		parent.setOsat(new ArrayList<AbstractNodeDto>());
		
		CompositeNodeDto firstChild = new CompositeNodeDto();
		firstChild.setOtsikko(new LokalisoituTekstiDto(Collections.singletonMap("fi", "Jotkut tutkinnon osat")));
		firstChild.setSaannot(new SaannostoDto("laajuus", "90", "ov"));
		firstChild.setOsat(new ArrayList<AbstractNodeDto>());
		
		parent.getOsat().add(firstChild);
		
		LeafNodeDto tutkinnonOsa = new LeafNodeDto();
		tutkinnonOsa.setOtsikko(new LokalisoituTekstiDto(Collections.singletonMap("fi", "Perus elektroniikka")));
		tutkinnonOsa.setSaannot(new SaannostoDto("laajuus", "30", "ov"));
		tutkinnonOsa.setTutkinnonOsa(new EntityReference(1L));
		
		firstChild.getOsat().add(tutkinnonOsa);
		
		CompositeNodeDto secondChild = new CompositeNodeDto();
		secondChild.setSaannot(new SaannostoDto("valitse yksi", null, null));
		secondChild.setOsat(new ArrayList<AbstractNodeDto>());
		
		firstChild.getOsat().add(secondChild);
		
		tutkinnonOsa = new LeafNodeDto();
		tutkinnonOsa.setOtsikko(new LokalisoituTekstiDto(Collections.singletonMap("fi", "Sulautetut sovellukset ja projektityöt")));
		tutkinnonOsa.setSaannot(new SaannostoDto("laajuus", "20", "ov"));
		tutkinnonOsa.setTutkinnonOsa(new EntityReference(2L));
		
		secondChild.getOsat().add(tutkinnonOsa);
		
		tutkinnonOsa = new LeafNodeDto();
		tutkinnonOsa.setOtsikko(new LokalisoituTekstiDto(Collections.singletonMap("fi", "Elektroniikkatuotanto")));
		tutkinnonOsa.setSaannot(new SaannostoDto("laajuus", "20", "ov"));
		tutkinnonOsa.setTutkinnonOsa(new EntityReference(3L));
		
		secondChild.getOsat().add(tutkinnonOsa);
		
		return parent;
	}
}
