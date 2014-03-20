'use strict';


angular.module('eperusteApp')
  .config(function($stateProvider) {
    $stateProvider
      .state('esitys.tutkinnonosa', {
        url: '/:perusteenId/tutkinnonosa/:tutkinnonOsaId',
        templateUrl: 'views/tutkinnonosa.html',
        controller: 'TutkinnonosaCtrl',
        naviRest: [':tutkinnonOsaId'],
        naviConfig: {
          append: true,
        }
      });
  })
  .controller('TutkinnonosaCtrl', function ($q, $scope, $rootScope, $stateParams, $state,
    YleinenData, Navigaatiopolku, PerusteenOsat, Perusteet, palvelinhaunIlmoitusKanava) {

    $scope.tutkinnonOsa = {};
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
      $scope.peruste = vastaus[0];
      Navigaatiopolku.navigaatiopolkuElementit.peruste = $scope.peruste.nimi;

      $scope.tutkinnonOsa = vastaus[1];
      Navigaatiopolku.asetaElementit({ tutkinnonOsaId: $scope.tutkinnonOsa.nimi });
      Navigaatiopolku.haeArviointiasteikot();
    }, function() {
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
