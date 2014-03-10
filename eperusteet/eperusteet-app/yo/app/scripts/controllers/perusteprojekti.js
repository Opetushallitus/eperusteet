'use strict';

angular.module('eperusteApp')
  .config(function($routeProvider) {
    $routeProvider
      .when('/perusteprojekti', {
        templateUrl: 'views/perusteprojekti.html',
        controller: 'PerusteprojektiCtrl',
        navigaationimi: 'Perusteprojekti'
      })
      .when('/perusteprojekti/:id', {
        templateUrl: 'views/perusteprojekti.html',
        controller: 'PerusteprojektiCtrl',
        navigaationimiId: 'projektiId'
      });
  }).controller('PerusteprojektiCtrl', function($scope, $rootScope, $location, $routeParams,
    PerusteprojektiResource, PerusteProjektiService, YleinenData) {
  
  $scope.projekti = {};
  var perusteprojektiPolku = 'perusteprojekti/';
  
  $rootScope.$broadcast('paivitaNavigaatiopolku');
  PerusteProjektiService.clean();
  PerusteProjektiService.watcher($scope, 'projekti');
  
  $scope.$watch('projekti.nimi', function(temp) {
    YleinenData.navigaatiopolkuElementit.projektiId = temp;
    $rootScope.$broadcast('paivitaNavigaatiopolku');
    console.log('projekti.nimi', temp);
  });

  $scope.tabs = [
    {otsikko: 'Perustiedot', url: '/views/partials/perusteprojektiPerustiedot.html'},
    {otsikko: 'Projektiryhmä', url: '/views/partials/perusteprojektiProjektiryhma.html'}
  ];

  if ($routeParams.id) {
    $scope.projekti.id = $routeParams.id;
    PerusteprojektiResource.get($scope.projekti, function(vastaus) {
      $scope.projekti = vastaus;
    }, function(virhe) {
      console.log('virhe', virhe);
      $location.path(perusteprojektiPolku);
    });
  }

  $scope.tallennaPerusteprojekti = function() {
    console.log('tallenna projekti');
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
