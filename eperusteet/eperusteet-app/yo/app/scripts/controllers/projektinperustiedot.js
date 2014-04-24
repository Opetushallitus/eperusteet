'use strict';

angular.module('eperusteApp')
  .controller('ProjektinperustiedotCtrl', function($scope, PerusteProjektiService, YleinenData) {
  PerusteProjektiService.watcher($scope, 'projekti');

  $scope.tehtavaluokat = [
    { nimi: 'Tehtäväluokka-1'},
    { nimi: 'Tehtäväluokka-2'},
    { nimi: 'Tehtäväluokka-3'},
    { nimi: 'Tehtäväluokka-4'}
  ];
  
  $scope.paatosPvmOpen = false;
  
  $scope.showWeeks = true;
  
  $scope.open = function($event) {
    $event.preventDefault();
    $event.stopPropagation();
    
    $scope.paatosPvmOpen = !$scope.paatosPvmOpen;
  };

  $scope.dateOptions = {
    'year-format': 'yy',
    //'month-format': 'M',
    //'day-format': 'd',
    'starting-day': 1
  };

  $scope.format = YleinenData.dateFormatDatepicker;
});

