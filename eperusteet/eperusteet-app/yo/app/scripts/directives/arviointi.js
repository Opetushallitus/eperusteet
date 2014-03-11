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
  .directive('arviointi', function(YleinenData, Editointikontrollit) {
    return {
      templateUrl: 'views/partials/arviointi.html',
      restrict: 'E',
      scope: {
        arviointi: '=',
        editAllowed: '@?editointiSallittu'
      },
      link: function(scope) {
        console.log('setup arviointi');
        scope.editAllowed = scope.editAllowed || 'false';
        
        scope.arviointiasteikot = YleinenData.arviointiasteikot || {};
        scope.showNewKohdealueInput = false;
        
        YleinenData.haeArviointiasteikot();
        
        scope.$on('arviointiasteikot', function() {
          scope.arviointiasteikot = YleinenData.arviointiasteikot;
        });
        
        scope.addNewKohdealue = function() {
          if(scope.uudenKohdealueenNimi === undefined || scope.uudenKohdealueenNimi === null || (angular.isString(scope.uudenKohdealueenNimi) && scope.uudenKohdealueenNimi.length === 0)) {
            return;
          }
          
          if(scope.arviointi === undefined || scope.arviointi === null) {
            scope.arviointi = {};
          }
          
          if(scope.arviointi.arvioinninKohdealueet === undefined || scope.arviointi.arvioinninKohdealueet === null) {
            scope.arviointi.arvioinninKohdealueet = [];
          }
          
       // TODO: Add localization
          var kohdealue = {
              otsikko: {
                fi: scope.uudenKohdealueenNimi
              }
          };
          
          scope.arviointi.arvioinninKohdealueet.push(kohdealue);
          
          scope.uudenKohdealueenNimi = null;
          scope.showNewKohdealueInput = false;
        };
        
        scope.addNewKohde = function(kohdealue, uudenKohteenTiedot) {
          if(kohdealue.arvioinninKohteet === undefined || kohdealue.arvioinninKohteet === null) {
            kohdealue.arvioinninKohteet = [];
          }
          
          console.log(uudenKohteenTiedot.nimi);
          console.log(uudenKohteenTiedot.arviointiasteikko);
          console.log(uudenKohteenTiedot.showInputArea);
          
          // TODO: Add localization
          var kohde = {
              otsikko: {
                fi: uudenKohteenTiedot.nimi
                },
              _arviointiAsteikko: uudenKohteenTiedot.arviointiasteikko.id,
              osaamistasonKriteerit: []
          };
          
          angular.forEach(uudenKohteenTiedot.arviointiasteikko.osaamistasot, function(taso) {
            console.log('push:');
            console.log(taso);
            kohde.osaamistasonKriteerit.push({
                _osaamistaso: taso.id
            });
          });
          console.log(kohde);
          
          kohdealue.arvioinninKohteet.push(kohde);
          console.log(kohdealue);
          uudenKohteenTiedot.nimi = null;
          uudenKohteenTiedot.arviointiasteikko = null;
          
          uudenKohteenTiedot.showInputArea = false;
        };
        
        scope.addNewKriteeri = function(osaamistasonKriteeri, uudenKriteerinTiedot) {
          if(osaamistasonKriteeri.kriteerit === undefined || osaamistasonKriteeri.kriteerit === null) {
            osaamistasonKriteeri.kriteerit = [];
          }
          
          console.log('uusi kriteeri');
          
          osaamistasonKriteeri.kriteerit.push({fi: uudenKriteerinTiedot.teksti});
          uudenKriteerinTiedot.teksti = null;
          uudenKriteerinTiedot.showInput = false;
        };
        
        scope.valitseKieli = function(nimi) {
          return YleinenData.valitseKieli(nimi);
        };
        
        scope.removeItem = function(item, list) {
          console.log('poistetaan');
          _.remove(list, item);
        };
        
        scope.showRemoveButton = function() {
          return scope.editAllowed && Editointikontrollit.editMode;
        };
        
        scope.checkboxClick = function($event) {
          console.log('TESTI');
          $event.stopPropagation();
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
  });
