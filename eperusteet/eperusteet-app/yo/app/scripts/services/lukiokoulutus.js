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
                                             $q, SuoritustapaSisalto,
                                             Notifikaatiot,
                                             LukiokoulutuksenSisalto,
                                             LukioKurssit,
                                             LukiokoulutusYleisetTavoitteet,
                                             LukiokoulutusAihekokonaisuudet) {

    this.OPETUKSEN_YLEISET_TAVOITTEET = 'opetuksen_yleiset_tavoitteet';
    this.AIHEKOKONAISUUDET = 'aihekokonaisuudet';
    this.OPPIAINEET_OPPIMAARAT = 'oppiaineet_oppimaarat';
    this.KURSSIT = 'kurssit';

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
          return getOsaGeneric(LukiokoulutusYleisetTavoitteet, params);
        case this.AIHEKOKONAISUUDET:
          return getOsaGeneric(LukiokoulutusAihekokonaisuudet, params);
        case this.OPPIAINEET_OPPIMAARAT:
          return getOsaGeneric(LukionOppiaineet, params);
        case this.KURSSIT:
          return getOsaGeneric(LukioKurssit, params);
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
      return SuoritustapaSisalto.get(commonParams({suoritustapa: 'lukiokoulutus'}));
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
        case this.KURSSIT:
          return LukioKurssit.query(commonParams(), function(data) {
            cached[tyyppi] = data;
          }).$promise;
        case this.OPETUKSEN_YLEISET_TAVOITTEET:
          return LukiokoulutusYleisetTavoitteet.query(commonParams(), function (data) {
            cached[tyyppi] = data;
          }).$promise;
        case this.AIHEKOKONAISUUDET:
          return LukiokoulutusAihekokonaisuudet.query(commonParams(), function (data) {
            cached[tyyppi] = data;
          }).$promise;
        default:
          return promisify([]);
      }
    };
  })

  .service('LukioKurssiService', function(LukioKurssit, Lukitus, Notifikaatiot, LukiokoulutusService, $translate, $q) {

    /**
     * Lists kurssit and related oppiaineet with jarjestys for given peruste.
     *
     * @param perusteId <number> id of Peruste
     * @return Promise<LukioKurssiListausDto[]>
     */
    var listByPeruste = function(perusteId, cb) {
      return LukioKurssit.query({
          perusteId: perusteId
      }, cb).$promise;
    };

    /**
     * @param id of kurssi
     * @return Promise<LukiokurssiMuokkausDto>
     */
    var get = function(id) {
      return LukioKurssit.get({osanId: id, perusteId: LukiokoulutusService.getPerusteId()});
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
      Lukitus.lukitseLukioKurssi(kurssi.id, function () {
        LukioKurssit.update({
          perusteId: LukiokoulutusService.getPerusteId(),
          osanId: kurssi.id
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
      get: get,
      save: save,
      update: update
    };
  })
  .service('LukioAihekokonaisuudetService',
            function(LukiokoulutusAihekokonaisuudet, Lukitus,
                     Notifikaatiot, LukiokoulutusService, $translate, $q) {

    /**
     * Tallentaa yhden aihekokonaisuuden
     *
     * @param aihekokonaisuus <LukioAihekokonaisuusLuontiDto>
     * @return Promise<LukioAihekokonaisuusMuokkausDto>
     */
    var saveAihekokonaisuus = function(aihekokonaisuus) {
      var d = $q.defer();
      LukiokoulutusAihekokonaisuudet.saveAihekokonaisuus({
        perusteId: LukiokoulutusService.getPerusteId()
      }, aihekokonaisuus, function(aihekokonaisuusTiedot) {
        Notifikaatiot.onnistui('tallennus-onnistui');
        d.resolve(aihekokonaisuusTiedot);
      }, Notifikaatiot.serverCb);
      return d.promise;
    };

    /**
     * Tallentaa aihekokobaisuudet yleiskuvauksen
     *
     * @param aihekokonaisuus <AihekokonaisuudetYleiskuvausDto>
     * @return Promise<AihekokonaisuudetYleiskuvausDto>
     */
    var saveAihekokonaisuudetYleiskuvaus = function(aihekokonaisuudetYleiskuvaus) {
      var d = $q.defer();
      LukiokoulutusAihekokonaisuudet.saveAihekokonaisuudetYleiskuvaus({
        perusteId: LukiokoulutusService.getPerusteId()
      }, aihekokonaisuudetYleiskuvaus, function(aihekokonaisuudetYleiskuvausTiedot) {
        Notifikaatiot.onnistui('tallennus-onnistui');
        d.resolve(aihekokonaisuudetYleiskuvausTiedot);
      }, Notifikaatiot.serverCb);
      return d.promise;
    };

    /**
     *
     * Lukitesee ja muokkaa LukioAihekokobaisuuden
     *
     * @param aihekokonaisuus <LukioAihekokonaisuusMuokkausDto>
     * @return Promise<LukioAihekokonaisuusMuokkausDto>
     */
    var updateAihekokonaisuus = function(aihekokonaisuus) {
      var d = $q.defer();
      Lukitus.lukitseLukioAihekokonaisuus( aihekokonaisuus.id, function () {
        LukiokoulutusAihekokonaisuudet.updateAihekokonaisuus({
          perusteId: LukiokoulutusService.getPerusteId(),
          aihekokonaisuusId: aihekokonaisuus.id
        }, aihekokonaisuus, function(aihekokonaisuusTiedot) {
          Lukitus.vapautaLukioAihekokonaisuus(aihekokonaisuusTiedot.id, function() {
            Notifikaatiot.onnistui('tallennus-onnistui');
            d.resolve(aihekokonaisuusTiedot);
          });
        }, Notifikaatiot.serverCb);
      });
      return d.promise;
    };

    var getAihekokonaisuus = function(aihekokonaisuusId,cb) {
      return LukiokoulutusAihekokonaisuudet.query({
        aihekokonaisuusId: aihekokonaisuusId
      }, cb).$promise;
    };

    var getAihekokonaisuudetYleiskuvaus = function() {
      return LukiokoulutusAihekokonaisuudet.getAihekokonaisuudetYleiskuvaus({
        perusteId: LukiokoulutusService.getPerusteId()
      }).$promise;
    };

    var deleteAihekokonaisuus = function(aihekokonaisuusId,cb) {
      var d = $q.defer();
      Lukitus.lukitseLukioAihekokonaisuus(aihekokonaisuusId, function () {
        LukiokoulutusAihekokonaisuudet.delete({
          perusteId: LukiokoulutusService.getPerusteId(),
          osanId: aihekokonaisuusId
        }, aihekokonaisuusId, function() {
          Lukitus.vapautaLukioAihekokonaisuus(aihekokonaisuusId, function() {
            Notifikaatiot.onnistui('poisto-onnistui');
            d.resolve(aihekokonaisuusId);
          });
        }, Notifikaatiot.serverCb);
      });
      return d.promise;

      return LukiokoulutusAihekokonaisuudet.delete({
        aihekokonaisuusId: aihekokonaisuusId
      }, cb).$promise;
    };

    return {
      saveAihekokonaisuus: saveAihekokonaisuus,
      saveAihekokonaisuudetYleiskuvaus: saveAihekokonaisuudetYleiskuvaus,
      updateAihekokonaisuus: updateAihekokonaisuus,
      getAihekokonaisuus: getAihekokonaisuus,
      getAihekokonaisuudetYleiskuvaus: getAihekokonaisuudetYleiskuvaus,
      deleteAihekokonaisuus: deleteAihekokonaisuus
    };
  });
