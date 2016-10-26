// TODO: Keksi parempi nimi ja paikka
namespace Types {
    export interface Kommentti {
        nimi: string;
        muokkaaja: string;
        sisalto: string;
        luotu: Date;
        muokattu: Date;
        id: number;
        ylinId?: number;
        parentId?: number;
        perusteprojektiId: number;
        poistettu: boolean;
        perusteenOsaId?: number;
        suoritustapa?: string;
    };

    export interface Kayttajatieto {
        username?: string;
        kutsumanimi?: string;
        etunimet?: string;
        sukunimi?: string;
        oidHenkilo?: string;
        kieliKoodi?: string;
        $$esitysnimi?: string;
    };
}

// TODO: Keksi parempi nimi ja paikka
namespace Logic {
    let _state;

    export const init = ($state) => {
        _state = $state;
    };

    export const getKommenttiUrl = (kommentti: Types.Kommentti) => {
        if (kommentti.perusteenOsaId) {
            Endpoints.getPerusteenOsaViite(kommentti.perusteenOsaId)
                .then(() => {
                    _state.go("root.perusteprojekti.suoritustapa.tekstikappale", {
                        suoritustapa: kommentti.suoritustapa,
                        perusteenOsaViiteId: kommentti.perusteenOsaId
                    });
                })
                .catch(() => {
                    _state.go("root.perusteprojekti.suoritustapa.tutkinnonosa", {
                        suoritustapa: kommentti.suoritustapa,
                        tutkinnonOsaViiteId: kommentti.perusteenOsaId
                    });
                });
        }
        else {
            _state.go("root.perusteprojekti.suoritustapa.muodostumissaannot", { suoritustapa: kommentti.suoritustapa })
        }
    };

    export const getKayttajaNimi = (kayttaja: Types.Kayttajatieto) => {
        if (kayttaja) {
            kayttaja.$$esitysnimi = kayttaja.kutsumanimi + " " + kayttaja.sukunimi;
            return kayttaja;
        }
        else {
            return {
                $$esitysnimi: "-"
            }
        }
    }


};

// TODO: Keksi parempi nimi ja paikka
namespace Endpoints {
    let _Api;

    export const init = (Api) => {
        _Api = Api;
    }

    const kayttajaCache = {};

    export const getKayttajaByOid = (oid: string) => {
        if (!kayttajaCache[oid]) {
            kayttajaCache[oid] = _Api.one("kayttajatieto", oid).get();
        }
        return kayttajaCache[oid];
    };

    export const getPerusteenOsaViite = (id) => {
        return _Api.one("perusteenosat/viite", id).get();
    };
}

angular.module('eperusteApp')
.run($injector => $injector.invoke(Endpoints.init))
.run($injector => $injector.invoke(Logic.init));
