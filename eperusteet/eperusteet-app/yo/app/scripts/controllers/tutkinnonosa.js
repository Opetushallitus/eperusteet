'use strict';

angular.module('eperusteApp')
  .controller('TutkinnonosaCtrl', function ($scope, $routeParams, $location,
    YleinenData, PerusteenOsat, Arviointi) {
    
    $scope.kontekstit = YleinenData.kontekstit;
    $scope.arviointi = {};
  
    if ($routeParams.konteksti && $scope.kontekstit.indexOf($routeParams.konteksti.toLowerCase()) !== -1) {
      $scope.konteksti = $routeParams.konteksti;
    } else {
      $location.path('/selaus/ammatillinenperuskoulutus');
    }
    
    PerusteenOsat.get({osanId: $routeParams.tutkinnonOsaId}, function(tulos) {
      
      console.log('Success1');

        if (tulos.arviointi !== undefined) {
          
          Arviointi.get({arviointiId: tulos.arviointi['fi.vm.sade.eperusteet.domain.Arviointi']}, function(tulos) {
            $scope.arviointi = tulos;

          }, function(virhe) {
            console.log(virhe.status);
          });
        }
 
    }, function(virhe) {
      console.log(virhe.status);
      if (virhe.status === 404) {
        //virhe.data
      }
    });
  
    $scope.valitseKieli = function(nimi) {
      return YleinenData.valitseKieli(nimi);
    };
    
  });
