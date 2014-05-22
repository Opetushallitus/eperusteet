'use strict';

angular.module('eperusteApp')
  .config(function($stateProvider) {
    $stateProvider
      .state('aloitussivu', {
        url: '/',
        templateUrl: 'views/aloitussivu.html'
      });
  });
