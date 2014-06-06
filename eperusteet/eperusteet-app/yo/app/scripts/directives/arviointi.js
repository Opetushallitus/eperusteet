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
/*global _*/

angular.module('eperusteApp')
  .directive('arviointi', function(YleinenData) {
    return {
      templateUrl: 'views/partials/arviointi.html',
      restrict: 'E',
      scope: {
        arviointi: '=',
        editAllowed: '@?editointiSallittu',
        editEnabled: '='
      },
      link: function(scope) {
        scope.editAllowed = (scope.editAllowed === 'true' || scope.editAllowed === true);

        scope.arviointiasteikot = YleinenData.arviointiasteikot || {};
        scope.showNewKohdealueInput = false;

        YleinenData.haeArviointiasteikot();

        scope.$on('arviointiasteikot', function() {
          scope.arviointiasteikot = YleinenData.arviointiasteikot;
        });

        scope.addNewKohdealue = function() {
          if(angular.isUndefined(scope.uudenKohdealueenNimi) || scope.uudenKohdealueenNimi === null || (angular.isString(scope.uudenKohdealueenNimi) && _.isEmpty(scope.uudenKohdealueenNimi))) {
            return;
          }

          if(angular.isUndefined(scope.arviointi) || scope.arviointi === null) {
            scope.arviointi = [];
          }

          var kohdealue = {
              otsikko: {}
          };
          kohdealue.otsikko[YleinenData.kieli] = scope.uudenKohdealueenNimi;

          scope.arviointi.push(kohdealue);

          scope.uudenKohdealueenNimi = null;
          scope.showNewKohdealueInput = false;
        };

        scope.addNewKohde = function(kohdealue, uudenKohteenTiedot) {
          if(angular.isUndefined(kohdealue.arvioinninKohteet) || kohdealue.arvioinninKohteet === null) {
            kohdealue.arvioinninKohteet = [];
          }

          var kohde = {
              otsikko: {},
              _arviointiAsteikko: uudenKohteenTiedot.arviointiasteikko.id,
              osaamistasonKriteerit: []
          };
          kohde.otsikko[YleinenData.kieli] = uudenKohteenTiedot.nimi;

          angular.forEach(uudenKohteenTiedot.arviointiasteikko.osaamistasot, function(taso) {
            kohde.osaamistasonKriteerit.push({
                _osaamistaso: taso.id
            });
          });

          kohdealue.arvioinninKohteet.push(kohde);
          uudenKohteenTiedot.nimi = null;
          uudenKohteenTiedot.arviointiasteikko = null;

          uudenKohteenTiedot.showInputArea = false;
        };

        scope.closeNewKohde = function(uudenKohteenTiedot) {
          uudenKohteenTiedot.nimi = null;
          uudenKohteenTiedot.arviointiasteikko = null;

          uudenKohteenTiedot.showInputArea = false;
        };

        scope.addNewKriteeri = function(osaamistasonKriteeri, uudenKriteerinTiedot) {
          if(osaamistasonKriteeri.kriteerit === undefined || osaamistasonKriteeri.kriteerit === null) {
            osaamistasonKriteeri.kriteerit = [];
          }

          var newKriteeri = {};
          newKriteeri[YleinenData.kieli] = uudenKriteerinTiedot.teksti;

          osaamistasonKriteeri.kriteerit.push(newKriteeri);
          uudenKriteerinTiedot.teksti = null;
          uudenKriteerinTiedot.showInput = false;
        };

        scope.valitseKieli = function(nimi) {
          return YleinenData.valitseKieli(nimi);
        };

        scope.removeItem = function(item, list) {
          _.remove(list, item);
        };

        scope.valitseKieli = function(teksti) {
          return YleinenData.valitseKieli(teksti);
        };

        scope.elementDragged = false;

        scope.sortableOptions = {
          axis: 'y',
          start: function() {
            scope.elementDragged = true;
          },
          stop: function() {
            // ei toimi
          }
        };
        scope.kriteeriSortableOptions = {};

        scope.$watch('editEnabled', function (value) {
          scope.sortableOptions.disabled = !value;
          scope.kriteeriSortableOptions.disabled = !value;
        });

        scope.isElementDragged = function() {
          if (scope.elementDragged) {
            scope.elementDragged = false;
            return true;
          } else {
            return false;
          }
        };

        /**
         * is-open attribuutti on annettava modelina accordionille, jotta
         * ui-sortable voidaan disabloida lukutilassa.
         * Accordionin tiloja seurataan suoraan modelin datassa. Haittapuoli
         * on se, että tallennettaessa pitää siivota accordionOpen-tagit pois.
         */
        function setAccordion(mode) {
          var obj = scope.arviointi;
          _.each(obj, function (kohdealue) {
            kohdealue.accordionOpen = mode;
            _.each(kohdealue.arvioinninKohteet, function (kohde) {
              kohde.accordionOpen = mode;
            });
          });
        }

        function accordionState() {
          var obj = _.first(scope.arviointi);
          return obj && obj.accordionOpen;
        }

        scope.toggleAll = function () {
          setAccordion(!accordionState());
        };

        setAccordion(true);
      }
    };
  })
  .directive('onEnter', function() {
    return function (scope, element, attrs) {
      element.bind('keydown keypress', function (event) {
        if(event.which === 13) {
          scope.$apply(function (){
            scope.$eval(attrs.onEnter);
          });

          event.preventDefault();
        }
      });
    };
  })
  .directive('onEsc', function() {
    return function(scope, element, attrs) {
      element.bind('keydown keypress', function (event) {
        if(event.which === 27) {
          scope.$apply(function (){
            scope.$eval(attrs.onEsc);
          });

          event.preventDefault();
        }
      });
    };
  })
  .directive('focusMe', function($timeout) {
    return function(scope, element, attrs) {
      scope.$watch(attrs.focusMe, function(value) {
        if(value === true) {
          $timeout(function() {
            element[0].focus();
          }, 100);
        }
      });
    };
  })

  .controller('arvioinninTekstiKenttaCtrl', function ($scope) {
    $scope.muokkaustila = false;

    $scope.siirraYlos = function(item, list, $event) {
      var index = _.indexOf(list, item);
      list[index] = null;
      list[index] = list[index-1];
      list[index-1] = item;
      $event.stopPropagation();
    };

    $scope.siirraAlas = function(item, list, $event) {
      var index = _.indexOf(list, item);
      list[index] = null;
      list[index] = list[index+1];
      list[index+1] = item;
      $event.stopPropagation();
    };

    $scope.poistaAlkio = function(item, list) {
      _.remove(list, item);
    };

    $scope.estaEventti = function($event) {
      $event.stopPropagation();
    };

    $scope.asetaMuokkaustila = function(mode, $event) {
      $scope.muokkaustila = mode;
      if ($event) {
        $event.stopPropagation();
      }
    };

    $scope.hyvaksyMuutos = function () {
      $scope.asetaMuokkaustila(false);
    };

    $scope.peruMuutos = function () {
      // TODO palauta vanha teksti
      $scope.asetaMuokkaustila(false);
    };
  })

  .directive('arvioinninTekstikentta', function() {
    return {
      templateUrl: 'views/partials/arvioinninTekstikentta.html',
      restrict: 'E',
      scope: {
        sisalto: '=',
        sisaltoalue: '=',
        editAllowed: '=',
        sisaltoteksti: '=?',
        clickable: '@?'
      },
      controller: 'arvioinninTekstiKenttaCtrl',
      link: function (scope) {
        scope.editAllowed = (scope.editAllowed === 'true' || scope.editAllowed === true);
        scope.teksti = !scope.sisaltoteksti ? scope.sisalto : scope.sisaltoteksti;
      }
    };
  });

// Kustomoitu accordion group, lisätty isElementDragged-tarkastelu
// jotta ui-sortable toimii accordionin kanssa.
angular.module('template/accordion/accordion-group.html', []).run(['$templateCache', function($templateCache) {
  $templateCache.put('template/accordion/accordion-group.html',
    '<div class="panel panel-default">\n' +
    '  <div class="panel-heading">\n' +
    '    <h4 class="panel-title">\n' +
    '      <a class="accordion-toggle" ng-click="$parent.isElementDragged() || (isOpen = !isOpen)" accordion-transclude="heading">{{heading}}</a>\n' +
    '    </h4>\n' +
    '  </div>\n' +
    '  <div class="panel-collapse" collapse="!isOpen">\n' +
    '	  <div class="panel-body" ng-transclude></div>\n' +
    '  </div>\n' +
    '</div>');
}]);
