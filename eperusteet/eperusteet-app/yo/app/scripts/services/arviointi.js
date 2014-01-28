'use strict';


angular.module('eperusteApp')
  .factory('Arviointi', function($resource, SERVICE_LOC) {
    return $resource(SERVICE_LOC + '/arvioinnit/:arviointiId', {
      arviointiId: '@id'
    });
  });