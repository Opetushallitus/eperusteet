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
        editAllowed: '@?editointiSallittu'
      },
      link: function(scope) {
        scope.editAllowed = scope.editAllowed || 'false';
        scope.editEnabled = false;
        
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
            scope.arviointi = {};
          }
          
          if(angular.isUndefined(scope.arviointi.arvioinninKohdealueet) || scope.arviointi.arvioinninKohdealueet === null) {
            scope.arviointi.arvioinninKohdealueet = [];
          }
          
          var kohdealue = {
              otsikko: {}
          };
          kohdealue.otsikko[YleinenData.kieli] = scope.uudenKohdealueenNimi;
          
          scope.arviointi.arvioinninKohdealueet.push(kohdealue);
          
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
  .directive('arvioinninTekstikentta', function(YleinenData, $filter) {
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
      link: function(scope, element) {
        scope.editContent = false;
        scope.teksti = !scope.sisaltoteksti ? scope.sisalto : scope.sisaltoteksti;
        
        scope.valitseKieli = function(teksti) {
          var lokalisoituTeksti = YleinenData.valitseKieli(teksti);
          
          if(angular.isUndefined(lokalisoituTeksti) || _.isEmpty(lokalisoituTeksti)) {
            element.addClass('has-placeholder');
            return $filter('translate')('tyhjä');
          } else {
            element.removeClass('has-placeholder');
            return lokalisoituTeksti;
          }
        };
        
        scope.removeItem = function(item, list) {
          _.remove(list, item);
        };
        
        scope.moveUp = function(item, list, $event) {
          var index = _.indexOf(list, item);
          list[index] = null;
          list[index] = list[index-1];
          list[index-1] = item;
          $event.stopPropagation();
        };
        
        scope.moveDown = function(item, list, $event) {
          var index = _.indexOf(list, item);
          list[index] = null;
          list[index] = list[index+1];
          list[index+1] = item;
          $event.stopPropagation();
        };
       
        scope.switchEditMode = function(mode, $event) {
          scope.editContent = mode;
          $event.stopPropagation();
        };
        
        scope.blockEvent = function($event) {
          $event.stopPropagation();
        };
      }
    };
  });
