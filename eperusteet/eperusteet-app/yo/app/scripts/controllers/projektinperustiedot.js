'use strict';

angular.module('eperusteApp')
  .controller('ProjektinperustiedotCtrl', function($scope, PerusteProjektiService) {
  PerusteProjektiService.watcher($scope, 'projekti');

  $scope.tehtavaluokat = [
    { nimi: 'Tehtäväluokka-1'},
    { nimi: 'Tehtäväluokka-2'},
    { nimi: 'Tehtäväluokka-3'},
    { nimi: 'Tehtäväluokka-4'}
  ];
  
  $scope.kalenteriTilat = {
    'paatosPvmButton': false,
    'toimikausiAlkuButton': false,
    'toimikausiLoppuButton': false
  };
  
  $scope.showWeeks = true;
  
  $scope.open = function($event) {
    $event.preventDefault();
    $event.stopPropagation();
    
    for (var key in $scope.kalenteriTilat) {
      if ($scope.kalenteriTilat.hasOwnProperty(key) && key !== $event.target.id) {
        $scope.kalenteriTilat[key] = false;
      }
    }
    $scope.kalenteriTilat[$event.target.id] = !$scope.kalenteriTilat[$event.target.id];
  };

  $scope.dateOptions = {
    'year-format': 'yy',
    //'month-format': 'M',
    //'day-format': 'd',
    'starting-day': 1
  };

  $scope.format = 'd.M.yyyy';
});

