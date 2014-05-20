'use strict';
/* global _ */

angular.module('eperusteApp')
  .controller('JarjestelmaVirheModalCtrl', function($scope, $modalInstance, $state, viesti) {
    $scope.viesti = viesti;
    $scope.ok = function() { $modalInstance.close(); };
  })
  .service('Notifikaatiot', function($rootScope, $timeout, NOTIFICATION_DELAY_SUCCESS, NOTIFICATION_DELAY_WARNING, $modal, $state, Kaanna) {
    var viestit = [];

    function refresh() {
      $timeout(function() {
        paivita();
        $rootScope.$broadcast('update:notifikaatiot');
        if (!_.isEmpty(viestit)) { refresh(); }
      }, NOTIFICATION_DELAY_SUCCESS);
    }

    function uusiViesti(tyyppi, viesti, ilmanKuvaa) {
      if (_.isObject(viesti) && viesti.data && viesti.data.syy) { viesti = viesti.data.syy; }
      else if (!viesti) { viesti = ''; }

      viestit.push({
        viesti: viesti ? viesti : tyyppi === 1 ? 'tallennus-onnistui' : '',
        ilmanKuvaa: ilmanKuvaa || false,
        tyyppi: tyyppi,
        luotu: new Date(),
      });

      $rootScope.$broadcast('update:notifikaatiot');
      refresh();
    }

    function fataali(viesti, cb) {
      cb = cb || function(){};
      $modal.open({
        templateUrl: 'views/modals/jarjestelmavirhe.html',
        controller: 'JarjestelmaVirheModalCtrl',
        resolve: { viesti: function() { return viesti; } }
      }).result.then(function() {
        cb();
      });
    }

    function paivita() {
      function comp(luotu, delay) {
        var nyt = (new Date()).getTime();
        var viesti = luotu.getTime() + delay;
        return nyt < viesti;
      }

      viestit = _.filter(viestit, function(viesti) {
        if (viesti.tyyppi === 1) { return comp(viesti.luotu, NOTIFICATION_DELAY_SUCCESS); }
        else if (viesti.tyyppi === 2) { return comp(viesti.luotu, NOTIFICATION_DELAY_WARNING); }
        else { return true; }
      });
    }

    function poista(i) {
      if (_.isObject(i)) {
        _.remove(viestit, i);
        paivita();
        $rootScope.$broadcast('update:notifikaatiot');
      }
      else { viestit.splice(i, 1); }
    }

    function serverCb(response) {
      if (response && response.status && response.status >= 500) {
        fataali(Kaanna.kaanna('järjestelmävirhe-alku') + response.status + Kaanna.kaanna('järjestelmävirhe-loppu'), function() {
          // $state.go('aloitussivu');
        });
      }
      else { uusiViesti(2, 'odottamaton-virhe'); }
    }

    $rootScope.$on('$stateChangeStart', function() {
      // viestit = [];
      // $rootScope.$broadcast('update:notifikaatiot');
    });

    return {
      normaali: _.partial(uusiViesti, 0),
      onnistui: _.partial(uusiViesti, 1),
      varoitus: _.partial(uusiViesti, 2),
      fataali: fataali,
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
