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

/**
 * Statusbadge:
 * <statusbadge status="luonnos|..." editable="true|false"></statusbadge>
 * Tyylit eri statuksille määritellään "statusbadge" sass-moduulissa.
 * Sama avainsana pitää olla käytössä tyyleissä ja lokalisoinnissa.
 */
angular.module('eperusteApp')
  .directive('statusbadge', function () {
    var OFFSET = 4;
    return {
      templateUrl: 'views/partials/statusbadge.html',
      restrict: 'EA',
      replace: true,
      scope: {
        status: '=',
        editable: '=?',
        projektiId: '='
      },
      controller: 'StatusbadgeCtrl',
      link: function (scope, element) {
        // To fit long status names into the badge, adjust letter spacing
        var el = element.find('.status-name');

        function adjust() {
          if (scope.status.length > 8) {
            var spacing = 1 - ((scope.status.length - OFFSET) * 0.2);
            el.css('letter-spacing', spacing + 'px');
          }
        }
        scope.$watch('status', adjust);
        adjust();
      }
    };
  })

  .controller('StatusbadgeCtrl', function ($scope, PerusteprojektinTilanvaihto,
    PerusteprojektiTila, Notifikaatiot, $http, SERVICE_LOC, $modal) {
    $scope.iconMapping = {
      laadinta: 'pencil',
      kommentointi: 'comment',
      viimeistely: 'certificate',
      kaannos: 'book',
      valmis: 'thumbs-up',
      julkaistu: 'glass'
    };

    $scope.appliedClasses = function () {
      var classes = {editable: $scope.editable};
      classes[$scope.status] = true;
      return classes;
    };

    $scope.iconClasses = function () {
      return 'glyphicon glyphicon-' + $scope.iconMapping[$scope.status];
    };

    $scope.startEditing = function() {
      $http.get(SERVICE_LOC + '/perusteprojektit/' + $scope.projektiId + '/tilat').then(function(vastaus) {

        if (vastaus.data.length !== 0) {
          PerusteprojektinTilanvaihto.start($scope.status, vastaus.data, function(newStatus) {
            // TODO tilan tallennus, tämä asettaa uuden tilan parent scopen projektiobjektiin.
            PerusteprojektiTila.save({id: $scope.projektiId, tila: newStatus}, {}, function(vastaus) {
              if (vastaus.vaihtoOk) {
                $scope.status = newStatus;
              } else {
                $modal.open({
                  templateUrl: 'views/modals/tilanVaihtoVirhe.html',
                  controller: 'TilanvaihtovirheCtrl',
                  size: 'lg',
                  resolve: {infot: function() {
                      return vastaus.infot;
                    }}
                }).result.then(function() {
                });
              }
          }, function(virhe) {
            console.log('tilan vaihto virhe', virhe);
            Notifikaatiot.serverCb(virhe);
          });

        });
      }
      }, function(err) {
        // TODO: serverCb ei toimi hyvin yhteen $http virheiden kanssa. Korjaa.
        Notifikaatiot.serverCb(err);
      });

    };
  });
