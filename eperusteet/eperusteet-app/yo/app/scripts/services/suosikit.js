'use strict';

angular.module('eperusteApp')
  .factory('Suosikit', function($resource, SERVICE_LOC) {
    return $resource(SERVICE_LOC + '/kayttajaprofiili/suosikki/:suosikkiId',
      {
        
      });
  })
  .factory('Suosikitbroadcast', function($rootScope) {
    var suosikitbroadcast = {};

    suosikitbroadcast.suosikitMuuttuivat = function() {
      $rootScope.$broadcast('suosikitMuuttuivat');
    };

    suosikitbroadcast.kieliVaihtui = function() {
      $rootScope.$broadcast('kieliVaihtui');
    };

    return suosikitbroadcast;
  });