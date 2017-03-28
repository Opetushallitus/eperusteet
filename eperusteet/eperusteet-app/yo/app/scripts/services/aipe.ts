/*
 * Copyright (c) 2013 The Finnish Board of Education - Opetushallitus
 *
 * This program is free software: Licensed under the EUPL, Version 1.1 or - as
 * soon as they will be approved by the European Commission - subsequent versions
 * of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at: http://ec.europa.eu/idabc/eupl
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * European Union Public Licence for more details.
 */

angular.module('eperusteApp')
.service('AIPEService', function (AIPEOppiaineet, $q, AIPELaajaalaisetOsaamiset, Notifikaatiot, SuoritustapaSisalto, AIPEVaiheet) {
    this.OSAAMINEN = 'osaaminen';
    this.VAIHEET = 'vaiheet';
    this.OPPIAINEET = 'oppiaineet';
    this.LABELS = {
        'laaja-alainen-osaaminen': this.OSAAMINEN,
        'vaiheet': this.VAIHEET
    };
    let tiedot = null;
    let cached = {};
    this.setTiedot = function (value) {
        tiedot = value;
    };
    this.getPerusteId = function () {
        return tiedot.getProjekti()._peruste;
    };

    this.sisallot = [
        {
            tyyppi: this.OSAAMINEN,
            label: 'laaja-alainen-osaaminen',
            emptyPlaceholder: 'tyhja-placeholder-osaaminen',
            addLabel: 'lisaa-osaamiskokonaisuus'
        },
        {
            tyyppi: this.VAIHEET,
            label: 'vaiheet',
            emptyPlaceholder: 'tyhja-placeholder-vaiheet',
            addLabel: 'lisaa-vaihe'
        },
        {
            tyyppi: this.OPPIAINEET,
            label: 'oppiaineet',
            emptyPlaceholder: 'tyhja-placeholder-oppiaineet',
            addLabel: 'lisaa-oppiaine'
        },
    ];

    const errorCb = function (err) {
        Notifikaatiot.serverCb(err);
    };

    function promisify(data) {
        const deferred = $q.defer();
        if (!_.isArray(data)) {
            _.extend(deferred, data);
        }
        deferred.resolve(data);
        return deferred.promise;
    }

    function commonParams (extra?) {
        if (!tiedot) { return {}; }
        const obj = { perusteId: tiedot.getProjekti()._peruste };
        if (extra) {
            _.extend(obj, extra);
        }
        return obj;
    }

    function getOsaGeneric(resource, params) {
        return resource.get(commonParams({osanId: params.osanId})).$promise;
    }

    this.getOsa = function (params) {
        if (params.osanId === 'uusi') {
            return promisify({});
        }
        switch (params.osanTyyppi) {
            case this.VAIHEET:
                return getOsaGeneric(AIPEVaiheet, params);
            case this.OSAAMINEN:
                return getOsaGeneric(AIPELaajaalaisetOsaamiset, params);
            //case 'tekstikappale':
            //    return getOsaGeneric(PerusopetuksenSisalto, params);
            default:
                break;
        }
    };

    this.deleteOsa = function (osa, success) {
        const successCb = success || angular.noop;
        osa.$delete(commonParams(), successCb, errorCb);
    };

    this.saveOsa = function (data, config, success) {
        const successCb = success || angular.noop;
        switch (config.osanTyyppi) {
            case this.OPPIAINEET:
                AIPEOppiaineet.save(commonParams(), data, successCb, errorCb);
                break;
            case this.OSAAMINEN:
                AIPELaajaalaisetOsaamiset.save(commonParams(), data, successCb, errorCb);
                break;
            default:
                // Sisältö
                //PerusopetuksenSisalto.save(commonParams(), data, successCb, errorCb);
                break;
        }
    };

    this.updateSisaltoViitteet = function (sisalto, data, success) {
        const payload = commonParams(sisalto);
        //PerusopetuksenSisalto.updateViitteet(payload, success, Notifikaatiot.serverCb);
        console.warn("updateSisaltoViitteet");
    };

    this.getOppimaarat = function (oppiaine) {
        if (!oppiaine.koosteinen) {
            return promisify([]);
        }
        return AIPEOppiaineet.oppimaarat(commonParams({
            vaiheId: 1,
            osanId: oppiaine.id
        })).$promise;
    };

    this.getSisalto = function (suoritustapa) {
        return SuoritustapaSisalto.get(commonParams({suoritustapa: suoritustapa}));
    };

    this.clearCache = function () {
        cached = {};
    };

    this.getOsat = function (tyyppi, useCache) {
        if (useCache && cached[tyyppi]) {
            return promisify(cached[tyyppi]);
        }
        switch(tyyppi) {
            case this.OSAAMINEN:
                return AIPELaajaalaisetOsaamiset.query(commonParams(), function (data) {
                    cached[tyyppi] = data;
                }).$promise;
            case this.VAIHEET:
                return AIPEVaiheet.query(commonParams(), data => {
                    cached[tyyppi] = data;
                }).$promise;
            case this.OPPIAINEET:
                let deferred = $q.defer();
                deferred.resolve();
                return deferred.promise;
            default:
                return [];
        }
    };
});
