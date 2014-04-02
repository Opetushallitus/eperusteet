package fi.vm.sade.eperusteet.resource.util;

import fi.vm.sade.eperusteet.dto.AbstractRakenneosaDto;
import java.util.ArrayList;
import java.util.Collections;

import fi.vm.sade.eperusteet.dto.EntityReference;
import fi.vm.sade.eperusteet.dto.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.dto.RakenteenHaaraDto;
import fi.vm.sade.eperusteet.dto.RakenteenLehtiDto;
import fi.vm.sade.eperusteet.dto.SaannostoDto;

public final class RakenneUtil {

	public static RakenteenHaaraDto getStaticRakenneDto() {
		RakenteenHaaraDto parent = new RakenteenHaaraDto();
		parent.setOtsikko(new LokalisoituTekstiDto(Collections.singletonMap("fi", "Joku perustutkinto")));
		parent.setKuvaus(new LokalisoituTekstiDto(Collections.singletonMap("fi", "Opiskellaan jotain jotain perustutkintoa varten.")));
		parent.setSaannot(new SaannostoDto("laajuus", "120", "ov"));
		parent.setOsat(new ArrayList<AbstractRakenneosaDto>());

		RakenteenHaaraDto firstChild = new RakenteenHaaraDto();
		firstChild.setOtsikko(new LokalisoituTekstiDto(Collections.singletonMap("fi", "Jotkut tutkinnon osat")));
		firstChild.setSaannot(new SaannostoDto("laajuus", "90", "ov"));
		firstChild.setOsat(new ArrayList<AbstractRakenneosaDto>());
		parent.getOsat().add(firstChild);

		RakenteenLehtiDto tutkinnonOsa = new RakenteenLehtiDto();
		tutkinnonOsa.setOtsikko(new LokalisoituTekstiDto(Collections.singletonMap("fi", "Perus elektroniikka")));
		tutkinnonOsa.setSaannot(new SaannostoDto("laajuus", "30", "ov"));
		tutkinnonOsa.setPerusteenOsa(new EntityReference(1L));
		firstChild.getOsat().add(tutkinnonOsa);

		RakenteenHaaraDto secondChild = new RakenteenHaaraDto();
		secondChild.setSaannot(new SaannostoDto("maara", "1", null));
		secondChild.setOsat(new ArrayList<AbstractRakenneosaDto>());
		firstChild.getOsat().add(secondChild);

		tutkinnonOsa = new RakenteenLehtiDto();
		tutkinnonOsa.setOtsikko(new LokalisoituTekstiDto(Collections.singletonMap("fi", "Sulautetut sovellukset ja projektityöt")));
		tutkinnonOsa.setSaannot(new SaannostoDto("laajuus", "20", "ov"));
		tutkinnonOsa.setPerusteenOsa(new EntityReference(2L));
		secondChild.getOsat().add(tutkinnonOsa);

		tutkinnonOsa = new RakenteenLehtiDto();
		tutkinnonOsa.setOtsikko(new LokalisoituTekstiDto(Collections.singletonMap("fi", "Elektroniikkatuotanto")));
		tutkinnonOsa.setSaannot(new SaannostoDto("laajuus", "20", "ov"));
		tutkinnonOsa.setPerusteenOsa(new EntityReference(3L));
		secondChild.getOsat().add(tutkinnonOsa);

		RakenteenHaaraDto thirdChild = new RakenteenHaaraDto();
		thirdChild.setSaannot(new SaannostoDto("laajuus", "40", "ov"));
		thirdChild.setOsat(new ArrayList<AbstractRakenneosaDto>());
		firstChild.getOsat().add(thirdChild);

		tutkinnonOsa = new RakenteenLehtiDto();
		tutkinnonOsa.setOtsikko(new LokalisoituTekstiDto(Collections.singletonMap("fi", "Dataustuotanto")));
		tutkinnonOsa.setSaannot(new SaannostoDto("laajuus", "20", "ov"));
		tutkinnonOsa.setPerusteenOsa(new EntityReference(3L));
                thirdChild.getOsat().add(tutkinnonOsa);

		tutkinnonOsa = new RakenteenLehtiDto();
		tutkinnonOsa.setOtsikko(new LokalisoituTekstiDto(Collections.singletonMap("fi", "Dataustoteutus")));
		tutkinnonOsa.setSaannot(new SaannostoDto("laajuus", "20", "ov"));
		tutkinnonOsa.setPerusteenOsa(new EntityReference(3L));
                thirdChild.getOsat().add(tutkinnonOsa);

		return parent;
	}
}
