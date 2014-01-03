'use strict';

angular.module('eperusteApp', ['ngRoute', 'ngSanitize', 'ngResource', 'pascalprecht.translate'])
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
  .config(['$translateProvider', '$httpProvider', function($translateProvider, $httpProvider) {
      $translateProvider.useStaticFilesLoader({
        prefix: 'localisation/locale-',
        suffix: '.json'
      });
      $translateProvider.preferredLanguage('fi');

      // Asetetaan oma interceptor kuuntelemaan palvelinkutsuja
      $httpProvider.interceptors.push('palvelinHakuInterceptor');
    }]);

