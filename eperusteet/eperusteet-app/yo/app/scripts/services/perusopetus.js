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

'use strict';
/*global _*/

angular.module('eperusteApp')
  .service('PerusopetusService', function (Vuosiluokkakokonaisuudet, Oppiaineet, $q,
      OppiaineenVuosiluokkakokonaisuudet, LaajaalaisetOsaamiset, Notifikaatiot,
      PerusopetuksenSisalto) {
    this.OSAAMINEN = 'osaaminen';
    this.VUOSILUOKAT = 'vuosiluokat';
    this.OPPIAINEET = 'oppiaineet';
    this.LABELS = {
      'laaja-alainen-osaaminen': this.OSAAMINEN,
      'vuosiluokkakokonaisuudet': this.VUOSILUOKAT,
      'oppiaineet': this.OPPIAINEET
    };
    var tiedot = null;
    var cached = {};
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
        tyyppi: this.VUOSILUOKAT,
        label: 'vuosiluokkakokonaisuudet',
        emptyPlaceholder: 'tyhja-placeholder-vuosiluokat',
        addLabel: 'lisaa-vuosiluokkakokonaisuus'
      },
      {
        tyyppi: this.OPPIAINEET,
        label: 'oppiaineet',
        emptyPlaceholder: 'tyhja-placeholder-oppiaineet',
        addLabel: 'lisaa-oppiaine'
      },
    ];
    var errorCb = function (err) {
      Notifikaatiot.serverCb(err);
    };

    function promisify(data) {
      var deferred = $q.defer();
      _.extend(deferred, data);
      deferred.resolve(data);
      return deferred.promise;
    }

    function commonParams (extra) {
      var obj = { perusteId: tiedot.getProjekti()._peruste };
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
        case this.VUOSILUOKAT:
          return getOsaGeneric(Vuosiluokkakokonaisuudet, params);
        case this.OPPIAINEET:
          return getOsaGeneric(Oppiaineet, params);
        case this.OSAAMINEN:
          return getOsaGeneric(LaajaalaisetOsaamiset, params);
        case 'tekstikappale':
          return getOsaGeneric(PerusopetuksenSisalto, params);
        default:
          break;
      }
    };

    this.deleteOsa = function (osa, success) {
      var successCb = success || angular.noop;
      if (osa.$url) {
        PerusopetuksenSisalto.delete(commonParams({osanId: osa.id}), successCb, errorCb);
      } else {
        osa.$delete(commonParams(), successCb, errorCb);
      }
    };

    this.saveOsa = function (data, config, success) {
      var successCb = success || angular.noop;
      switch (config.osanTyyppi) {
        case this.OPPIAINEET:
          Oppiaineet.save(commonParams(), data, successCb, errorCb);
          break;
        case this.OSAAMINEN:
          LaajaalaisetOsaamiset.save(commonParams(), data, successCb, errorCb);
          break;
        default:
          // Sisältö
          PerusopetuksenSisalto.save(commonParams(), data, successCb, errorCb);
          break;
      }
    };

    this.addSisaltoChild = function (id, success) {
      console.log('addSisaltoChild', commonParams({osanId: id}), success);
      // TODO odottaa bäkkäriä
      //PerusopetuksenSisalto.addChild(commonParams({osanId: id}), {}, success);
    };

    this.saveVuosiluokkakokonaisuudenOsa = function (vuosiluokkakokonaisuus, oppiaine, cb) {
      OppiaineenVuosiluokkakokonaisuudet.save({
        perusteId: tiedot.getProjekti()._peruste,
        oppiaineId: oppiaine.id
      }, vuosiluokkakokonaisuus, function (res) {
        vuosiluokkakokonaisuus = res;
        (cb || angular.noop)();
      }, errorCb);
    };

    this.getTekstikappaleet = function () {
      // TODO oikea data
      return [];
    };

    this.getOppimaarat = function (oppiaine) {
      if (!oppiaine.koosteinen) {
        return promisify([]);
      }
      return Oppiaineet.oppimaarat(commonParams({osanId: oppiaine.id})).$promise;
    };

    this.getSisalto = function () {
      return PerusopetuksenSisalto.root(commonParams());
    };

    this.getOsat = function (tyyppi, useCache) {
      if (useCache && cached[tyyppi]) {
        return cached[tyyppi];
      }
      switch(tyyppi) {
        case this.OSAAMINEN:
          return LaajaalaisetOsaamiset.query(commonParams(), function (data) {
            cached[tyyppi] = data;
          });
        case this.VUOSILUOKAT:
          return Vuosiluokkakokonaisuudet.query(commonParams(), function (data) {
            cached[tyyppi] = data;
          });
        case this.OPPIAINEET:
          return Oppiaineet.query(commonParams(), function (data) {
            cached[tyyppi] = data;
          });
        default:
          return [];
      }
    };
  });
