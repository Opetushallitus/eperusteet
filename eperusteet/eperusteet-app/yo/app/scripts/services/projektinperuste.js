'use strict';

angular.module('eperusteApp')
  .factory('Projektinperuste', function ($resource, SERVICE_LOC) {
    return $resource(SERVICE_LOC + '/koodisto/relaatio/sisaltyy-alakoodit/:koodi');
  });
