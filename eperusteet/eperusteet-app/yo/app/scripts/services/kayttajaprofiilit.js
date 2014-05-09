'use strict';

angular.module('eperusteApp')
  .factory('Kayttajaprofiilit', function($resource, SERVICE_LOC) {
    return $resource(SERVICE_LOC + '/kayttajaprofiili/:id',
      {
        id: '@id'
      }
    );
  });
