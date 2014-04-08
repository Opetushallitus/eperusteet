'use strict';

angular.module('eperusteApp')
  .config(function($stateProvider) {
    $stateProvider
      .state('aloitussivu', {
        url: '/',
        naviBase: [],
        templateUrl: 'views/aloitussivu.html'
      });
  });
