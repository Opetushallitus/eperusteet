'use strict';

angular.module('eperusteApp')
  .controller('PerusteprojektiToimikausiCtrl', function ($scope, YleinenData) {
    
  $scope.kalenteriTilat = {
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

  $scope.format = YleinenData.dateFormatDatepicker;
  });
