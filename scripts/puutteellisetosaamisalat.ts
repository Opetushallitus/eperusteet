import * as lib from "./lib";
import * as _ from "lodash";
import axios from "axios";
import { createObjectCsvWriter } from "csv-writer";

async function main() {
    const writer = createObjectCsvWriter({
        path: "puutteelliset.csv",
        header: [
            { id: "perusteId", title: "Peruste id" },
            { id: "nimi", title: "Nimi" },
            { id: "puuttuvat", title: "Puuttuvat osaamisalakuvaukset" },
        ]
    });

    const iter = lib.iteratePerusteet();
    const brokenOsaamisalat = [];

    for await (const peruste of iter) {
        if (!_.isEmpty(peruste.osaamisalat)) {
            const kuvaukset = await lib.getOsaamisalakuvaukset(peruste.id);
            const puuttuvat = _(peruste.osaamisalat)
                .filter(oa => !kuvaukset[oa.uri])
                .map("arvo")
                .value();

            if (!_.isEmpty(puuttuvat)) {
                brokenOsaamisalat.push({
                    perusteId: peruste.id,
                    nimi: peruste.nimi.fi,
                    puuttuvat: _.join(puuttuvat, " ")
                });
                console.log(_.last(brokenOsaamisalat));
            }
        }
    }

    await writer.writeRecords(brokenOsaamisalat);
}

async function guard() {
    try {
        await main();
    }
    catch (err) {
        console.error(err);
    }
}

guard();
