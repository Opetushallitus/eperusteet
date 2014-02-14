'use strict';
/* global _ */

angular.module('eperusteApp', [
    'ngRoute',
    'ngSanitize',
    'ngResource',
    'ngAnimate',
    'pascalprecht.translate',
    'ui.bootstrap',
  ])
  .constant('SERVICE_LOC','/eperusteet-service/api')
  .factory('palvelinHakuInterceptor', function($injector, $q, palvelinhaunIlmoitusKanava) {
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
        return $q.reject(rejection);
      }
    };
  })
  // .config(function($routeProvider, $sceProvider) {
  //   $sceProvider.enabled(true);
  //   $routeProvider.otherwise({
  //       redirectTo: '/selaus/ammatillinenperuskoulutus'
  //     });
  // })
  .config(function($translateProvider) {
      $translateProvider.useStaticFilesLoader({
        prefix: 'localisation/locale-',
        suffix: '.json'
      });
      $translateProvider.preferredLanguage('fi');
  })
  .config(function($httpProvider) {
    // Asetetaan oma interceptor kuuntelemaan palvelinkutsuja
    $httpProvider.interceptors.push('palvelinHakuInterceptor');
    $httpProvider.interceptors.push(['$rootScope', '$q', function($rootScope, $q) {
      return {
        'response': function(response) {
          // var uudelleenohjausStatuskoodit = [401, 403, 412, 500];
          var uudelleenohjausStatuskoodit = [412, 500];
          if (_.indexOf(uudelleenohjausStatuskoodit, response.status) !== -1) {
            // TODO: ota käyttöön poistamalla kommentista
            $rootScope.$emit('event:uudelleenohjattava', response.status);
          }
          return response || $q.when(response);
        },
        'responseError': function(err) {
          return $q.reject(err);
        }
      };
    }]);
  })
  .run(function($rootScope, $modal, $location, $window) {
    var onAvattuna = false;

    $rootScope.$on('event:uudelleenohjattava', function(event, status) {
      if (onAvattuna) {
        return;
      }
      onAvattuna = true;

      function getCasURL() {
        var host = $location.host();
        var port = $location.port();
        var protocol = $location.protocol();
        var cas = '/cas/login';
        var redirectURL = encodeURIComponent($location.absUrl());

        var url = protocol + '://' + host;

        if (port !== 443 && port !== 80) {
          url += ':' + port;
        }

        url += cas + '?service=' + redirectURL;

        return url;
      }

      var casurl = getCasURL();

      var uudelleenohjausModaali = $modal.open({
        templateUrl: 'views/modals/uudelleenohjaus.html',
        controller: 'UudelleenohjausModalCtrl',
        resolve: {
          status: function() { return status; },
          redirect: function() { return casurl; }
        }
      });

      uudelleenohjausModaali.result.then(function () {
      }, function() {
      }).finally(function() {
        onAvattuna = false;
        switch (status) {
        case 500:
          $location.path('/');
          break;
        case 412:
          $window.location.href = casurl;
          break;
        }
      });
    });
  });
