/*
 * Copyright (c) 2013 The Finnish Board of Education - Opetushallitus
 *
 * This program is free software: Licensed under the EUPL, Version 1.1 or - as
 * soon as they will be approved by the European Commission - subsequent versions
 * of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at: http://ec.europa.eu/idabc/eupl
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * European Union Public Licence for more details.
 */

angular.module('eperusteApp', [
  'ngRoute',
  'ngSanitize',
  'ui.router',
  'ngResource',
  'ngAnimate',
  'pascalprecht.translate',
  'ui.bootstrap',
  'ui.utils',
  'ui.sortable',
  'monospaced.elastic',
  'ui.tree',
  'angular-data.DSCacheFactory',
  'ui.select',
  'eperusteet.esitys',
  'ngFileUpload',
  'eGenericTree',
  'eMathDisplay'
])
  .constant('SERVICE_LOC', '/eperusteet-service/api')
  // .constant('ORGANISATION_SERVICE_LOC', '/organisaatio-service/rest')
  .constant('ORGANISATION_SERVICE_LOC', '/lokalisointi/cxf/rest/v1/localisation')
  .constant('LOKALISOINTI_SERVICE_LOC', '')
  .constant('AUTHENTICATION_SERVICE_LOC', '/authentication-service/resources')
  .constant('REQUEST_TIMEOUT', 10000)
  .constant('SPINNER_WAIT', 100)
  .constant('NOTIFICATION_DELAY_SUCCESS', 4000)
  .constant('NOTIFICATION_DELAY_WARNING', 10000)
  .constant('LUKITSIN_MINIMI', 5000)
  .constant('LUKITSIN_MAKSIMI', 20000)
  .constant('TEXT_HIERARCHY_MAX_DEPTH', 8)
  .constant('SHOW_VERSION_FOOTER', true)
  .config(function($urlRouterProvider, $sceProvider) {
    $sceProvider.enabled(true);
    $urlRouterProvider.when('', '/');
    $urlRouterProvider.otherwise(function($injector, $location) {
      $injector.get('virheService').setData({path: $location.path()});
      $injector.get('$state').go('root.virhe');
    });
  })
  .config(function (epEsitysSettingsProvider) {
    epEsitysSettingsProvider.setValue('perusopetusState', 'root.selaus.perusopetus');
    epEsitysSettingsProvider.setValue('showPreviewNote', true);
  })
  .config(function($translateProvider, $urlRouterProvider) {
    var preferred = 'fi';
    $urlRouterProvider.when('/', '/' + preferred);
    $translateProvider.useLoader('LokalisointiLoader');
    $translateProvider.preferredLanguage(preferred);
    moment.lang(preferred);
  })
  .config(function($rootScopeProvider) {
    // workaround for infdig with recursive tree structures
    $rootScopeProvider.digestTtl(20);
  })
  .config(function($httpProvider) {
    $httpProvider.interceptors.push(['UiKieli', function(kieli) {
      return {
        request: function(config) {
          if ( kieli && kieli.kielikoodi ) {
            config.headers['Accept-Language'] = kieli.kielikoodi;
          }
          return config;
        }
      };
    }]);

    $httpProvider.interceptors.push(['$rootScope', '$q', 'SpinnerService', function($rootScope, $q, Spinner) {
        return {
          request: function(request) {
            Spinner.enable();
            return request;
          },
          response: function(response) {
            Spinner.disable();
            return response || $q.when(response);
          },
          responseError: function(error) {
            Spinner.disable();
            return $q.reject(error);
          }
        };
      }]);
  })
  // Uudelleenohjaus autentikointiin ja palvelinvirheiden ilmoitukset
  .config(function($httpProvider) {
    // Asetetaan oma interceptor kuuntelemaan palvelinkutsuja
    $httpProvider.interceptors.push(['$rootScope', '$q', function($rootScope, $q) {
        return {
          'response': function(response) {
            var uudelleenohjausStatuskoodit = [401, 412, 500];
            var fail = _.indexOf(uudelleenohjausStatuskoodit, response.status) !== -1;

            if (fail) {
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
  // Lodash mixins and other stuff
  .run(function($log) {
    _.mixin({arraySwap: function(array, a, b) {
        if (_.isArray(array) && _.size(array) > a && _.size(array) > b) {
          var temp = array[a];
          array[a] = array[b];
          array[b] = temp;
        }
        return array;
      }});
    _.mixin({zipBy: function(array, kfield, vfield) {
        if (_.isArray(array) && kfield) {
          if (vfield) {
            return _.zipObject(_.map(array, kfield), _.map(array, vfield));
          }
          else {
            return _.zipObject(_.map(array, kfield), array);
          }
        }
        else {
          return {};
        }
      }});
    _.mixin({set: function(obj, field) {
        return function(value) {
          obj[field] = value;
        };
      }});
    _.mixin({setWithCallback: function(obj, field, cb) {
        return function(value) {
          cb = cb || angular.noop;
          obj[field] = value;
          cb(value);
        };
      }});
    _.mixin({flattenTree: function (obj, extractChildren) {
        if (!_.isArray(obj) && obj) {
          obj = [obj];
        }
        if (_.isEmpty(obj)) {
          return [];
        }
        return _.union(obj, _(obj).map(function(o) {
          return _.flattenTree(extractChildren(o), extractChildren);
        }).flatten().value());
      }});
    _.mixin({reducedIndexOf: function (obj, extractor, combinator) {
      if (!_.isArray(obj) && obj) {
        obj = [obj];
      }
      var results = {};
      _.each(obj, function(o) {
        var index = extractor(o);
        if (results[index]) {
          results[index] = combinator(results[index], o);
        } else {
          results[index] = o;
        }
      });
      return results;
    }});
  })
  .run(function($rootScope) {
    var f = _.debounce(function() {
      $rootScope.$broadcast('poll:mousemove');
    }, 10000, {
      leading: true,
      maxWait: 60000
    });
    angular.element(window).on('mousemove', f);
  })
  .run(function($rootScope, $modal, $location, $window, $state, $http, paginationConfig, Editointikontrollit,
    Varmistusdialogi, Kaanna, virheService, $log) {
    paginationConfig.firstText = '';
    paginationConfig.previousText = '';
    paginationConfig.nextText = '';
    paginationConfig.lastText = '';
    paginationConfig.maxSize = 5;
    paginationConfig.rotate = false;

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

      if (status === 401) {
        $window.location.href = casurl;
        return;
      }

      var uudelleenohjausModaali = $modal.open({
        templateUrl: 'views/modals/uudelleenohjaus.html',
        controller: 'UudelleenohjausModalCtrl',
        resolve: {
          status: function() {
            return status;
          },
          redirect: function() {
            return casurl;
          }
        }
      });

      uudelleenohjausModaali.result.then(angular.noop).catch(angular.noop).finally(function() {
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

    $rootScope.$on('$stateChangeStart', function(event, toState, toParams, fromState, fromParams) {
      $rootScope.lastState = {
        state: _.clone(fromState),
        params: _.clone(fromParams)
      };

      if (Editointikontrollit.getEditMode() && fromState.name !== 'root.perusteprojekti.suoritustapa.tutkinnonosat' &&
        fromState.name !== 'root.perusteprojekti.suoritustapa.koulutuksenosa') {
        event.preventDefault();

        var data = {toState: toState, toParams: toParams};
        Varmistusdialogi.dialogi({
          successCb: function(data) {
            $state.go(data.toState, data.toParams);
          }, data: data, otsikko: 'vahvista-liikkuminen', teksti: 'tallentamattomia-muutoksia',
          lisaTeksti: 'haluatko-jatkaa',
          primaryBtn: 'poistu-sivulta'
        })();
      }
    });

    $rootScope.$on('$stateChangeError', function(event, toState, toParams, fromState, fromParams, error) {
      $log.error(error);
      virheService.virhe({state: toState.name});
    });

    $rootScope.$on('$stateNotFound', function(event, toState) {
      virheService.virhe({state: toState.to});
    });

    // Jos käyttäjä editoi dokumenttia ja koittaa poistua palvelusta (reload, iltalehti...), niin varoitetaan, että hän menettää muutoksensa jos jatkaa.
    $window.addEventListener('beforeunload', function(event) {
      if (Editointikontrollit.getEditMode()) {
        var confirmationMessage = Kaanna.kaanna('tallentamattomia-muutoksia');
        (event || window.event).returnValue = confirmationMessage;
        return confirmationMessage;
      }
    });
  })
  .run(function($templateCache) {
    //angular-ui-select korjaus (IE9)
    var expected = '<ul class=\"ui-select-choices ui-select-choices-content dropdown-menu\" role=\"listbox\" ng-show=\"$select.items.length > 0\"><li class=\"ui-select-choices-group\" id=\"ui-select-choices-{{ $select.generatedId }}\"><div class=\"divider\" ng-show=\"$select.isGrouped && $index > 0\"></div><div ng-show=\"$select.isGrouped\" class=\"ui-select-choices-group-label dropdown-header\" ng-bind=\"$group.name\"></div><div id=\"ui-select-choices-row-{{ $select.generatedId }}-{{$index}}\" class=\"ui-select-choices-row\" ng-class=\"{active: $select.isActive(this), disabled: $select.isDisabled(this)}\" role=\"option\"><a href=\"javascript:void(0)\" class=\"ui-select-choices-row-inner\"></a></div></li></ul>';
    var fix      = '<ul class=\"ui-select-choices ui-select-choices-content dropdown-menu\" role=\"listbox\" ng-show=\"$select.items.length > 0\"><li class=\"ui-select-choices-group\" id=\"ui-select-choices-{{ $select.generatedId }}\"><div class=\"divider\" ng-show=\"$select.isGrouped && $index > 0\"></div><div ng-show=\"$select.isGrouped\" class=\"ui-select-choices-group-label dropdown-header\" ng-bind=\"$group.name\"></div><div id=\"ui-select-choices-row-{{ $select.generatedId }}-{{$index}}\" class=\"ui-select-choices-row\" ng-class=\"{active: $select.isActive(this), disabled: $select.isDisabled(this)}\" role=\"option\"><a href=\"javascript:void(0)\" onclick=\"return false;\" class=\"ui-select-choices-row-inner\"></a></div></li></ul>';
    $templateCache.put('eperusteet/ui-select-choices-fix.html', fix);

    if ( $templateCache.get('bootstrap/choices.tpl.html') === expected ) {
      $templateCache.put('bootstrap/choices.tpl.html', fix);
    } else {
      console.warn('angular-ui-select korjaus (IE9), bootstrap/choices.tpl.html on muuttunut');
    }
  })
  .run(function($rootScope) {
    $rootScope.$$isEmpty = _.isEmpty;
  });
  // For debugging
  // .run(function($rootScope) {
  //   $rootScope.$on('$stateChangeSuccess', function(event, toState, toParams, fromState, fromParams, error) {
  //     console.log('Success', event, toState, error);
  //   });
  //   $rootScope.$on('$stateChangeError', function(event, toState, toParams, fromState, fromParams, error) {
  //     console.log('Failure', event, error);
  //   });
  // });
