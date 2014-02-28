'use strict';

angular.module('eperusteApp')
  .config(function($routeProvider) {
    $routeProvider
      .when('/perusteprojekti', {
        templateUrl: 'views/perusteprojekti.html',
        controller: 'PerusteprojektiCtrl',
        navigaationimi: 'Perusteprojekti'
      });
  })
  .controller('PerusteprojektiCtrl', function ($scope) {
    
  });
