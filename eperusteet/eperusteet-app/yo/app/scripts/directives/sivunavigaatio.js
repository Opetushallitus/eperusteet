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

'use strict';
/* global _ */

/**
 * Perusteprojektin sivunavigaatioelementti
 */
angular.module('eperusteApp')
  .directive('sivunavigaatio', function() {
    return {
      templateUrl: 'views/partials/sivunavigaatio.html',
      restrict: 'E',
      transclude: true,
      controller: 'sivunavigaatioCtrl'
    };
  })

  .directive('subnavi', function ($compile) {
    return {
      templateUrl: 'views/partials/subnavi.html',
      restrict: 'A',
      scope: {
        subnavi: '='
      },
      controller: function ($scope) {
        $scope.sivunaviopen = {};
        $scope.toggle = function (id, event) {
          $scope.sivunaviopen[id] = !$scope.sivunaviopen[id];
          event.stopPropagation();
        };
        $scope.openFor = function (id) {
          $scope.sivunaviopen[id] = true;
        };
      },
      compile: function(tElement) {
        var contents = tElement.contents().remove();
        var compiledContents;
        return function(scope, iElement) {
          if(!compiledContents) {
            compiledContents = $compile(contents);
          }
          compiledContents(scope, function(clone) {
            iElement.append(clone);
          });
        };
      }
    };
  })
  .controller('sivunavigaatioCtrl', function($rootScope, $scope, $stateParams, $state, SivunavigaatioService, PerusteProjektiService) {
    var lastParams = {};
    $scope.suoritustapa = $stateParams.suoritustapa;
    $scope.menuCollapsed = true;
    $scope.data = {};
    $scope.piilota = true;

    function onStateChange(params) {
      function load() {
        $scope.data = SivunavigaatioService.getData();
      }

      if (_.size(params) !== _.size(lastParams)) {
        load();
      }
      else {
        var isSame = _.isObject(params) && _.isObject(lastParams);
        _.forEach(params, function(v, k) {
          if (!lastParams[k] || lastParams[k] !== v) {
            isSame = false;
          }
        });
        if (!isSame) {
          load();
        }
        lastParams = params;
      }
    }
    onStateChange($stateParams);

    $scope.$on('$stateChangeStart', function() {
      $scope.menuCollapsed = true;
    });

    $scope.$on('$stateChangeSuccess', function(_1, _2, params) {
      onStateChange(params);
    });

    $scope.goBackToMain = function () {
      $state.go('perusteprojekti.suoritustapa.sisalto', {perusteProjektiId: $scope.projekti.id, suoritustapa: PerusteProjektiService.getSuoritustapa()}, {reload: true});
    };

    $scope.toggleSideMenu = function () {
      $scope.menuCollapsed = !$scope.menuCollapsed;
    };

    $scope.isHidden = function() {
      return $scope.data.piilota;
    };
  })
  .service('SivunavigaatioService', function ($stateParams, PerusteprojektiTiedotService) {
    var data = {
      osiot: false,
      piilota: false,
      projekti: {id: 0},
      service: null
    };

    function setData(afterCb) {
      afterCb = afterCb || angular.noop;

      function load(service) {
        data.projekti = service.getProjekti();
        data.projekti.peruste = service.getPeruste();
        data.projekti.peruste.sisalto = service.getSisalto();
      }

      if (data.service) {
          load(data.service);
          afterCb();
      } else {
        PerusteprojektiTiedotService.then(function(res) {
          data.service = res;
          load(data.service);
          afterCb();
        });
      }
    }

    /**
     * Asettaa sivunavigaation tiettyyn tilaan.
     * Suositeltu käyttöpaikka: $stateProvider.state -> onEnter
     * @param data {Object} Mahdolliset asetukset:
     *     osiot: true näyttää projektin kaikki osiot,
     *            false näyttää vain "takaisin"-linkin
     *     piilota: true piilottaa koko navigaatioelementin
     */
    this.aseta = function(config) {
      setData(function() {
        data.osiot = config.osiot || false;
        data.piilota = !!config.piilota;
      });
    };

    this.update = function () {
      data.service.alustaPerusteenSisalto($stateParams, true).then(function () {
        setData();
      });
    };

    this.getData = function() {
      return data;
    };

    function openParent(el) {
      var parentEl = el.closest('.subnavi-lapset').closest('.list-group-item').find('a');
      if (parentEl.length === 0) {
        return;
      }
      var parentId = parentEl.attr('id').split('-').pop();
      parentEl.scope().openFor(parentId);
      return parentEl;
    }

    /**
     * Open (uncollapse) the subnavi tree for given item.
     * Opens the tree up to the item and shows its children also.
     * @param {type} id Id of item in subnavi tree
     * @returns {undefined}
     */
    this.unCollapseFor = function (id) {
      var visibleEl = angular.element('#subnavi-id-'+id);
      var hiddenEl = angular.element('#subnavi-id-'+id+':hidden');
      if (visibleEl.length === 0 && hiddenEl.length === 0) {
        return;
      }
      if (hiddenEl.length > 0) {
        // Element is hidden, need to open parents
        hiddenEl.scope().openFor(id);
        var parent = openParent(hiddenEl);
        openParent(parent);
      } else {
        visibleEl.scope().openFor(id);
      }
    };

    function linktext(id) {
      return angular.element('#subnavi-id-'+id).text();
    }

    /**
     * Sets breadcrumb in ui-view based on given content ids
     */
    this.setCrumb = function (ids) {
      var crumbEl = angular.element('#tekstikappale-crumbs');
      ids.splice(0, 1);
      ids.reverse();
      var crumbs = _.map(ids, function (id) {
        return {name: linktext(id), id: id};
      });
      var scope = crumbEl.scope();
      if (!scope) {
        console.log('Ei pystynyt asettamaan tekstikappaleen murupolkua!');
      } else {
        scope.setCrumbs(crumbs);
      }
    };
  });
