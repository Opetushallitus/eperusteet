'use strict';

angular.module('eperusteApp')
  .factory('Koulutusalat', function($resource, SERVICE_LOC) {
    return $resource(SERVICE_LOC + '/koulutusala/:koulutusalaId',
      {
        koulutusalaId: '@id'
      }, {'query': {method: 'GET', isArray: true}});
  });
  

