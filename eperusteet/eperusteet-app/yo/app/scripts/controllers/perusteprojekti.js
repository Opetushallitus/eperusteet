'use strict';
/*global _*/

angular.module('eperusteApp')
  .config(function($routeProvider) {
    $routeProvider
      .when('/perusteprojekti', {
        templateUrl: 'views/perusteprojekti.html',
        controller: 'PerusteprojektiCtrl',
        navigaationimi: 'navi-perusteprojekti',
        resolve: {'koulutusalaService': 'Koulutusalat',
                  'opintoalaService': 'Opintoalat'}
      })
      .when('/perusteprojekti/:id', {
        templateUrl: 'views/perusteprojekti.html',
        controller: 'PerusteprojektiCtrl',
        navigaationimiId: 'projektiId',
        resolve: {'koulutusalaService': 'Koulutusalat',
                  'opintoalaService': 'Opintoalat'}
      });
  }).controller('PerusteprojektiCtrl', function($scope, $rootScope, $location, $routeParams,
    PerusteprojektiResource, PerusteProjektiService, YleinenData, koulutusalaService, opintoalaService) {

    $scope.Koulutusalat = koulutusalaService;
    $scope.Opintoalat = opintoalaService;

    $scope.koodistoHaku = function(koodi) {
      console.log(koodi);
    };

  $scope.alustaProjekti = function() {
    $scope.projekti = {};
    $scope.projekti.peruste = {};
    $scope.projekti.peruste.nimi = {};
    $scope.projekti.peruste.opintoalat = [];
  };
  $scope.alustaProjekti();

  var perusteprojektiPolku = 'perusteprojekti/';

  $rootScope.$broadcast('paivitaNavigaatiopolku');
  PerusteProjektiService.clean();
  PerusteProjektiService.watcher($scope, 'projekti');

  $scope.$watch('projekti.nimi', function(temp) {
    YleinenData.navigaatiopolkuElementit.projektiId = temp;
    $rootScope.$broadcast('paivitaNavigaatiopolku');
  });

  $scope.tabs = [
    {otsikko: 'projekti-perustiedot', url: 'views/partials/perusteprojektiPerustiedot.html'},
    {otsikko: 'projekti-projektiryhmä', url: 'views/partials/perusteprojektiProjektiryhma.html'},
    {otsikko: 'projekti-peruste', url: 'views/partials/perusteprojektiPeruste.html'}
  ];

  if ($routeParams.id) {
    $scope.projekti.id = $routeParams.id;
    PerusteprojektiResource.get($scope.projekti, function(vastaus) {
      $scope.alustaProjekti();
      $scope.projekti = _.merge($scope.projekti, vastaus);
    }, function(virhe) {
      console.log('virhe', virhe);
      $location.path(perusteprojektiPolku);
    });
  }

  $scope.tallennaPerusteprojekti = function() {
    var projekti = PerusteProjektiService.get();
    
    if (projekti.id) {
      PerusteprojektiResource.update(projekti,
        function(vastaus) {
          $location.path(perusteprojektiPolku + vastaus.id);
        }, function(virhe) {
        console.log('virhe: ', virhe);
      });
    } else {
      PerusteprojektiResource.save(projekti,
        function(vastaus) {
          $location.path(perusteprojektiPolku + vastaus.id);
          PerusteProjektiService.perusteprojektiLuotu();
        }, function(virhe) {
        console.log('virhe: ', virhe);
      });
    }
  };

  $scope.paivitaNavigaatiopolku = function (nimi) {
        YleinenData.navigaatiopolkuElementit.projektiId = nimi;
        $rootScope.$broadcast('paivitaNavigaatiopolku');
  };
});
