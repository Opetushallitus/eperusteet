'use strict';


angular.module('eperusteApp')
  .factory('navigaatiopolku', ['$rootScope', '$location', '$route', 'YleinenData',
    function($rootScope, $location, $route, YleinenData) {

      var navigaatiopolut = [],
        navigaatiopolkuService = {},
        routes = $route.routes;

      var luoNavigaatiopolku = function() {
        navigaatiopolut = [];
        var pathElements = $location.path().split('/'),
          path = '';

        var getRoute = function(route) {
          angular.forEach($route.current.params, function(value, key) {
            route = route.replace(value, ':' + key);
          });
          return route;
        };

        if (pathElements[1] === '') {
          delete pathElements[1];
        }

        angular.forEach(pathElements, function(el) {
          path += path === '/' ? el : '/' + el;
          var route = getRoute(path);

          if (routes[route] && routes[route].navigaationimi) {
            
            navigaatiopolut.push({navigaationimi: routes[route].navigaationimi, polku: path});
            
          } else if (routes[route] && routes[route].navigaationimiId) {
            
            YleinenData.valitseKieli(YleinenData.navigaatiopolkuElementit[routes[route].navigaationimiId]);
            navigaatiopolut.push({navigaationimi: YleinenData.valitseKieli(YleinenData.navigaatiopolkuElementit[routes[route].navigaationimiId]), polku: path});
            
          }
        });
      };

      // Luodaan uusi navigaatiopolku, kun saadaan päivityssanoma.
      $rootScope.$on('paivitaNavigaatiopolku', function() {
        luoNavigaatiopolku();
      });

      navigaatiopolkuService.getKaikki = function() {
        return navigaatiopolut;
      };

      navigaatiopolkuService.getEnsimmainen = function() {
        return navigaatiopolut[0] || {};
      };

      return navigaatiopolkuService;
    }]
    );
