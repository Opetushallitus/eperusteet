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
  .directive('muokkausTutkinnonosa', function() {
    return {
      templateUrl: 'views/partials/muokkaus/tutkinnonosa.html',
      restrict: 'E',
      scope: {
        tutkinnonOsa: '=tutkinnonOsa'
      },
      controller: function($scope, $location, Editointikontrollit, PerusteenOsat) {
        
        function setupTutkinnonOsa(osa) {
          $scope.editableTutkinnonOsa = angular.copy(osa);
          
          Editointikontrollit.registerCallback({
            edit: function() {
              console.log('tutkinnon osa - edit');
            },
            save: function() {
              //TODO: Validate tutkinnon osa
              console.log('validate tutkinnon osa');
              if($scope.editableTutkinnonOsa.id) {
                $scope.editableTutkinnonOsa.$saveTutkinnonOsa();  
              } else {
                PerusteenOsat.saveTutkinnonOsa($scope.editableTutkinnonOsa).$promise.then(function(response) {
                  $location.path('/muokkaus2/tutkinnonosa/' + response.id);
                });
              }
              $scope.tutkinnonOsa = angular.copy($scope.editableTutkinnonOsa);
            },
            cancel: function() {
              console.log('tutkinnon osa - cancel');
              $scope.editableTutkinnonOsa = angular.copy($scope.tutkinnonOsa);
            }
          });
        }
        
        if($scope.tutkinnonOsa) {
          $scope.tutkinnonOsa.$promise.then(function(response) {
            setupTutkinnonOsa(response);
          });
        } else {
          $scope.tutkinnonOsa = {
            nimi: {
              fi: '',
              sv: ''
            },
            tavoitteet: {
              fi: '',
              sv: ''
            },
            arviointi: null,
            ammattitaitovaatimukset: {
              fi: '',
              sv: ''
            },
            ammattitaidonOsoittamistavat: {
              fi: '',
              sv: ''
            },
            opintoluokitus: null,
            koodi: null,
            osaamisala: {
              fi: '',
              sv: ''
            }
          };
          setupTutkinnonOsa($scope.tutkinnonOsa);
        }
      }
    };
  });