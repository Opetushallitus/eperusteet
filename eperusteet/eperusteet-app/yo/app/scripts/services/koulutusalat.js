'use strict';
/*global _*/

angular.module('eperusteApp')
  .service('Koulutusalat', function Koulutusalat($resource, SERVICE_LOC) {

      var koulutusalatResource = $resource(SERVICE_LOC + '/koulutusalat/:koulutusalaId',
        {koulutusalaId: '@id'}, {'query': {method: 'GET', isArray: true, cache: true}});
      this.koulutusalatMap = {};
      this.koulutusalat = [];
      var self = this;

      var koulutusalaPromise = koulutusalatResource.query().$promise;

      this.haeKoulutusalat = function() {
        return self.koulutusalat;
      };

      this.haeKoulutusalaNimi = function(koodi) {
        return self.koulutusalatMap[koodi];
      };

      return koulutusalaPromise.then(function(vastaus) {
        
        self.koulutusalatMap = _.zipObject(_.pluck(vastaus, 'koodi'), _.map(vastaus, function(e) {
          return {
            nimi: e.nimi
          };
        }));
        self.koulutusalat = vastaus;
        return self;
      });

    });