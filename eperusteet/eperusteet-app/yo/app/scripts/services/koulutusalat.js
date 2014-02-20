'use strict';
/*global _*/

angular.module('eperusteApp')
  /*.service('Koulutusalat', [ '$resource',  function Koulutusalat ($resource) {
      
      var koulutusalatResource = $resource('/eperusteet-service/api' + '/koulutusalat/:koulutusalaId', {koulutusalaId: '@id'}, {'query': {method: 'GET', isArray: true}});
      this.koulutusalatMap;
      this.koulutusalat;
      
      var self = this;
      
      var muunnaKoulutusalat = function(vastaus) {
        var muunnos = [];
        for (var i = 0; i < vastaus.length; i++) {
         // console.log('Koodi ' + vastaus[i].koodi);
          muunnos[vastaus[i].koodi] =
            {
              nimi: vastaus[i].nimi,
              opintoalat: vastaus[i].opintoalat
            };
        }
        return muunnos;
      };
      
      koulutusalatResource.query( function(vastaus) {
        console.log("koulutusalat haettu BE");
        self.koulutusalatMap = muunnaKoulutusalat(vastaus);
        self.koulutusalat = vastaus;
      });
      
      
      this.haeKoulutusalat = function () {
        console.log('haeKoulutusalat ' + self.koulutusalat);
        return self.koulutusalat;
      };
      
      this.haeKoulutusala = function(koodi) {
        return self.koulutusalatMap[koodi];
      };
  
    }
    
  ]);*/


  .factory('Koulutusalat', function($resource, SERVICE_LOC) {
    return $resource(SERVICE_LOC + '/koulutusalat/:koulutusalaId',
      {
        koulutusalaId: '@id'
      }, {'query': {method: 'GET', isArray: true, cache: true}});
  });
  

