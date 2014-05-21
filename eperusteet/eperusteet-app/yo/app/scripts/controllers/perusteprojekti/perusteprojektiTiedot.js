'use strict';

angular.module('eperusteApp')
  .controller('PerusteprojektiTiedotCtrl', function($scope, $state, $stateParams,
    PerusteprojektiResource, PerusteProjektiService, Navigaatiopolku) {
    PerusteProjektiService.watcher($scope, 'projekti');

    $scope.projekti = {};
    PerusteProjektiService.clean();
    // NOTE: Jos ei löydy suoritustapaa serviceltä niin käytetään suoritustapaa 'naytto'.
    //       Tämä toimii ammatillisen puolen projekteissa, mutta ei yleissivistävän puolella.
    //       Korjataan kun keksitään parempi suoritustavan valinta-algoritmi.
    $scope.suoritustapa = PerusteProjektiService.getSuoritustapa() || 'naytto';

    $scope.projekti.id = $stateParams.perusteProjektiId;

    $scope.tabs = [{otsikko: 'projekti-perustiedot', url: 'views/partials/perusteprojekti/perusteprojektiPerustiedot.html'},
                   {otsikko: 'projekti-toimikausi', url: 'views/partials/perusteprojekti/perusteprojektiToimikausi.html'}];

    if (angular.isDefined($stateParams.perusteProjektiId)) {
      $scope.projekti.id = $stateParams.perusteProjektiId;
      PerusteprojektiResource.get({ id: $stateParams.perusteProjektiId }, function(vastaus) {
        $scope.projekti = vastaus;
        Navigaatiopolku.asetaElementit({ perusteProjektiId: vastaus.nimi });
      });
    }

    $scope.tallennaPerusteprojekti = function() {
      var projekti = PerusteProjektiService.get();
      console.log('projekti.id', projekti.id);
      if (angular.isDefined(projekti.id)) {
        // Poista tämä hackkin, kun keksitty parempi tapa viedä koulutustyyppi uuden projektin luonnissa.
        // Uuden projektin luonti dto:ssa kulkee koulutustyyppi, mutta ei normaalissa perusteprojektiDto:ssa
        delete projekti.koulutustyyppi;
        PerusteprojektiResource.update(projekti, function(vastaus) {
          PerusteProjektiService.save(vastaus);
          PerusteProjektiService.update();
          avaaProjektinSisalto(vastaus.id);
        }, function (virhe) {
          console.log('projektin tallennusvirhe', virhe);
        });
      } else {
        projekti.id = null;
        PerusteprojektiResource.save(projekti, function(vastaus) {
          PerusteProjektiService.save(vastaus);
          PerusteProjektiService.update();
          avaaProjektinSisalto(vastaus.id);
        }, function(virhe) {
          console.log('projektin luontivirhe', virhe);
        });
      }
    };

    var avaaProjektinSisalto = function(projektiId) {
      $state.go('perusteprojekti.suoritustapa.sisalto', {perusteProjektiId: projektiId, suoritustapa: $scope.suoritustapa}, {reload:true});
    };

  });
