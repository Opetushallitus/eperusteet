'use strict';
/* global _ */

angular.module('eperusteApp')
  .service('Notifikaatiot', function($rootScope, $timeout, NOTIFICATION_DELAY_SUCCESS, NOTIFICATION_DELAY_WARNING) {
    var viestit = [];

    function refresh() {
      $timeout(function() {
        paivita();
        $rootScope.$broadcast('update:notifikaatiot');
        if (!_.isEmpty(viestit)) { refresh(); }
      }, NOTIFICATION_DELAY_SUCCESS);
    }

    function uusiViesti(tyyppi, otsikko, viesti) {
      viestit.push({
        otsikko: otsikko,
        viesti: viesti,
        tyyppi: tyyppi,
        luotu: new Date(),
      });
      $rootScope.$broadcast('update:notifikaatiot');
      refresh();
    }

    function paivita() {
      function comp(luotu, delay) {
        var nyt = (new Date()).getTime();
        var viesti = luotu.getTime() + delay;
        return nyt < viesti;
      }

      viestit = _.filter(viestit, function(viesti) {
        if (viesti.tyyppi === 0 || viesti.tyyppi === 1) { return comp(viesti.luotu, NOTIFICATION_DELAY_SUCCESS); }
        else if (viesti.tyyppi === 2) { return comp(viesti.luotu, NOTIFICATION_DELAY_WARNING); }
        else { return true; }
      });
    }

    function poista(viesti) {
      _.remove(viestit, viesti);
      paivita();
      $rootScope.$broadcast('update:notifikaatiot');
    }

    return {
      normaali: _.partial(uusiViesti, 0),
      onnistui: _.partial(uusiViesti, 1),
      varoitus: _.partial(uusiViesti, 2),
      fataali: _.partial(uusiViesti, 3),
      viestit: function() { return _.clone(viestit); },
      paivita: paivita,
      poista: poista
    };
  })
  .controller('NotifikaatioCtrl', function($scope, Notifikaatiot) {
    $scope.viestit = [];

    $scope.poistaNotifikaatio = function(viesti) {
      Notifikaatiot.poista(viesti);
    };

    // $scope.lisaaNotifikaatio = function(tyyppi) {
    //   switch(tyyppi) {
    //     case 0: Notifikaatiot.normaali('normaali', 'viestiosa tätä notifikaatiota'); break;
    //     case 1: Notifikaatiot.onnistui('onnistui', 'viestiosa tätä notifikaatiota'); break;
    //     case 2: Notifikaatiot.varoitus('varoitus', 'viestiosa tätä notifikaatiota'); break;
    //     case 3: Notifikaatiot.fataali('fataali', 'viestiosa tätä notifikaatiota'); break;
    //     default: break;
    //   }
    // };

    $scope.$on('update:notifikaatiot', function() { $scope.viestit = Notifikaatiot.viestit(); });
  });
