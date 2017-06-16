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
  .factory('PerusteprojektiTila', function ($resource, SERVICE_LOC) {
    return $resource(SERVICE_LOC + '/perusteprojektit/:id/tila/:tila', {id: '@id'});
  })
  .factory('OmatPerusteprojektit', function ($resource, SERVICE_LOC) {
    return $resource(SERVICE_LOC + '/perusteprojektit/omat');
  })
  .factory('PerusteprojektiResource', function ($resource, SERVICE_LOC) {
    return $resource(SERVICE_LOC + '/perusteprojektit/:id', {id: '@id'}, {
      update: {method: 'POST', isArray: false}
    });
  })
  .factory('PerusteprojektiOikeudet', function ($resource, SERVICE_LOC) {
    return $resource(SERVICE_LOC + '/perusteprojektit/:id/oikeudet', {id: '@id'});
  })
  .factory('DiaarinumeroUniqueResource', function ($resource, SERVICE_LOC) {
    return $resource(SERVICE_LOC + '/perusteprojektit/diaarinumero/uniikki/:diaarinumero');
  })
  .service('PerusteProjektit', function ($http, SERVICE_LOC, Notifikaatiot) {
    const haku = (query, success, failure, urlPostfix) => {
      success = success || angular.noop;
      failure = failure || Notifikaatiot.serverCb;
      $http.get(SERVICE_LOC + urlPostfix, query)
        .then(res => success(res))
        .catch(err => failure(err));
    };

    const hae = (query, success, failure) => haku(query, success, failure, '/perusteprojektit/info');
    const perusteHaku = (query, success, failure) => haku(query, success, failure, '/perusteprojektit/perusteHaku');

    return {
      hae: hae,
      perusteHaku: perusteHaku
    };
  })
  .service('PerusteProjektiService', function ($rootScope, $state, $q, $uibModal, YleinenData) {
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
      scope.$watch(kentta, function (temp) {
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

    function hasSuoritustapa(peruste, suoritustapakoodi) {
      return peruste && _.find(peruste.suoritustavat, function (st) {
          return st.suoritustapakoodi === suoritustapakoodi;
        });
    }

    function getRightSuoritustapa(peruste, projekti) {
      return hasSuoritustapa(peruste, getSuoritustapa()) ? getSuoritustapa() : projekti.suoritustapa;
    }

    function getSisaltoTunniste(projekti) {
      return YleinenData.koulutustyyppiInfo[projekti.koulutustyyppi] ?
        YleinenData.koulutustyyppiInfo[projekti.koulutustyyppi].sisaltoTunniste : 'sisalto';
    }

    /**
     * Luo oikea url perusteprojektille
     * @param peruste optional
     */
    function urlFn(method, projekti, peruste) {
      peruste = peruste || projekti.peruste;
      projekti = _.clone(projekti) || get();

      if (peruste && !projekti.koulutustyyppi) {
        projekti.koulutustyyppi = peruste.koulutustyyppi;
      }

      function suoritustapavalitsin() {
        let suoritustapakoodit;
        if (projekti.peruste && _.isArray(projekti.peruste.suoritustavat)) {
          suoritustapakoodit = _.map(projekti.peruste.suoritustavat, "suoritustapakoodi");
        }
        else if (projekti && _.isArray(projekti.suoritustavat) && !_.isEmpty(projekti.suoritustavat)) {
          suoritustapakoodit = projekti.suoritustavat;
        }

        if (_.isArray(suoritustapakoodit) && !_.isEmpty(suoritustapakoodit)) {
          if (_.size(suoritustapakoodit) === 1) {
            return suoritustapakoodit[0];
          }
          else {
            return _.includes(suoritustapakoodit, 'naytto')
              ? 'naytto'
              : suoritustapakoodit[0];
          }
        }
        else {
          const suoritustapa = getSuoritustapa() || getRightSuoritustapa(peruste, projekti);
          let reformi = false;
          if (peruste && peruste.reforminMukainen) {
              reformi = true;
          }
          const oletus = YleinenData.valitseSuoritustapaKoulutustyypille(projekti.koulutustyyppi, reformi);
          return (!_.includes(YleinenData.suoritustavat, suoritustapa) || oletus === 'aipe')
            ? oletus
            : suoritustapa;
        }
      }

      const suoritustapa = suoritustapavalitsin();

      const sisaltoTunniste = getSisaltoTunniste(projekti);
      return $state[method]('root.perusteprojekti.suoritustapa.' + sisaltoTunniste, {
        perusteProjektiId: projekti.id,
        suoritustapa: suoritustapa
      });
    }

    function mergeProjekti(projekti, tuoPohja) {
      var deferred = $q.defer();
      $uibModal.open({
        templateUrl: 'views/modals/projektiSisaltoTuonti.html',
        controller: 'ProjektiTiedotSisaltoModalCtrl',
        resolve: {
          pohja: function () {
            return !!tuoPohja;
          },
        }
      })
        .result.then(function (peruste) {
        peruste.tila = 'laadinta';
        peruste.tyyppi = 'normaali';
        var onOps = false;
        projekti.perusteId = peruste.id;
        projekti.koulutustyyppi = peruste.koulutustyyppi;
        _.forEach(peruste.suoritustavat, function (st) {
          if (st.suoritustapakoodi === 'ops') {
            onOps = true;
            projekti.laajuusYksikko = st.laajuusYksikko;
          }
        });
        deferred.resolve(peruste, projekti);
      }, deferred.reject);
      return deferred.promise;
    }

    return {
      mergeProjekti: mergeProjekti,
      save: save,
      get: get,
      watcher: watcher,
      clean: clean,
      update: update,
      getSuoritustapa: getSuoritustapa,
      setSuoritustapa: setSuoritustapa,
      cleanSuoritustapa: cleanSuoritustapa,
      getSisaltoTunniste: getSisaltoTunniste,
      getUrl: _.partial(urlFn, 'href'),
      goToProjektiState: _.partial(urlFn, 'go'),
      isPdfEnabled: function (peruste) {
        return YleinenData.koulutustyyppiInfo[peruste.koulutustyyppi] && YleinenData.koulutustyyppiInfo[peruste.koulutustyyppi].hasPdfCreation;
      }
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
  .service('PerusteprojektiTiedotService', function ($q, $state, PerusteprojektiResource, Perusteet, $log,
                                                     PerusteProjektiService, Notifikaatiot, YleinenData, PerusopetusService, SuoritustapaSisalto,
                                                     LukiokoulutusService, LukioKurssiService, AIPEService) {

    var deferred = $q.defer();
    var projekti: any = {};
    var peruste: any = {};
    var sisalto: any = {};
    var ylTiedot: any = {};
    var self = this;
    var projektinTiedotDeferred = $q.defer();

    this.getProjekti = function () {
      return _.clone(projekti);
    };

    this.setProjekti = function (obj) {
      projekti = _.clone(obj);
    };

    this.getPeruste = function () {
      return _.clone(peruste);
    };

    this.getSisalto = function () {
      return _.clone(sisalto);
    };

    this.getYlTiedot = function () {
      return _.clone(ylTiedot);
    };

    this.cleanData = function () {
      projekti = {};
      peruste = {};
      sisalto = {};
    };

    function getYlStructure(labels, osatProvider, sisaltoProvider, kurssitProvider) {
      // TODO replace with one resource call that fetches the whole structure
      var promises = [];
      _.each(labels, function (key) {
        var promise = osatProvider(key);
        promise.then(function (data) {
          ylTiedot[key] = data;
        });
        promises.push(promise);
      });
      var sisaltoPromise = sisaltoProvider();
      sisaltoPromise.then(function (data) {
        ylTiedot.sisalto = data;
      });
      promises.push(sisaltoPromise);
      if (kurssitProvider) {
        var kurssiPromise = kurssitProvider();
        kurssiPromise.then(function (data) {
          ylTiedot.kurssit = data;
        });
        promises.push(kurssiPromise);
      }
      return $q.all(promises);
    }

    this.haeSisalto = function (perusteId, suoritustapa) {
      var deferred = $q.defer();
      var ylDefer = $q.defer();

      if (!YleinenData.isPerusopetus(peruste) && !YleinenData.isAipe(peruste) && !YleinenData.isLukiokoulutus(peruste)) {
        SuoritustapaSisalto.get({perusteId: perusteId, suoritustapa: suoritustapa}, function (vastaus) {
          deferred.resolve(vastaus);
          sisalto = vastaus;
        }, function (virhe) {
          deferred.reject(virhe);
        });
        ylDefer.resolve();
      } else {
        var labels,
          osatProvider,
          sisaltoProvider,
          kurssitProvider = null;
        if (YleinenData.isLukiokoulutus(peruste)) {
          labels = LukiokoulutusService.LABELS;
          osatProvider = function (key) {
            return LukiokoulutusService.getOsat(key, true);
          };
          sisaltoProvider = function () {
            return LukiokoulutusService.getSisalto().$promise;
          };
          kurssitProvider = function () {
            return LukioKurssiService.listByPeruste(perusteId);
          };
        } else if (YleinenData.isAipe(peruste)) {
            labels = AIPEService.LABELS;
            osatProvider = function (key) {
                return AIPEService.getOsat(key, true);
            };
            sisaltoProvider = function () {
                return AIPEService.getSisalto(suoritustapa).$promise;
            };
        } else {
          labels = PerusopetusService.LABELS;
          osatProvider = function (key) {
            return PerusopetusService.getOsat(key, true);
          };
          sisaltoProvider = function () {
            return PerusopetusService.getSisalto(suoritustapa).$promise;
          };
        }
        getYlStructure(labels, osatProvider, sisaltoProvider, kurssitProvider).then(function () {
          ylDefer.resolve();
          sisalto = ylTiedot.sisalto;
          deferred.resolve(ylTiedot.sisalto);
        });
      }
      return $q.all([deferred.promise, ylDefer.promise]);
    };

    this.projektinTiedotAlustettu = function () {
      return projektinTiedotDeferred.promise;
    };


    this.alustaProjektinTiedot = function (stateParams) {
      LukiokoulutusService.setTiedot(this);
      PerusopetusService.setTiedot(this);
      AIPEService.setTiedot(this);
      projektinTiedotDeferred = $q.defer();

      PerusteprojektiResource.get({id: stateParams.perusteProjektiId}, function (projektiVastaus) {
        projekti = projektiVastaus;
        Perusteet.get({perusteId: projekti._peruste}, function (perusteVastaus) {
          peruste = perusteVastaus;
          if (!_.isEmpty(peruste.suoritustavat)) {
            peruste.suoritustavat = _.sortBy(peruste.suoritustavat, 'suoritustapakoodi');
          }
          projektinTiedotDeferred.resolve();
        }, function (virhe) {
          projektinTiedotDeferred.reject();
          Notifikaatiot.serverCb(virhe);
        });
      }, function (virhe) {
        projektinTiedotDeferred.reject();
        Notifikaatiot.serverCb(virhe);
      });

      return projektinTiedotDeferred.promise;
    };

    var asetaSuoritustapa = function (stateParams) {
      if (angular.isUndefined(stateParams.suoritustapa) || stateParams.suoritustapa === null || stateParams.suoritustapa === '') {
        stateParams.suoritustapa = YleinenData.valitseSuoritustapaKoulutustyypille(peruste.koulutustyyppi);
        $state.reload();
      }
    };

    this.alustaPerusteenSisalto = function (stateParams, forced) {
      asetaSuoritustapa(stateParams);
      PerusteProjektiService.setSuoritustapa(stateParams.suoritustapa);
      var perusteenSisaltoDeferred = $q.defer();

      if (forced
          || YleinenData.isPerusopetus(peruste)
          || YleinenData.isAipe(peruste)
          || YleinenData.isSimple(peruste)
          || (!_.isEmpty(peruste.suoritustavat))) {
        self.haeSisalto(peruste.id, stateParams.suoritustapa).then(function () {
          perusteenSisaltoDeferred.resolve();
        }, function (virhe) {
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
  .service('PerusteprojektiOikeudetService', function ($rootScope, $stateParams,
                                                       PerusteprojektiOikeudet, PerusteprojektiTiedotService) {
    var oikeudet;
    var projektiId = null;
    var projektiTila = null;

    function noudaOikeudet(stateParams) {
      var vastaus = PerusteprojektiOikeudet.get({id: stateParams.perusteProjektiId}, function (vastaus) {
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
        }
      }
      return false;
    }

    $rootScope.$on('$stateChangeSuccess', function () {
      PerusteprojektiTiedotService.then(function (res) {
        var projekti = res.getProjekti();
        if (projektiId && projektiId === projekti.id && projektiTila !== projekti.tila) {
          noudaOikeudet($stateParams);
        }
        else {
          projektiId = projekti.id;
          projektiTila = projekti.tila;
        }
      });
    });

    return {
      noudaOikeudet: noudaOikeudet,
      getOikeudet: getOikeudet,
      onkoOikeudet: onkoOikeudet
    };

  });
