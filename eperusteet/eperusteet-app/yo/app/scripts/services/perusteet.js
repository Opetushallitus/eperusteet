'use strict';

angular.module('eperusteApp')
  .factory('PerusteRakenteet', function($resource, SERVICE_LOC) {
    return $resource(SERVICE_LOC + '/perusteet/:perusteenId/rakenne',
      {
        perusteenId: '@id'
      }, {
        query: {method: 'GET', isArray: false}
      });
  })
  .factory('Perusteet', function($resource, SERVICE_LOC) {
    return $resource(SERVICE_LOC + '/perusteet/:perusteenId',
      {
        perusteenId: '@id'
      }, {
        query: {method: 'GET', isArray: false}
      });
  });
