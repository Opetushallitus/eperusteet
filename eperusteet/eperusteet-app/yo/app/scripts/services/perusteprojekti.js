'use strict';
/*global _*/

angular.module('eperusteApp')
  .factory('PerusteprojektiJasenet', function($resource, SERVICE_LOC) {
    return $resource(SERVICE_LOC + '/perusteprojektit/:id/jasenet', {
      id: '@id'
    }, {
      get: {method: 'GET', isArray: true}
    });
  })
  .factory('PerusteprojektiResource', function($resource, SERVICE_LOC) {
    return $resource(SERVICE_LOC + '/perusteprojektit/:id', {
      id: '@id'
    }, {
      update: {method: 'POST', isArray: false}
    });
  })
  .service('PerusteProjektiService', function($rootScope) {
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

    return {
      save: save,
      get: get,
      watcher: watcher,
      clean: clean,
      update: update,
      getSuoritustapa: getSuoritustapa,
      setSuoritustapa: setSuoritustapa,
      cleanSuoritustapa: cleanSuoritustapa
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
  .service('PerusteprojektiTiedotService', function ($q, PerusteprojektiResource, Perusteet, Suoritustapa, PerusteProjektiService) {

    var deferred = $q.defer();
    var projekti = {};
    var peruste = {};
    var sisalto = {};
    var self = this;
    var projektinTiedotDeferred = $q.defer();

    this.getProjekti = function () {
      return projekti;
    };
    
    this.getPeruste = function () {
      return peruste;
    };
    
    this.getSisalto = function () {
      return sisalto;
    };
    
    this.cleanData = function () {
      projekti = {};
      peruste = {};
      sisalto = {};
    };
    
    this.haeSisalto = function(perusteenId, suoritustapa) {
      var deferred = $q.defer();
      Suoritustapa.get({perusteenId: perusteenId, suoritustapa: suoritustapa}, function(vastaus) {
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
        Perusteet.get({perusteenId: projekti._peruste}, function (perusteVastaus) {
          peruste = perusteVastaus;
          if (peruste.suoritustavat !== null && peruste.suoritustavat.length > 0) {
            peruste.suoritustavat = _.sortBy(peruste.suoritustavat, 'suoritustapakoodi');
          }
          projektinTiedotDeferred.resolve();
          
        }, function(virhe) {
          projektinTiedotDeferred.reject();
          console.log('Virhe perusteen tietojen alustuksessa', virhe);
        });
      }, function(virhe) {
        projektinTiedotDeferred.reject();
        console.log('Virhe projektin tietojen alustuksessa', virhe);
      });
      
      return projektinTiedotDeferred.promise;
      
    };

    this.alustaPerusteenSisalto = function (stateParams) {

      // NOTE: Jos ei löydy suoritustapaa serviceltä niin käytetään suoritustapaa 'naytto'.
      //       Tämä toimii ammatillisen puolen projekteissa, mutta ei yleissivistävän puolella.
      //       Korjataan kun keksitään parempi suoritustavan valinta-algoritmi.
      console.log('alustaPerusteenSisalto suoritustapa', stateParams.suoritustapa);
      if (angular.isUndefined(stateParams.suoritustapa) || stateParams.suoritustapa === null || stateParams.suoritustapa === '') {
        stateParams.suoritustapa = 'naytto';
      }
      PerusteProjektiService.setSuoritustapa(stateParams.suoritustapa);
      var perusteenSisaltoDeferred = $q.defer();

      if (peruste.suoritustavat !== null && peruste.suoritustavat.length > 0) {
        self.haeSisalto(peruste.id, stateParams.suoritustapa).then(function() {
          perusteenSisaltoDeferred.resolve();
        }, function(virhe) {
          perusteenSisaltoDeferred.reject();
          console.log('Virhe perusteen sisällön alustuksessa', virhe);
        });
      } else {
        perusteenSisaltoDeferred.resolve();
      }
      return perusteenSisaltoDeferred.promise;
    };
  
    deferred.resolve(this);
    return deferred.promise;
    });
