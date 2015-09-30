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
  .service('LukiokoulutusService', function (LukionOppiaineet,
                                             $q,
                                             Notifikaatiot,
                                             LukiokoulutuksenSisalto,
                                             LukiokoulutuksenYleisetTavoitteet,
                                             $log) {

    this.OPETUKSEN_YLEISET_TAVOITTEET = 'opetuksen_yleiset_tavoitteet';
    this.AIHEKOKONAISUUDET = 'aihekokonaisuudet';
    this.OPPIAINEET_OPPIMAARAT = 'oppiaineet_oppimaarat';

    this.LABELS = {
      'opetuksen-yleiset-tavoitteet': this.OPETUKSEN_YLEISET_TAVOITTEET,
      'aihekokonaisuudet': this.AIHEKOKONAISUUDET,
      'oppiaineet-oppimaarat': this.OPPIAINEET_OPPIMAARAT
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
        tyyppi: this.OPETUKSEN_YLEISET_TAVOITTEET,
        label: 'opetuksen-yleiset-tavoitteet',
        emptyPlaceholder: 'tyhja-placeholder-opetuksen-yleiset-tavoitteet',
        addLabel: 'lisaa-opetuksen-yleinen-tavoite'
      },
      {
        tyyppi: this.AIHEKOKONAISUUDET,
        label: 'aihekokonaisuudet',
        emptyPlaceholder: 'tyhja-placeholder-aihekokonaisuudet',
        addLabel: 'lisaa-aihekokonaisuus'
      },
      {
        tyyppi: this.OPPIAINEET_OPPIMAARAT,
        label: 'oppiaineet-oppimaarat',
        emptyPlaceholder: 'tyhja-placeholder-oppiaineet-oppimaarat',
        addLabel: 'lisaa-oppiaine'
      }
    ];

    var errorCb = function (err) {
      Notifikaatiot.serverCb(err);
    };

    function promisify(data) {
      var deferred = $q.defer();
      if (!_.isArray(data)) {
        _.extend(deferred, data);
      }
      deferred.resolve(data);
      return deferred.promise;
    }

    function commonParams (extra) {
      if (!tiedot) { return {}; }
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
        case this.OPETUKSEN_YLEISET_TAVOITTEET:
          return getOsaGeneric(LukiokoulutuksenYleisetTavoitteet, params);
        case this.AIHEKOKONAISUUDET:
          return getOsaGeneric(LukionOppiaineet, params);
        case this.OPPIAINEET_OPPIMAARAT:
          return getOsaGeneric(LukionOppiaineet, params);
        case 'tekstikappale':
          return getOsaGeneric(LukiokoulutuksenSisalto, params);
        default:
          break;
      }
    };

    this.deleteOsa = function (osa, success) {
      var successCb = success || angular.noop;
      if (osa.$url) {
        LukiokoulutuksenSisalto.delete(commonParams({osanId: osa.id}), successCb, errorCb);
      } else {
        osa.$delete(commonParams(), successCb, errorCb);
      }
    };

    this.saveOsa = function (data, config, success) {
      var successCb = success || angular.noop;
      switch (config.osanTyyppi) {
        case this.OPPIAINEET_OPPIMAARAT:
          LukionOppiaineet.save(commonParams(), data, successCb, errorCb);
          break;
        default:
          // Sisältö
          LukiokoulutuksenSisalto.save(commonParams(), data, successCb, errorCb);
          break;
      }
    };

    this.addSisaltoChild = function(id, success) {
      LukiokoulutuksenSisalto.addChild(commonParams({osanId: id}), {}, success);
    };

    this.updateSisaltoViitteet = function (sisalto, data, success) {
      var payload = commonParams(sisalto);
      LukiokoulutuksenSisalto.updateViitteet(payload, success, Notifikaatiot.serverCb);
    };

    this.getTekstikappaleet = function () {
      // TODO oikea data
      return [];
    };

    this.getOppimaarat = function (oppiaine) {
      if (!oppiaine.koosteinen) {
        return promisify([]);
      }
      return LukionOppiaineet.oppimaarat(commonParams({osanId: oppiaine.id})).$promise;
    };

    this.getSisalto = function () {
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
        case this.OPPIAINEET_OPPIMAARAT:
          return LukionOppiaineet.query(commonParams(), function (data) {
            cached[tyyppi] = data;
          }).$promise;
        case this.OPETUKSEN_YLEISET_TAVOITTEET:
            // TODO:
        case this.AIHEKOKONAISUUDET:
             // TODO:
        default:
          var d = $q.defer();
          d.resolve([]);
          return d.promise;
      }
    };
  })

  .service('LukioKurssiService', function(LukioKurssit, Lukitus, Notifikaatiot, LukiokoulutusService, $translate,
                                          $q, $log) {

    /**
     * Lists kurssit and related oppiaineet with jarjestys for given peruste.
     *
     * @param perusteId <number> id of Peruste
     * @return Promise<LukioKurssiListausDto[]>
     */
    var listByPeruste = function(perusteId, cb) {
      return LukioKurssit.query({
          perusteId: perusteId,
          kieli: $translate.use().toUpperCase()
      }, cb).$promise;
    };

    /**
     * Saves a new Lukiokurssi and related oppiaineet
     *
     * @param kurssi <LukioKurssiLuontiDto>
     * @return Promise<LukiokurssiMuokkausDto>
     */
    var save = function(kurssi) {
      var d = $q.defer();
      LukioKurssit.save({
        perusteId: LukiokoulutusService.getPerusteId()
      }, kurssi, function(kurssinTiedot) {
        Notifikaatiot.onnistui('tallennus-onnistui');
        d.resolve(kurssinTiedot);
      }, Notifikaatiot.serverCb);
      return d.promise;
    };

    /**
     * Locks and updates Lukiokurssi and related oppiaineet
     *
     * @param kurssi <LukiokurssiMuokkausDto>
     * @return Promise<LukiokurssiMuokkausDto>
     */
    var update = function(kurssi) {
      var d = $q.defer();
      Lukitus.lukitse(function () {
        LukioKurssit.update({
          perusteId: LukiokoulutusService.getPerusteId()
        }, kurssi, function(kurssinTiedot) {
          Lukitus.vapauta(function() {
            Notifikaatiot.onnistui('tallennus-onnistui');
            d.resolve(kurssinTiedot);
          });
        }, Notifikaatiot.serverCb);
      });
      return d.promise;
    };

    return {
      listByPeruste: listByPeruste,
      save: save,
      update: update
    };
  });
