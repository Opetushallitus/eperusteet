'use strict';

angular.module('eperusteApp')
  .config(function($stateProvider) {
    $stateProvider
      .state('admin', {
        url: '/admin',
        templateUrl: 'views/admin.html',
        controller: 'AdminCtrl',
        resolve: {
          RyhmienHallintaData: function(RyhmienHallinta) {
            return RyhmienHallinta.promise;
          }
        }
      });
  })
  .controller('AdminCtrl', function($scope, RyhmienHallinta) {
    $scope.ryhmat = RyhmienHallinta.haeRyhmat();
    console.log($scope.ryhmat);
  });
