'use strict';

angular.module('eperusteApp')
  .controller('ProjektinTiedotCtrl', function($scope, $state, $stateParams,
    PerusteprojektiResource, PerusteProjektiService, Navigaatiopolku, perusteprojektiTiedot) {
    PerusteProjektiService.watcher($scope, 'projekti');

    PerusteProjektiService.clean();
    if ($state.current.name === 'perusteprojektiwizard.tiedot') {
      perusteprojektiTiedot.cleanData();
    }
    
    $scope.projekti = perusteprojektiTiedot.getProjekti();
    Navigaatiopolku.asetaElementit({ perusteProjektiId: $scope.projekti.nimi });
    

    $scope.tabs = [{otsikko: 'projekti-perustiedot', url: 'views/partials/perusteprojekti/perusteprojektiPerustiedot.html'},
                   {otsikko: 'projekti-toimikausi', url: 'views/partials/perusteprojekti/perusteprojektiToimikausi.html'}];

    $scope.tallennaPerusteprojekti = function() {
      var projekti = PerusteProjektiService.get();
      if (angular.isDefined(projekti.id)) {
        // Poista tämä hack:ki, kun keksitty parempi tapa viedä koulutustyyppi uuden projektin luonnissa.
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
      $state.go('perusteprojekti.suoritustapa.sisalto', {perusteProjektiId: projektiId, suoritustapa: PerusteProjektiService.getSuoritustapa()}, {reload:true});
    };

  });
