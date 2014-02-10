'use strict';


angular.module('eperusteApp')
  .config(function($routeProvider) {
    $routeProvider
      .when('/selaus/:konteksti/:perusteId/tutkinnonosa/:tutkinnonOsaId', {
        templateUrl: 'views/tutkinnonosa.html',
        controller: 'TutkinnonosaCtrl',
        navigaationimiId: 'tutkinnonOsa'
      });
  })
  .controller('TutkinnonosaCtrl', function ($q, $scope, $rootScope, $routeParams, $location,
    YleinenData, PerusteenOsat, Perusteet, palvelinhaunIlmoitusKanava) {

    $scope.kontekstit = YleinenData.kontekstit;
    $scope.tutkinnonOsa = {};
    //$scope.arviointi = {};
    $scope.arviointiasteikot = {};
    $scope.kontekstit = YleinenData.kontekstit;
    var avausTyyli = 'glyphicon glyphicon-plus pointer';
    var sulkemisTyyli = 'glyphicon glyphicon-minus pointer';
    $scope.ammattitaitovaatimusTyyli = sulkemisTyyli;
    $scope.ammattitaitovaatimuksetSuljettu = false;
    $scope.ammattitaidonOsoittamistavatTyyli = avausTyyli;
    $scope.ammattitaidonOsoittamistavatSuljettu = true;
    $scope.arviointiTyyli = avausTyyli;
    $scope.arviointiSuljettu = true;


    if ($routeParams.konteksti && $scope.kontekstit.indexOf($routeParams.konteksti.toLowerCase()) !== -1) {
      $scope.konteksti = $routeParams.konteksti;
    } else {
      $location.path('/selaus/ammatillinenperuskoulutus');
    }
  
    var perusteHakuPromise = (function() {
      if ($routeParams.perusteId) {
        return Perusteet.get({perusteenId: $routeParams.perusteId}).$promise;
      } else {
        return $q.reject();
      }
    }());
    
    var tutkinnonOsaHakuPromise = (function() {
      if ($routeParams.tutkinnonOsaId) {
        return PerusteenOsat.get({osanId: $routeParams.tutkinnonOsaId}).$promise;
      } else {
        return $q.reject();
      }
    }());
    
    $q.all([perusteHakuPromise, tutkinnonOsaHakuPromise]).then(function(vastaus) {

      var peruste = vastaus[0];

      $scope.peruste = peruste;
      YleinenData.navigaatiopolkuElementit.peruste = peruste.nimi;

      var tutkinnonOsa = vastaus[1];
      $scope.tutkinnonOsa = tutkinnonOsa;
      YleinenData.haeArviointiasteikot();
      YleinenData.navigaatiopolkuElementit.tutkinnonOsa = tutkinnonOsa.otsikko;

      // Data haettu, päivitetään navigaatiopolku
      $rootScope.$broadcast('paivitaNavigaatiopolku');

    }, function(virhe) {
      console.log('VIRHE: ' + virhe.status);
      //Virhe tapahtui, esim. perustetta ei löytynyt. Virhesivu.
      $location.path('/selaus/' + $scope.konteksti);
    });

    $scope.$on('arviointiasteikot', function() {
      $scope.arviointiasteikot = YleinenData.arviointiasteikot;
    });

    $scope.vaihdaAmmattitaitovaatimusNakyvyys = function() {
      $scope.ammattitaitovaatimuksetSuljettu = !$scope.ammattitaitovaatimuksetSuljettu;
      if ($scope.ammattitaitovaatimuksetSuljettu) {
        $scope.ammattitaitovaatimusTyyli = avausTyyli;
      } else {
        $scope.ammattitaitovaatimusTyyli = sulkemisTyyli;
      }
    };
    
    $scope.vaihdaAmmattitaidonOsoittamistavatNakyvyys = function() {
      $scope.ammattitaidonOsoittamistavatSuljettu = !$scope.ammattitaidonOsoittamistavatSuljettu;
      if ($scope.ammattitaidonOsoittamistavatSuljettu) {
        $scope.ammattitaidonOsoittamistavatTyyli = avausTyyli;
      } else {
        $scope.ammattitaidonOsoittamistavatTyyli = sulkemisTyyli;
      }
    };
    
    $scope.vaihdaArviointiNakyvyys = function() {
      $scope.arviointiSuljettu = !$scope.arviointiSuljettu;
      if ($scope.arviointiSuljettu) {
        $scope.arviointiTyyli = avausTyyli;
      } else {
        $scope.arviointiTyyli = sulkemisTyyli;
      }
    };

    $scope.valitseKieli = function(nimi) {
      return YleinenData.valitseKieli(nimi);
    };
    
    var hakuAloitettuKäsittelijä = function() {
      $scope.hakuMenossa = true;
    };

    var hakuLopetettuKäsittelijä = function() {
      $scope.hakuMenossa = false;
    };
    palvelinhaunIlmoitusKanava.kunHakuAloitettu($scope, hakuAloitettuKäsittelijä);
    palvelinhaunIlmoitusKanava.kunHakuLopetettu($scope, hakuLopetettuKäsittelijä);

  });
