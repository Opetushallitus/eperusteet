'use strict';

angular.module('eperusteApp')
  .factory('Perusteprojekti', function($resource, SERVICE_LOC) {
    return $resource(SERVICE_LOC + '/perusteprojekti');
  });
