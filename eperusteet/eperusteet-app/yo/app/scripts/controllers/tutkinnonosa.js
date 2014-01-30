'use strict';

angular.module('eperusteApp')
  .controller('TutkinnonosaCtrl', function ($scope, $routeParams, $location,
    YleinenData, PerusteenOsat, Perusteet) {
    
    $scope.kontekstit = YleinenData.kontekstit;
    $scope.arviointi = {};
    $scope.arviointiasteikot = {};
  
    if ($routeParams.konteksti && $scope.kontekstit.indexOf($routeParams.konteksti.toLowerCase()) !== -1) {
      $scope.konteksti = $routeParams.konteksti;
    } else {
      $location.path('/selaus/ammatillinenperuskoulutus');
    }
    
    // navigaatiopolkua varten
    if ($routeParams.perusteId) {
      $scope.perusteId = $routeParams.perusteId;
      Perusteet.get({perusteenId: $routeParams.perusteId}, function(peruste) {
        $scope.peruste = peruste;
      }, function(/*virhe*/) {
        //TODO
      });
    }
    
    PerusteenOsat.get({osanId: $routeParams.tutkinnonOsaId}, function(tulos) {

        if (tulos.arviointi !== undefined) {
          $scope.arviointi = tulos.arviointi;
          YleinenData.haeArviointiasteikot();
        }

    }, function(virhe) {
      console.log(virhe.status);
      if (virhe.status === 404) {
        //virhe.data
      }
    });

    $scope.$on('arviointiasteikot', function() {
      $scope.arviointiasteikot = YleinenData.arviointiasteikot;
    });
  
    $scope.valitseKieli = function(nimi) {
      return YleinenData.valitseKieli(nimi);
    };
    
  });
