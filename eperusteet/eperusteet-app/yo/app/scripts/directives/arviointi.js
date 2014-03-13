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
          console.log('add new kohdealue');
          if(angular.isUndefined(scope.uudenKohdealueenNimi) || scope.uudenKohdealueenNimi === null || (angular.isString(scope.uudenKohdealueenNimi) && _.isEmpty(scope.uudenKohdealueenNimi))) {
            return;
          }
          console.log(scope.uudenKohdealueenNimi);
          
          if(angular.isUndefined(scope.arviointi) || scope.arviointi === null) {
            scope.arviointi = {};
          }
          
          if(angular.isUndefined(scope.arviointi.arvioinninKohdealueet) || scope.arviointi.arvioinninKohdealueet === null) {
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
          if(angular.isUndefined(kohdealue.arvioinninKohteet) || kohdealue.arvioinninKohteet === null) {
            kohdealue.arvioinninKohteet = [];
          }
          
          // TODO: Add localization
          var kohde = {
              otsikko: {
                fi: uudenKohteenTiedot.nimi
                },
              _arviointiAsteikko: uudenKohteenTiedot.arviointiasteikko.id,
              osaamistasonKriteerit: []
          };
          
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
        
        scope.addNewKriteeri = function(osaamistasonKriteeri, uudenKriteerinTiedot) {
          if(osaamistasonKriteeri.kriteerit === undefined || osaamistasonKriteeri.kriteerit === null) {
            osaamistasonKriteeri.kriteerit = [];
          }
                    
          osaamistasonKriteeri.kriteerit.push({fi: uudenKriteerinTiedot.teksti});
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
          console.log('enter');
          scope.$apply(function (){
            scope.$eval(attrs.onEnter);
          });
  
          event.preventDefault();
        }
      });
    };
  })
  .directive('arvioinninTekstikentta', function(YleinenData, $filter) {
    return {
      template: 
      '<span class="tekstikentta" ng-hide="editContent">{{valitseKieli(teksti)}}</span>' +
      '<span ng-show="editAllowed">' +
        '<input class="form-control" ng-show="editContent" ng-model="teksti" localized editointi-kontrolli ng-click="blockEvent($event)" ng-blur="editContent = false;" on-enter="editContent = false;"/>' +
        '<span ng-click="removeItem(sisalto, sisaltoalue)" ng-show="!editContent" editointi-kontrolli class="glyphicon glyphicon-remove clickable pull-right badge"> </span>' +
        '<span ng-click="switchEditMode(true, $event)" ng-show="!editContent" editointi-kontrolli class="glyphicon glyphicon-pencil clickable pull-right badge"> </span>' +
      '</span>',
      restrict: 'E',
      scope: {
        sisalto: '=',
        sisaltoalue: '=',
        editAllowed: '=',
        sisaltoteksti: '=?'
      },
      link: function(scope, element, attrs) {
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
        
        scope.switchEditMode = function(mode, $event) {
          scope.editContent = mode;
          $event.stopPropagation();
        };
        
        scope.blockEvent = function($event) {
          $event.stopPropagation();
        }
      }
    };
  });
