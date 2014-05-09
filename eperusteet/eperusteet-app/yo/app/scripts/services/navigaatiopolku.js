'use strict';
/* global _ */

// Tila-asetukset:
// $stateProvider.state('', { ... asetus: optio, ... });
//
// naviBase []:
// Tyhjentää navin ja asettaa naviBasen listan naviksi
//
// naviRest []:
// Asettaa naviksi edellisen/voimassaolevan naviBasen ja yhdistää naviRestin sen perään
//
// Kummallekin voi antaa merkkijonona muuttujan (alkaa ':':lla), jonka tilalle haetaan
// kyseinen muuttuja $stateParamsista tilan vaihtuessa
//
// Navigaatiopolku.asetaElementit-funktiolla voi asettaa näiden merkkijonomuuttujien
// tilalle muita merkkijonoja, esimerkiksi voisit haluta korvata id:n nimellä.

angular.module('eperusteApp')
  .service('Navigaatiopolku', function($rootScope, $state) {
    var naviElementit = {};
    var naviPolku = [];
    $rootScope.naviBase = [];
    $rootScope.naviRest = [];

    function navigaatioMap(toState, toParams) {
      var defcon = _.merge({ append: false }, toState.naviConfig);

      function relink(link) {
        var nlink = link;

        if (_.first(link) === ':') {
          var kentta = link.substr(1);
          nlink = naviElementit[kentta] ? naviElementit[kentta] : toParams[kentta];
        }
        return {
          url: $state.href(toState.name, toParams),
          kentta: link,
          arvo: nlink
        };
      }

      var rest = [];
      if (toState.naviBase) { $rootScope.naviBase = _.map(toState.naviBase, relink); }
      if (toState.naviRest) { rest = _.map(toState.naviRest, relink); }
      $rootScope.naviRest = defcon.append ? $rootScope.naviRest.concat(rest) : rest;
    }

    function paivitaNavigaatio() {
      function paivita(l) {
        var nl = naviElementit[l.kentta.substr(1)];
        if (nl) { l.arvo = nl; }
      }
      _.forEach($rootScope.naviBase, paivita);
      _.forEach($rootScope.naviRest, paivita);
      naviPolku = _.filter($rootScope.naviBase.concat($rootScope.naviRest), function(link) { return !_.isEmpty(link); });
      $rootScope.$emit('naviUpdate');
    }

    $rootScope.$on('$stateChangeSuccess', function(event, toState, toParams) {
      if (_.isEmpty($rootScope.naviBase) && _.isEmpty($rootScope.naviRest)) {
        var state = _.first($state.$current.self.name.split('.'));
        $rootScope.naviBase.push({
          arvo: state,
          kentta: state,
          url: $state.href(state)
        });
      }
      navigaatioMap(toState, toParams);
      paivitaNavigaatio();
    });

    return {
      // Palauttaa populoidun navipolun - kotisivu
      haeNavipolku: function() { return _.clone(naviPolku); },
      // Asettaa :muuttujan näkyvän arvon
      asetaElementit: function(elements) {
        naviElementit = _.merge(_.clone(naviElementit), elements);
        paivitaNavigaatio();
      },
      // Tyhjentää navipolun täysin
      clear: function() {
        naviPolku = [];
        $rootScope.naviBase = [];
        $rootScope.naviRest = [];
      }
    };
  });
