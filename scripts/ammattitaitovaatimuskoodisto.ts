import * as lib from "./lib";
import * as _ from "lodash";
import axios from "axios";
import { createObjectCsvWriter } from "csv-writer";

async function main() {
    const writer = createObjectCsvWriter({
        path: "ammattitaitovaatimuskoodisto.csv",
        header: [
            { id: "perusteId", title: "Peruste" },
            { id: "tutkinnonOsaId", title: "Tutkinnon osa" },
            { id: "uri", title: "Koodi" },
            { id: "nimi_fi", title: "Nimi FI" },
            { id: "nimi_sv", title: "Nimi SV" },
            { id: "lyhyt_nimi_fi", title: "Lyhyt nimi FI" },
            { id: "lyhyt_nimi_sv", title: "Lyhyt nimi SV" },
            { id: "kuvaus_fi", title: "Kuvaus FI" },
            { id: "kuvaus_sv", title: "Kuvaus SV" },
        ]
    });

  try {
    const koodisto = [];

    for await (const peruste of lib.iteratePerusteet({ suoritustapa: "reformi" })) {
      const kaikki = await lib.getKaikki(peruste.id);
      for (const tosa of kaikki.tutkinnonOsat) {
        if (!tosa.arviointi) {
          if (tosa.tyyppi === "normaali") {
            console.log("Tutkinnon osa ilman arviointia:", peruste.nimi.fi, peruste.diaarinumero, tosa.nimi.fi, tosa.id);
          }
          continue;
        }

        for (const kohdealue of tosa.arviointi.arvioinninKohdealueet) {
          if (!kohdealue.koodi) {
            console.log("Kohdealue ilman koodia:", peruste.nimi.fi, peruste.diaarinumero, tosa.nimi.fi, tosa.id, kohdealue.nimi && kohdealue.nimi.fi);
          }
          else {
            koodisto.push({
              perusteId: peruste.id,
              tutkinnonOsaId: tosa.id,
              uri: kohdealue.koodi.uri,
              nimi_fi: kohdealue.otsikko ? kohdealue.otsikko.fi : "",
            });
          }
        }
      }

    }

    await writer.writeRecords(koodisto);
  }
  catch (err) {
    console.log(err.message);
  }
}

main();
