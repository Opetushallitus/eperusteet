// EP-2133
import * as fs from 'fs';
import * as _ from 'lodash';
import axios from 'axios';
import { createObjectCsvWriter } from 'csv-writer';

import * as lib from './lib';

const locales = {
    fi: {
        'nimi': 'Perusteen nimi',
        'tutkinnon-suorittaneen-osaaminen': 'Tutkinnon suorittaneen osaaminen',
        'tyotehtavat': 'Työtehtäviä, joissa tutkinnon suorittanut voi toimia',
        'tutkinnon-muodostuminen': 'Tutkinnon muodostuminen',
    },
    sv: {
        'nimi': 'Namn på grunderna',
        'tutkinnon-suorittaneen-osaaminen': 'Kunnande hos den som avlagt examen',
        'tyotehtavat': 'Den som har avlagt examen kan arbeta med följande uppgifter',
        'tutkinnon-muodostuminen': 'Uppbyggnaden av examen',
    },
    en: {
        'nimi': 'Title of the Qualification Requirements',
        'tutkinnon-suorittaneen-osaaminen': 'Vocational skills and competences required for completion of the qualification',
        'tyotehtavat': 'Range of occupations accessible to the holder of the certificate',
        'tutkinnon-muodostuminen': 'Composition of the qualification',
    }
}

async function getKvLiite(perusteId) {
    const res = await axios.get(`https://eperusteet.opintopolku.fi/eperusteet-service/api/perusteet/${perusteId}/kvliite`, {
        headers: {
            'Caller-Id': '1.2.246.562.10.00000000001.eperusteet_scripts'
        },
    });
    return res.data;
}

async function main() {
    const perusteet = lib.iteratePerusteet();

    const rows = {
        fi: [],
        sv: [],
        en: [],
    };

    for await (const peruste of perusteet) {
        const perusteId = peruste.id;
        try {
            let kvLiite = {};
            try {
                kvLiite = await getKvLiite(perusteId);
            } catch (e) {
                console.warn(e);
            }

            for (const kieli of _.keys(rows)) {
                const row = {
                    perusteId,
                    nimi: _.get(peruste, `nimi.${kieli}`),
                    suorittaneenOsaaminen: _.get(kvLiite, `suorittaneenOsaaminen.${kieli}`, '').replace(/(<([^>]+)>)/ig, ''),
                    tyotehtavatJoissaVoiToimia: _.get(kvLiite, `tyotehtavatJoissaVoiToimia.${kieli}`, '').replace(/(<([^>]+)>)/ig, ''),
                    opsMuodostumisenKuvaus: _.get(kvLiite, `muodostumisenKuvaus.ops.${kieli}`, '').replace(/(<([^>]+)>)/ig, ''),
                    nayttoMuodostumisenKuvaus: _.get(kvLiite, `muodostumisenKuvaus.naytto.${kieli}`, '').replace(/(<([^>]+)>)/ig, ''),
                    reformiMuodostumisenKuvaus: _.get(kvLiite, `muodostumisenKuvaus.reformi.${kieli}`, '').replace(/(<([^>]+)>)/ig, ''),
                };

                rows[kieli].push(row);
            }
            console.log(`Haettu: ${perusteId}`);
        } catch (e) {
            console.warn(`Haku epäonnistui: ${perusteId}`);
        }
    }

    for (const kieli of _.keys(rows)) {
        const path = `ammatillisten-tutkintojen-kuvaukset-${kieli}.csv`;
        try {
            fs.unlinkSync(path);
        } catch (e) {
            // noop
        }
        const writer = createObjectCsvWriter({
            path,
            header: [
                { id: 'perusteId', title: 'Peruste id' },
                { id: 'nimi', title: locales[kieli]['nimi'] },
                { id: 'suorittaneenOsaaminen', title: locales[kieli]['tutkinnon-suorittaneen-osaaminen'] },
                { id: 'tyotehtavatJoissaVoiToimia', title: locales[kieli]['tyotehtavat'] },
                { id: 'opsMuodostumisenKuvaus', title: locales[kieli]['tutkinnon-muodostuminen'] + ' (peruskoulutus)' },
                { id: 'nayttoMuodostumisenKuvaus', title: locales[kieli]['tutkinnon-muodostuminen'] + ' (naytto)' },
                { id: 'reformiMuodostumisenKuvaus', title: locales[kieli]['tutkinnon-muodostuminen'] + ' (reformi)' },
            ]
        });
        await writer.writeRecords(rows[kieli]);
    }
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
