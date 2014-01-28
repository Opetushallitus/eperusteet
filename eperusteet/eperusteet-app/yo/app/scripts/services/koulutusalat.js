'use strict';

angular.module('eperusteApp')
  .factory('Koulutusalat', function($resource, SERVICE_LOC) {
    return $resource(SERVICE_LOC + '/koulutusalat/:koulutusalaId',
      {
        koulutusalaId: '@id'
      }, {'query': {method: 'GET', isArray: true}});
  });
  

