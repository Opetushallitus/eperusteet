'use strict';
/* global _ */

angular.module('eperusteApp')
  .factory('navigaatiopolku',
    function($rootScope, $location, $state, YleinenData, $translate,$q) {

      var navigaatiopolut = [],
        navigaatiopolkuService = {},
        routes = [];

      // routes = _($state.get())
      //           .filter(function(o) { return !o.abstract; })
      //           .map(function(o) { return o.url; })
      //           .value();

      var luoNavigaatiopolku = function() {
        navigaatiopolut = [];
        var pathElements = $location.path().split('/'),
          path = '';

        var getRoute = function(route) {
          angular.forEach($state.current.params, function(value, key) {
            route = route.replace(value, ':' + key);
          });
          return route;
        };

        if (pathElements[0] === '') {
          pathElements.splice(0, 1);
        }

        var polku = [];

        angular.forEach(pathElements, function(el) {
          path += path === '/' ? el : '/' + el;
          var route = getRoute(path);

          console.log(routes, route);

          if (routes[route] && routes[route].navigaationimi) {

            var t = $translate(routes[route].navigaationimi);
            var pathTmp = path;
            var p = t.then(function(nimi) {
              return {navigaationimi: nimi, polku: pathTmp};
            });
            polku.push(p);
          } else if (routes[route] && routes[route].navigaationimiId) {
            if (YleinenData.valitseKieli(YleinenData.navigaatiopolkuElementit[routes[route].navigaationimiId]) !== '') {
              polku.push($q.when({navigaationimi: YleinenData.valitseKieli(YleinenData.navigaatiopolkuElementit[routes[route].navigaationimiId]), polku: path}));
            } else {
              polku.push($q.when({navigaationimi: YleinenData.navigaatiopolkuElementit[routes[route].navigaationimiId], polku: path}));
            }
          }
        });
        $q.all(polku).then(function(values) {
          console.log('values', values);
          navigaatiopolut = values;
        });
      };

      // Luodaan uusi navigaatiopolku, kun saadaan päivityssanoma.
      $rootScope.$on('paivitaNavigaatiopolku', function() {
        luoNavigaatiopolku();
      });

      $rootScope.$on('$translateChangeSuccess', function () {
        luoNavigaatiopolku();
      });

      navigaatiopolkuService.getKaikki = function() {
        return navigaatiopolut;
      };

      navigaatiopolkuService.getEnsimmainen = function() {
        return navigaatiopolut[0] || {};
      };

      return navigaatiopolkuService;
  });
