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

angular.module('eperusteApp')
  .directive('muokkausTekstikappale', function() {
    return {
      templateUrl: 'views/partials/muokkaus/tekstikappale.html',
      restrict: 'E',
      scope: {
        tekstikappale: '='
      },
      controller: function($scope, $location, Editointikontrollit, PerusteenOsat) {
        
        $scope.panelType = 'panel-default';
        
        function setupTekstikappale(kappale) {
          $scope.editableTekstikappale = angular.copy(kappale);
          
          Editointikontrollit.registerCallback({
            edit: function() {
              console.log('tutkinnon osa - edit');
              $scope.editClass = 'editing';
              $scope.panelType = 'panel-info';
            },
            save: function() {
              $scope.editClass = '';
              $scope.panelType = 'panel-default';
              //TODO: Validate tutkinnon osa
              console.log('validate tekstikappale');
              if($scope.editableTekstikappale.id) {
                $scope.editableTekstikappale.$saveTekstikappale();  
              } else {
                PerusteenOsat.saveTekstikappale($scope.editableTekstikappale).$promise.then(function(response) {
                  $location.path('/muokkaus/tekstikappale/' + response.id);
                });
              }
              $scope.tekstikappale = angular.copy($scope.editableTekstikappale);
            },
            cancel: function() {
              $scope.editClass = '';
              $scope.panelType = 'panel-default';
              console.log('tutkinnon osa - cancel');
              $scope.editableTekstikappale = angular.copy($scope.tekstikappale);
            }
          });
        }
        
        if($scope.tekstikappale) {
          $scope.tekstikappale.$promise.then(function(response) {
            setupTekstikappale(response);
          });
        } else {
          $scope.tekstikappale = {
              nimi: {
                fi: '',
                sv: ''
              },
              teksti: {
                fi: '',
                sv: ''
              }
          };
          setupTekstikappale($scope.tekstikappale);
        }
      }
    };
  });
      