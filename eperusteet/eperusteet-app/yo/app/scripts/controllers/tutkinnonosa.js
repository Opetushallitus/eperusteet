'use strict';


angular.module('eperusteApp')
  .config(function($stateProvider) {
    $stateProvider
      .state('esitys.tutkinnonosa', {
        url: '/:perusteenId/tutkinnonosa/:tutkinnonOsaId',
        templateUrl: 'views/tutkinnonosa.html',
        controller: 'TutkinnonosaCtrl',
        navigaationimiId: 'tutkinnonOsa'
      });
  })
  .controller('TutkinnonosaCtrl', function ($q, $scope, $rootScope, $stateParams, $state,
    YleinenData, PerusteenOsat, Perusteet, palvelinhaunIlmoitusKanava) {

    $scope.tutkinnonOsa = {};
    //$scope.arviointi = {};
//    $scope.arviointiasteikot = {};
    var avausTyyli = 'glyphicon glyphicon-plus pointer';
    var sulkemisTyyli = 'glyphicon glyphicon-minus pointer';
    $scope.ammattitaitovaatimusTyyli = sulkemisTyyli;
    $scope.ammattitaitovaatimuksetSuljettu = false;
    $scope.ammattitaidonOsoittamistavatTyyli = avausTyyli;
    $scope.ammattitaidonOsoittamistavatSuljettu = true;
    $scope.arviointiTyyli = avausTyyli;
    $scope.arviointiSuljettu = true;

    var perusteHakuPromise = (function() {
      if ($stateParams.perusteenId) {
        return Perusteet.get({perusteenId: $stateParams.perusteeId}).$promise;
      } else {
        return $q.reject();
      }
    }());

    var tutkinnonOsaHakuPromise = (function() {
      if ($stateParams.tutkinnonOsaId) {
        return PerusteenOsat.get({osanId: $stateParams.tutkinnonOsaId}).$promise;
      } else {
        return $q.reject();
      }
    }());

    $q.all([perusteHakuPromise, tutkinnonOsaHakuPromise]).then(function(vastaus) {
      console.log(vastaus);
      var peruste = vastaus[0];

      $scope.peruste = peruste;
      YleinenData.navigaatiopolkuElementit.peruste = peruste.nimi;

      var tutkinnonOsa = vastaus[1];
      $scope.tutkinnonOsa = tutkinnonOsa;
      YleinenData.haeArviointiasteikot();
      YleinenData.navigaatiopolkuElementit.tutkinnonOsa = tutkinnonOsa.otsikko;

      // Data haettu, päivitetään navigaatiopolku
      $rootScope.$broadcast('paivitaNavigaatiopolku');

    }, function() {
    });

//    $scope.$on('arviointiasteikot', function() {
//      $scope.arviointiasteikot = YleinenData.arviointiasteikot;
//    });

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
