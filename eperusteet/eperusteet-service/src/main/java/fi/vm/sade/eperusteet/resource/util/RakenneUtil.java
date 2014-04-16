package fi.vm.sade.eperusteet.resource.util;

import fi.vm.sade.eperusteet.domain.LaajuusYksikko;
import fi.vm.sade.eperusteet.dto.EntityReference;
import fi.vm.sade.eperusteet.dto.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.AbstractRakenneOsaDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.MuodostumisSaantoDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.RakenneModuuliDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.RakenneOsaDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.TutkinnonOsaViiteDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.TutkinnonRakenneDto;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class RakenneUtil {

	public static TutkinnonRakenneDto getStaticRakenneDto() {
        TutkinnonRakenneDto rakenne = new TutkinnonRakenneDto();
		RakenneModuuliDto parent = new RakenneModuuliDto();
        rakenne.setRakenne(parent);
        List<TutkinnonOsaViiteDto> osat = new ArrayList<>();
        rakenne.setTutkinnonOsat(osat);

		parent.setNimi(new LokalisoituTekstiDto(Collections.singletonMap("fi", "Joku perustutkinto")));
		parent.setKuvaus(new LokalisoituTekstiDto(Collections.singletonMap("fi", "Opiskellaan jotain jotain perustutkintoa varten.")));
		parent.setMuodostumisSaanto(new MuodostumisSaantoDto(new MuodostumisSaantoDto.Laajuus(120, 120, LaajuusYksikko.OPINTOVIIKKO)));
		parent.setOsat(new ArrayList<AbstractRakenneOsaDto>());

		RakenneModuuliDto firstChild = new RakenneModuuliDto();
		firstChild.setNimi(new LokalisoituTekstiDto(Collections.singletonMap("fi", "Jotkut tutkinnon osat")));
		firstChild.setMuodostumisSaanto(new MuodostumisSaantoDto(new MuodostumisSaantoDto.Laajuus(120, 120, LaajuusYksikko.OPINTOVIIKKO)));
		firstChild.setOsat(new ArrayList<AbstractRakenneOsaDto>());
		parent.getOsat().add(firstChild);

		TutkinnonOsaViiteDto tutkinnonOsa = new TutkinnonOsaViiteDto();
		tutkinnonOsa.setNimi(new LokalisoituTekstiDto(Collections.singletonMap("fi", "Perus elektroniikka")));
		tutkinnonOsa.setLaajuus(30);
        tutkinnonOsa.setYksikko(LaajuusYksikko.OPINTOVIIKKO);
		tutkinnonOsa.setTutkinnonOsa(new EntityReference(1L));
        osat.add(tutkinnonOsa);
        RakenneOsaDto r = new RakenneOsaDto();
        r.setTutkinnonOsa(tutkinnonOsa.getTutkinnonOsa());

		RakenneModuuliDto secondChild = new RakenneModuuliDto();
		secondChild.setMuodostumisSaanto(new MuodostumisSaantoDto(new MuodostumisSaantoDto.Koko(1, 2)));
		secondChild.setOsat(new ArrayList<AbstractRakenneOsaDto>());
		firstChild.getOsat().add(secondChild);

		tutkinnonOsa = new TutkinnonOsaViiteDto();
		tutkinnonOsa.setNimi(new LokalisoituTekstiDto(Collections.singletonMap("fi", "Sulautetut sovellukset ja projektityöt")));
		tutkinnonOsa.setLaajuus(20);
        tutkinnonOsa.setYksikko(LaajuusYksikko.OPINTOVIIKKO);
		tutkinnonOsa.setTutkinnonOsa(new EntityReference(2L));
        osat.add(tutkinnonOsa);
        r = new RakenneOsaDto();
        r.setTutkinnonOsa(tutkinnonOsa.getTutkinnonOsa());
		secondChild.getOsat().add(r);

		tutkinnonOsa = new TutkinnonOsaViiteDto();
		tutkinnonOsa.setNimi(new LokalisoituTekstiDto(Collections.singletonMap("fi", "Elektroniikkatuotanto")));
		tutkinnonOsa.setLaajuus(20);
        tutkinnonOsa.setYksikko(LaajuusYksikko.OPINTOVIIKKO);
		tutkinnonOsa.setTutkinnonOsa(new EntityReference(3L));
        osat.add(tutkinnonOsa);
        r = new RakenneOsaDto();
        r.setTutkinnonOsa(tutkinnonOsa.getTutkinnonOsa());
        secondChild.getOsat().add(r);

		RakenneModuuliDto thirdChild = new RakenneModuuliDto();
		thirdChild.setMuodostumisSaanto(new MuodostumisSaantoDto(new MuodostumisSaantoDto.Laajuus(120, 120, LaajuusYksikko.OPINTOVIIKKO)));
		thirdChild.setOsat(new ArrayList<AbstractRakenneOsaDto>());
		firstChild.getOsat().add(thirdChild);

		tutkinnonOsa = new TutkinnonOsaViiteDto();
		tutkinnonOsa.setNimi(new LokalisoituTekstiDto(Collections.singletonMap("fi", "Dataustuotanto")));
		tutkinnonOsa.setLaajuus(20);
        tutkinnonOsa.setYksikko(LaajuusYksikko.OPINTOVIIKKO);
		tutkinnonOsa.setTutkinnonOsa(new EntityReference(3L));
        r = new RakenneOsaDto();
        r.setTutkinnonOsa(tutkinnonOsa.getTutkinnonOsa());
        thirdChild.getOsat().add(r);

		tutkinnonOsa = new TutkinnonOsaViiteDto();
		tutkinnonOsa.setNimi(new LokalisoituTekstiDto(Collections.singletonMap("fi", "Dataustoteutus")));
		tutkinnonOsa.setLaajuus(20);
        tutkinnonOsa.setYksikko(LaajuusYksikko.OPINTOVIIKKO);
		tutkinnonOsa.setTutkinnonOsa(new EntityReference(3L));
        r = new RakenneOsaDto();
        r.setTutkinnonOsa(tutkinnonOsa.getTutkinnonOsa());
        thirdChild.getOsat().add(r);

		return rakenne;
	}
}
