'use strict';

angular.module('eperusteApp')
  .factory('Perusteet', function($resource, SERVICE_LOC) {
    return $resource(SERVICE_LOC + '/perusteet/:perusteenId',
      {
        perusteenId: '@id'
      }, {
        query: {method: 'GET', isArray: false}
      });
  });
