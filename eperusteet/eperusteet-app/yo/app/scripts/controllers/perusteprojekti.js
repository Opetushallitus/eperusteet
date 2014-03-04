'use strict';

angular.module('eperusteApp')
  .config(function($routeProvider) {
    $routeProvider
      .when('/perusteprojekti', {
        templateUrl: 'views/perusteprojekti.html',
        controller: 'PerusteprojektiCtrl',
        navigaationimi: 'Perusteprojekti'
      });
  }).controller('PerusteprojektiCtrl', function($scope, $rootScope, PerusteprojektiResource, PerusteProjektiService) {
    
  $rootScope.$broadcast('paivitaNavigaatiopolku');
  
  $scope.projekti = {};

  $scope.tabs = [
    {otsikko: 'Perustiedot', url: '/views/partials/perusteprojektiPerustiedot.html'},
    {otsikko: 'Projektiryhmä', url: '/views/partials/perusteprojektiProjektiryhma.html'}
  ];

  /*$scope.tallennaProjekti = function(projekti) {
    PerusteProjektiService.save(projekti);
    console.log(PerusteProjektiService.get());
  };*/

  $scope.tehtavaluokat = [
    { nimi: 'Tehtäväluokka-1'},
    { nimi: 'Tehtäväluokka-2'},
    { nimi: 'Tehtäväluokka-3'},
    { nimi: 'Tehtäväluokka-4'}
  ];

  $scope.tallennaPerusteprojekti = function() {
    var projekti = PerusteProjektiService.get();
    PerusteprojektiResource.save(
      {
      nimi: projekti.projektinNimi,
      diaarinumero: projekti.diaarinumero,
      paatosPvm: projekti.paatosPvm,
      tehtavaluokka: projekti.tehtavaluokka
      },
      function(vastaus) {
      console.log('vastaus: ', vastaus);
      
      
      }, function(virhe) {
        console.log('virhe: ', virhe);
      });
  };

  
});
