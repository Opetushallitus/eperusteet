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
  .factory('PerusteprojektiTila', function($resource, SERVICE_LOC) {
    return $resource(SERVICE_LOC + '/perusteprojektit/:id/tila/:tila', {
      id: '@id'
    });
  })
  .factory('OmatPerusteprojektit', function($resource, SERVICE_LOC) {
    return $resource(SERVICE_LOC + '/perusteprojektit/omat');
  })
  .factory('PerusteprojektiResource', function($resource, SERVICE_LOC) {
    return $resource(SERVICE_LOC + '/perusteprojektit/:id', {
      id: '@id'
    }, {
      update: {method: 'POST', isArray: false}
    });
  })
  .factory('PerusteprojektiOikeudet', function($resource, SERVICE_LOC) {
    return $resource(SERVICE_LOC + '/perusteprojektit/:id/oikeudet', {
      id: '@id'
    });
  })
  .factory('DiaarinumeroUniqueResource', function($resource, SERVICE_LOC) {
    return $resource(SERVICE_LOC + '/perusteprojektit/diaarinumero/uniikki/:diaarinumero');
  })
  .service('PerusteProjektit', function($http, SERVICE_LOC, Notifikaatiot) {
    function hae(query, success, failure) {
      success = success || angular.noop;
      failure = failure || Notifikaatiot.serverCb;
      $http.get(SERVICE_LOC + '/perusteprojektit/info', query)
        .success(success)
        .error(failure);
    }

    return {
      hae: hae
    };
  })
  .service('PerusteProjektiService', function($rootScope, $state, YleinenData) {
    var pp = {};
    var suoritustapa = '';

    function save(obj) {
      obj = obj || {};
      pp = _.merge(_.clone(pp), _.clone(obj));
    }

    function get() {
      return _.clone(pp);
    }

    function clean() {
      pp = {};
    }

    function watcher(scope, kentta) {
      scope.$watch(kentta, function(temp) {
        save(temp);
      }, true);
    }

    function update() {
      $rootScope.$broadcast('update:perusteprojekti');
    }

    function getSuoritustapa() {
      return _.clone(suoritustapa);
    }

    function setSuoritustapa(st) {
      suoritustapa = _.clone(st);
    }

    function cleanSuoritustapa() {
      suoritustapa = '';
    }

    /**
     * Luo oikea url perusteprojektille
     * @param peruste optional
     */
    function urlFn(method, projekti, peruste) {
      projekti = _.clone(projekti) || get();
        if (peruste && !projekti.koulutustyyppi) {
          projekti.koulutustyyppi = peruste.koulutustyyppi;
        }
        if (YleinenData.isPerusopetus(projekti)) {
          return $state[method]('root.perusteprojekti.perusopetus', {
            perusteProjektiId: projekti.id
          });
        } else {
          // TODO: Omat perusteprojektit linkin suoritustapa pitäisi varmaankin olla jotain muuta kuin kovakoodattu 'naytto'
          return $state[method]('root.perusteprojekti.suoritustapa.sisalto', {
            perusteProjektiId: projekti.id,
            suoritustapa: YleinenData.valitseSuoritustapaKoulutustyypille(projekti.koulutustyyppi)
          });
        }
    }

    return {
      save: save,
      get: get,
      watcher: watcher,
      clean: clean,
      update: update,
      getSuoritustapa: getSuoritustapa,
      setSuoritustapa: setSuoritustapa,
      cleanSuoritustapa: cleanSuoritustapa,
      getUrl: _.partial(urlFn, 'href'),
      goToProjektiState: _.partial(urlFn, 'go')
    };
  })
  .service('TutkinnonOsaEditMode', function () {
    this.mode = false;
    this.setMode = function (mode) {
      this.mode = mode;
    };
    this.getMode = function () {
      var ret = this.mode;
      this.mode = false;
      return ret;
    };
  })
  .service('PerusteprojektiTiedotService', function ($q, $state, PerusteprojektiResource, Perusteet,
      SuoritustapaSisalto, PerusteProjektiService, Notifikaatiot, YleinenData) {

    var deferred = $q.defer();
    var projekti = {};
    var peruste = {};
    var sisalto = {};
    var self = this;
    var projektinTiedotDeferred = $q.defer();

    this.getProjekti = function () {
      return _.clone(projekti);
    };

    this.getPeruste = function () {
      return _.clone(peruste);
    };

    this.getSisalto = function () {
      return _.clone(sisalto);
    };

    this.cleanData = function () {
      projekti = {};
      peruste = {};
      sisalto = {};
    };

    this.haeSisalto = function(perusteId, suoritustapa) {
      var deferred = $q.defer();
      SuoritustapaSisalto.get({perusteId: perusteId, suoritustapa: suoritustapa}, function(vastaus) {
        deferred.resolve(vastaus);
        sisalto = vastaus;
      }, function(virhe) {
        deferred.reject(virhe);
      });
      return deferred.promise;
    };

    this.projektinTiedotAlustettu = function () {
      return projektinTiedotDeferred.promise;
    };


    this.alustaProjektinTiedot = function (stateParams) {
      projektinTiedotDeferred = $q.defer();

      PerusteprojektiResource.get({id: stateParams.perusteProjektiId}, function(projektiVastaus) {
        projekti = projektiVastaus;
        Perusteet.get({perusteId: projekti._peruste}, function (perusteVastaus) {
          peruste = perusteVastaus;
          if (peruste.suoritustavat && !_.isEmpty(peruste.suoritustavat.length)) {
            peruste.suoritustavat = _.sortBy(peruste.suoritustavat, 'suoritustapakoodi');
          }
          projektinTiedotDeferred.resolve();
        }, function(virhe) {
          projektinTiedotDeferred.reject();
          Notifikaatiot.serverCb(virhe);
        });
      }, function(virhe) {
        projektinTiedotDeferred.reject();
        Notifikaatiot.serverCb(virhe);
      });

      return projektinTiedotDeferred.promise;
    };

    this.alustaPerusteenSisalto = function (stateParams, forced) {

      // NOTE: Jos ei löydy suoritustapaa stateParams:ista niin käytetään suoritustapaa 'naytto'.
      //       Tämä toimii ammatillisen puolen projekteissa, mutta ei yleissivistävän puolella.
      // TODO: Korjataan kun keksitään parempi suoritustavan valinta-algoritmi.
      if (angular.isUndefined(stateParams.suoritustapa) || stateParams.suoritustapa === null || stateParams.suoritustapa === '') {
        stateParams.suoritustapa = YleinenData.valitseSuoritustapaKoulutustyypille(peruste.koulutustyyppi);
        if (!YleinenData.isPerusopetus(peruste)) {
          $state.reload();
        }
      }
      PerusteProjektiService.setSuoritustapa(stateParams.suoritustapa);
      var perusteenSisaltoDeferred = $q.defer();

      if (forced || (peruste.suoritustavat !== null && peruste.suoritustavat.length > 0)) {
        self.haeSisalto(peruste.id, stateParams.suoritustapa).then(function() {
          perusteenSisaltoDeferred.resolve();
        }, function(virhe) {
          perusteenSisaltoDeferred.reject();
          Notifikaatiot.serverCb(virhe);
        });
      } else {
        perusteenSisaltoDeferred.resolve();
      }
      return perusteenSisaltoDeferred.promise;
    };

    deferred.resolve(this);
    return deferred.promise;
  })
  .service('PerusteprojektiOikeudetService', function (PerusteprojektiOikeudet) {

    var oikeudet;

    function noudaOikeudet(stateParams) {
      var vastaus = PerusteprojektiOikeudet.get({id: stateParams.perusteProjektiId}, function(vastaus) {
        oikeudet = vastaus;
      });

      return vastaus.$promise;
    }

    function getOikeudet() {
      return _.clone(oikeudet);
    }

    function onkoOikeudet(target, permission) {
      if (oikeudet) {
       if (_.contains(oikeudet[target], permission)) {
         return true;
       } else {
         return false;
       }
     } else {
       console.log('virhe oikeuksien haussa');
       return false;
     }
    }


    return {
      noudaOikeudet: noudaOikeudet,
      getOikeudet: getOikeudet,
      onkoOikeudet: onkoOikeudet
    };

  });
