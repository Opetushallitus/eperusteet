package fi.vm.sade.eperusteet.resource.util;

import java.util.ArrayList;
import java.util.Collections;

import fi.vm.sade.eperusteet.dto.AbstractNodeDto;
import fi.vm.sade.eperusteet.dto.CompositeNodeDto;
import fi.vm.sade.eperusteet.dto.LeafNodeDto;
import fi.vm.sade.eperusteet.dto.LokalisoituTekstiDto;

public final class RakenneUtil {

	public static CompositeNodeDto getStaticRakenne() {
		CompositeNodeDto parent = new CompositeNodeDto();
		parent.setOtsikko(new LokalisoituTekstiDto(Collections.singletonMap("fi", "Joku perustutkinto")));
		parent.setMuutaTietoa("Tyyppi: ov, maara: 120");
		parent.setOsat(new ArrayList<AbstractNodeDto>());
		
		CompositeNodeDto firstChild = new CompositeNodeDto();
		firstChild.setOtsikko(new LokalisoituTekstiDto(Collections.singletonMap("fi", "Jotkut tutkinnon osat")));
		firstChild.setMuutaTietoa("Tyyppi: ov, maara: 90");
		firstChild.setOsat(new ArrayList<AbstractNodeDto>());
		
		parent.getOsat().add(firstChild);
		
		LeafNodeDto tutkinnonOsa = new LeafNodeDto();
		tutkinnonOsa.setOtsikko(new LokalisoituTekstiDto(Collections.singletonMap("fi", "Perus elektroniikka")));
		tutkinnonOsa.setMuutaTietoa("Tyyppi: ov, maara: 30");
		tutkinnonOsa.setTutkinnonOsaId(1L);
		
		firstChild.getOsat().add(tutkinnonOsa);
		
		CompositeNodeDto secondChild = new CompositeNodeDto();
		secondChild.setMuutaTietoa("Tyyppi: yksi, rajoite: laajuus");
		secondChild.setOsat(new ArrayList<AbstractNodeDto>());
		
		firstChild.getOsat().add(secondChild);
		
		tutkinnonOsa = new LeafNodeDto();
		tutkinnonOsa.setOtsikko(new LokalisoituTekstiDto(Collections.singletonMap("fi", "Sulautetut sovellukset ja projektityöt")));
		tutkinnonOsa.setMuutaTietoa("Tyyppi: ov, maara: 20");
		tutkinnonOsa.setTutkinnonOsaId(2L);
		
		secondChild.getOsat().add(tutkinnonOsa);
		
		tutkinnonOsa = new LeafNodeDto();
		tutkinnonOsa.setOtsikko(new LokalisoituTekstiDto(Collections.singletonMap("fi", "Elektroniikkatuotanto")));
		tutkinnonOsa.setMuutaTietoa("Tyyppi: ov, maara: 20");
		tutkinnonOsa.setTutkinnonOsaId(3L);
		
		secondChild.getOsat().add(tutkinnonOsa);
		
		return parent;
	}
}
