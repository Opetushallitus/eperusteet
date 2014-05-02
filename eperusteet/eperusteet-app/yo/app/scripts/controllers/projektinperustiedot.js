'use strict';

angular.module('eperusteApp')
  .controller('ProjektinperustiedotCtrl', function($scope, PerusteProjektiService, YleinenData) {
  PerusteProjektiService.watcher($scope, 'projekti');

  $scope.tehtavaluokat = [
    'Tehtäväluokka-1',
    'Tehtäväluokka-2',
    'Tehtäväluokka-3',
    'Tehtäväluokka-4'
  ];

  $scope.koulutustyypit = YleinenData.koulutustyypit;
  
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

