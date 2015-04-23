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

 angular.module('eperusteet.esitys')
.controller('epEsitysSisaltoController', function($scope, $state, $stateParams, PerusteenOsat, YleinenData,
  MurupolkuData, epParentFinder, epTekstikappaleChildResolver) {
  $scope.linkVar = $stateParams.osanId ? 'osanId' : 'tekstikappaleId';
  //$scope.$parent.valittu.sisalto = $stateParams[$scope.linkVar];
  $scope.valittuSisalto = $scope.$parent.sisalto[$stateParams[$scope.linkVar]];
  $scope.tekstikappale = $scope.valittuSisalto;
  $scope.lapset = epTekstikappaleChildResolver.getSisalto();
  var parentNode = $scope.$parent.originalSisalto ? $scope.$parent.originalSisalto : $scope.tekstisisalto;
  MurupolkuData.set({
    osanId: $scope.valittuSisalto.id,
    tekstikappaleNimi: $scope.valittuSisalto.nimi,
    parents: epParentFinder.find(parentNode ? parentNode.lapset : null, parseInt($stateParams[$scope.linkVar], 10))
  });
  if (!$scope.valittuSisalto) {
    var params = _.extend(_.clone($stateParams), {
      suoritustapa: YleinenData.validSuoritustapa($scope.peruste, $stateParams.suoritustapa)
    });
    $state.go('root.esitys.peruste.tiedot', params);
  } else {
    PerusteenOsat.get({ osanId: $scope.valittuSisalto.id }, function (res) {
      $scope.valittuSisalto = res;
      $scope.tekstikappale = res;
    });
  }
})

.controller('epEsitysTiedotController', function($scope, $q, $state, YleinenData, PerusteenTutkintonimikkeet, Perusteet) {
  $scope.showKoulutukset = _.constant(YleinenData.showKoulutukset($scope.peruste));
  $scope.koulutusalaNimi = $scope.Koulutusalat.haeKoulutusalaNimi;
  $scope.opintoalaNimi = $scope.Opintoalat.haeOpintoalaNimi;

  PerusteenTutkintonimikkeet.get($scope.peruste.id, $scope);

  $scope.korvattavatPerusteet = {};
  _.each($scope.peruste.korvattavatDiaarinumerot, function (diaari) {
    $scope.korvattavatPerusteet[diaari] = {diaarinumero: diaari};
    Perusteet.diaari({diaarinumero: diaari}, function (res) {
      $scope.korvattavatPerusteet[diaari] = res;
    });
  });
})

.service('JarjestysService', function () {
  this.options = [
    {value: 'jarjestys', label: 'tutkinnonosa-jarjestysnumero'},
    {value: 'nimi', label: 'nimi'}
  ];
  this.selection = {};
})

.controller('epEsitysTutkinnonOsaController', function($scope, $state, $stateParams, PerusteenOsat, TutkinnonosanTiedotService,
    Tutke2Osa, Kieli, MurupolkuData) {
  $scope.tutkinnonOsaViite = _.find($scope.$parent.tutkinnonOsat, function(tosa) {
    return tosa.id === parseInt($stateParams.id, 10);
  });
  MurupolkuData.set({id: $scope.tutkinnonOsaViite.id, tutkinnonosaNimi: $scope.tutkinnonOsaViite.nimi});
  $scope.osaAlueet = {};
  TutkinnonosanTiedotService.noudaTutkinnonOsa({perusteenOsaId: $scope.tutkinnonOsaViite._tutkinnonOsa}).then(function () {
    $scope.tutkinnonOsa = TutkinnonosanTiedotService.getTutkinnonOsa();
    $scope.fieldKeys = _.intersection(_.keys($scope.tutkinnonOsa), TutkinnonosanTiedotService.keys());
    if ($scope.tutkinnonOsa.tyyppi === 'tutke2') {
      Tutke2Osa.kasitteleOsaAlueet($scope.tutkinnonOsa);
    }
  });

  $scope.fieldOrder = function (item) {
    return TutkinnonosanTiedotService.order(item);
  };
  $scope.hasArviointi = function (osaamistavoite) {
    return osaamistavoite.arviointi &&
      osaamistavoite.arviointi.arvioinninKohdealueet &&
      osaamistavoite.arviointi.arvioinninKohdealueet.length > 0 &&
      osaamistavoite.arviointi.arvioinninKohdealueet[0].arvioinninKohteet &&
      osaamistavoite.arviointi.arvioinninKohdealueet[0].arvioinninKohteet.length > 0;
  };
  $scope.osaAlueFilter = function (item) {
    return _.contains(item.$kielet, Kieli.getSisaltokieli());
  };
})

.controller('epEsitysTutkinnonOsatController', function($scope, $state, $stateParams, Algoritmit, JarjestysService, Kaanna) {
  $scope.jarjestysOrder = _.isBoolean(JarjestysService.selection.order) ? JarjestysService.selection.order : false;
  $scope.jarjestysOptions = JarjestysService.options;
  $scope.jarjestysTapa = JarjestysService.selection.value || _.first($scope.jarjestysOptions).value;

  $scope.jarjestysFn = function(data) {
    switch($scope.jarjestysTapa) {
      case 'jarjestys': return data.jarjestys;
      default: return Kaanna.kaanna(data.nimi);
    }
  };

  $scope.$watch('jarjestysOrder', function (value) {
    if (_.isBoolean(value)) {
      JarjestysService.selection.order = value;
    }
  });

  $scope.$watch('jarjestysTapa', function (value) {
    if (value) {
      JarjestysService.selection.value = value;
    }
  });

  $scope.$parent.valittu.sisalto = 'tutkinnonosat';
  $scope.tosarajaus = '';
  $scope.rajaaTutkinnonOsia = function(haku) { return Algoritmit.rajausVertailu($scope.tosarajaus, haku, 'nimi'); };
})

.controller('epEsitysRakenneController', function($scope, $state, $stateParams, PerusteenRakenne, realParams) {
  $scope.$parent.valittu.sisalto = 'rakenne';
  $scope.muodostumisOtsikko = _.find($scope.$parent.sisalto, function (item) {
    return item.tunniste === 'rakenne';
  });
  PerusteenRakenne.hae(realParams.perusteId, realParams.suoritustapa, function(rakenne) {
    $scope.rakenne = rakenne;
    $scope.rakenne.$suoritustapa = realParams.suoritustapa;
    $scope.rakenne.$resolved = true;
  });
});
