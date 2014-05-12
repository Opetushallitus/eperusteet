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
      if (_.isObject(viesti) && viesti.data && viesti.data.syy) { viesti = viesti.data.syy; }
      else if (!viesti) { viesti = ''; }

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

    function serverCb(response) {
      console.log(response);
      if (response && response.status) {
        uusiViesti(response.status >= 500 ? 3 : 2, 'virheellinen-palvelinkutsu', response);
      } else {
        uusiViesti(3, 'virheellinen-palvelinkutsu', 'odottamaton-virhe');
      }
    }

    return {
      normaali: _.partial(uusiViesti, 0),
      onnistui: _.partial(uusiViesti, 1),
      varoitus: _.partial(uusiViesti, 2),
      fataali: _.partial(uusiViesti, 3),
      serverCb: serverCb,
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

    $scope.$on('update:notifikaatiot', function() { $scope.viestit = Notifikaatiot.viestit(); });
  });
