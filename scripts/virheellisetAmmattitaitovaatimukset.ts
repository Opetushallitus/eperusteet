import * as lib from "./lib";
import * as _ from "lodash";


async function main() {
  try {
    _.forEach(await lib.virheellisetTekstimuotoisetAmmattitaitovaatimukset(), v => {
      // console.log(`https://virkailija.testiopintopolku.fi/eperusteet-app/#/fi/perusteprojekti/${}/reformi/tutkinnonosa/5930277`);
      console.log(`http://localhost:9000/#/fi/perusteprojekti/${v.projektiId}/reformi/tutkinnonosa/${v.tutkinnonOsaViite}`);
    })

  }
  catch (err) {
    console.log(err.message);
  }
}

main();
