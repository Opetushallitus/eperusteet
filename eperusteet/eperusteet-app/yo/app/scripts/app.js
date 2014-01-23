'use strict';
/*global _*/


angular.module('eperusteApp', ['ngRoute', 'ngSanitize', 'ngResource', 'pascalprecht.translate', 'ui.bootstrap'])
  .constant('SERVICE_LOC','/eperusteet-service/api')
  .factory('palvelinHakuInterceptor', function($injector, palvelinhaunIlmoitusKanava) {
    var http;
    return {
      'request': function(config) {
        palvelinhaunIlmoitusKanava.hakuAloitettu();
        return config;
      },
      'requestError': function(rejection) {
        // TODO: pitäisikö olla sama toteutus kuin responsella?
        return rejection;
      },
      'response': function(response) {
        // Injektoidaan $http injector:illa, jotta estetään riippuvuuksien circular dependency
        http = http || $injector.get('$http');
        // Ei lähetetä ilmoitusta ennen kuin kaikki haut ovat päättyneet
        if (http.pendingRequests.length < 1) {
          // Lähetetään ilmoitus, että haut ovat päättyneet.
          palvelinhaunIlmoitusKanava.hakuLopetettu();
        }
        return response;
      },
      'responseError': function(rejection) {
        // Injektoidaan $http injector:illa, jotta estetään riippuvuuksien circular dependency
        http = http || $injector.get('$http');
        // Ei lähetetä ilmoitusta ennen kuin kaikki haut ovat päättyneet
        if (http.pendingRequests.length < 1) {
          // Lähetetään ilmoitus, että haut ovat päättyneet.
          palvelinhaunIlmoitusKanava.hakuLopetettu();
        }
        return rejection;
      }
    };
  })
  .config(function($routeProvider, $sceProvider) {
    $sceProvider.enabled(true);
    $routeProvider
      .when('/muokkaus', {
        templateUrl: 'views/muokkaus.html',
        controller: 'MuokkausCtrl'
      })
      .when('/selaus/:konteksti', {
        templateUrl: 'views/haku.html',
        controller: 'HakuCtrl'
      })
      .when('/selaus/:konteksti/:perusteId', {
        templateUrl: 'views/esitys.html',
        controller: 'EsitysCtrl',
        //Estää sisällysluettelossa navigoinnin lataamasta sivua uudelleen
        reloadOnSearch: false
      })
      .otherwise({
        redirectTo: '/selaus/ammatillinenperuskoulutus'
      });
  })
  .config(function($translateProvider, $httpProvider) {
      $translateProvider.useStaticFilesLoader({
        prefix: 'localisation/locale-',
        suffix: '.json'
      });
      $translateProvider.preferredLanguage('fi');

      // Asetetaan oma interceptor kuuntelemaan palvelinkutsuja
      $httpProvider.interceptors.push('palvelinHakuInterceptor');
  })
  .config(function($httpProvider) {
    $httpProvider.interceptors.push(function($rootScope, $q) {
      return {
        'response': function(response) {
          var uudelleenohjausStatuskoodit = [401, 403, 500];
          if (_.indexOf(uudelleenohjausStatuskoodit, response.status) !== -1) {
            // TODO: ota käyttöön poistamalla kommentista
            // $rootScope.$emit('event:uudelleenohjattava', response.status);
          }
          return response || $q.when(response);
        },
        'responseError': function(err) {
          return $q.reject(err);
        }
      };
    });
  })
  .run(function($rootScope, $modal, $location) {
    var onAvattuna = false;

    $rootScope.$on('event:uudelleenohjattava', function(event, status) {
      if (onAvattuna) {
        return;
      }
      onAvattuna = true;

      var uudelleenohjausModaali = $modal.open({
        templateUrl: 'views/modals/uudelleenohjaus.html',
        controller: 'UudelleenohjausModalCtrl',
        resolve: {
          status: function() { return status; }
        }
      });

      uudelleenohjausModaali.result.then(function () {
      }, function() {
      }).finally(function() {
        onAvattuna = false;
        if (status === 500) {
          $location.path('/');
        }
      });
    });
  });
