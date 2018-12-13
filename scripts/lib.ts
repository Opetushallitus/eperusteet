import axios from "axios";
import * as _ from "lodash";
import * as qs from "qs";

const HakuEndpoint = "https://eperusteet.opintopolku.fi/eperusteet-service/api/perusteet";


const defaultParams = {
    tyyppi: ["koulutustyyppi_1", "koulutustyyppi_11", "koulutustyyppi_12"],
};

export async function* iteratePerusteet(params: any = {}) {
    let sivu = 0;

    params = { ...defaultParams, ...params };

    while (true) {
        const res = await axios.get(HakuEndpoint, {
            params: { ...params, sivu, sivukoko: 100 },
            paramsSerializer(params) {
                return qs.stringify(params, { arrayFormat: "repeat" });
            }
        });

        if (_.isEmpty(res.data.data)) {
            return;
        }

        console.log(`Got page ${sivu}`, _.size(res.data.data));
        console.log(res.data.sivu, res.data.kokonaismäärä, res.data.sivukoko);
        sivu += 1;

        for (const data of res.data.data) {
            yield data as any;
        }
    }
}


export async function getOsaamisalakuvaukset(perusteId: number) {
    const res = await axios.get(`https://eperusteet.opintopolku.fi/eperusteet-service/api/perusteet/${perusteId}/osaamisalakuvaukset`);
    return _(_.values(res.data))
        .map(_.keys)
        .flatten()
        .groupBy(_.identity)
        .mapValues(_.constant(true))
        .value();
}


export async function getKaikki(perusteId: number) {
    const url = `https://eperusteet.opintopolku.fi/eperusteet-service/api/perusteet/${perusteId}/kaikki`;
    console.log(url);
    const result = await axios.get(url);
    return result.data;
}


export async function getTutkinnonOsat(perusteId: number, suoritustapa: string) {
    const url = `https://eperusteet.opintopolku.fi/eperusteet-service/api/perusteet/${perusteId}/suoritustavat/${suoritustapa}/tutkinnonosat`;
    const result = await axios.get(url);
    return result.data;
}


export async function getTutkinnonOsa(perusteId: number, suoritustapa: string, tovId: number) {
    const url = `https://eperusteet.opintopolku.fi/eperusteet-service/api/perusteet/${perusteId}/suoritustavat/${suoritustapa}/tutkinnonosat/${tovId}`;
    console.log(url);
    const result = await axios.get(url);
    return result.data;
}
