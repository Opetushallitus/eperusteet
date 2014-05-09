'use strict';
/*global _*/

angular.module('eperusteApp')
  .service('Opintoalat', function Opintoalat($resource, SERVICE_LOC) {
    var opintoalatResource = $resource(SERVICE_LOC + '/opintoalat/',
        {}, {'query': {method: 'GET', isArray: true, cache: true}});
      this.opintoalatMap = {};
      this.opintoalat = [];
      var self = this;

      var opintoalaPromise = opintoalatResource.query().$promise;

      this.haeOpintoalat = function() {
        return self.opintoalat;
      };

      this.haeOpintoalaNimi = function(koodi) {
        return self.opintoalatMap[koodi];
      };

      return opintoalaPromise.then(function(vastaus) {
        
        self.opintoalatMap = _.zipObject(_.pluck(vastaus, 'koodi'), _.map(vastaus, function(e) {
          return {
            nimi: e.nimi
          };
        }));
        self.opintoalat = vastaus;
        return self;
      });
  });
