'use strict';

var muokkausUtils = muokkausUtils || {};

muokkausUtils = {
  setTutkinnonOsaForModification: function(scope, tutkinnonOsa, sce) {
    scope.tutkinnonOsa = tutkinnonOsa;
    if(!scope.tutkinnonOsa.nimi) {
      scope.tutkinnonOsa.nimi = {fi: '&nbsp;'};
    } else if(!scope.tutkinnonOsa.nimi.fi) {
      scope.tutkinnonOsa.nimi.fi = '&nbsp;';
    }
    if(!scope.tutkinnonOsa.tavoitteet) {
      scope.tutkinnonOsa.tavoitteet = {fi: '&nbsp;'};
    } else if(!scope.tutkinnonOsa.tavoitteet.fi) {
      scope.tutkinnonOsa.tavoitteet.fi = '&nbsp;';
    }
    if(!scope.tutkinnonOsa.ammattitaitovaatimukset) {
      scope.tutkinnonOsa.ammattitaitovaatimukset = {fi: '&nbsp;'};
    } else if(!scope.tutkinnonOsa.ammattitaitovaatimukset.fi) {
      scope.tutkinnonOsa.ammattitaitovaatimukset.fi = '&nbsp;';
    }
    scope.trustedTutkinnonOsanTavoitteet = sce.trustAsHtml(scope.tutkinnonOsa.tavoitteet.fi);
    scope.trustedTutkinnonOsanAmmattitaitovaatimukset = sce.trustAsHtml(scope.tutkinnonOsa.ammattitaitovaatimukset.fi);
    
    muokkausUtils.updateArvioinnit(scope);
  },
  fetchAllTopics: function(scope, service, sce) {
    var allPerusteenOsat = [];
    scope.topics = [];
    allPerusteenOsat = service.query(function() {
      angular.forEach(allPerusteenOsat, function(osa) {
        if(osa.nimi && osa.teksti) {
          scope.topics.push(osa);
          if(scope.topics.length < 2) {
            scope.thing = osa;
            scope.trustedThing = sce.trustAsHtml(scope.thing.teksti.fi);
          }
        }
      });
    });
  },
  fetchAllTutkinnonOsat: function(scope, service, sce) {
    var allPerusteenOsat = [];
    scope.tutkinnonOsat = [];
    allPerusteenOsat = service.query(function() {
      angular.forEach(allPerusteenOsat, function(osa) {
        if(osa.tavoitteet) {
          scope.tutkinnonOsat.push(osa);
          if(scope.tutkinnonOsat.length < 2) {
            muokkausUtils.setTutkinnonOsaForModification(scope, osa, sce);
          }
        }
      });
    });
  },
  updateArvioinnit: function(scope) {
    scope.arvioinnit = [];
    
    angular.forEach(scope.kaikkiArvioinnit, function(arviointi) {
      if(scope.tutkinnonOsa.arviointi == null || arviointi.id !== scope.tutkinnonOsa.arviointi.id) {
        scope.arvioinnit.push(arviointi);
      }
    });
  }
};

angular.module('eperusteApp')
  .config(function($routeProvider) {
    $routeProvider
      .when('/muokkaus', {
        templateUrl: 'views/muokkaus.html',
        controller: 'MuokkausCtrl',
        navigaationimi: 'Muokkaus'
      });
  })
  .controller('MuokkausCtrl', function($scope, $sce, PerusteenOsat, Arviointi) {
    
    $scope.osienTyypit = ['perusteen-osat-tekstikappale', 'perusteen-osat-tutkinnon-osa'];
    
    $scope.kaikkiArvioinnit = Arviointi.query();
    
    $scope.comments = [];
    
    $scope.valittuTyyppi = '';

    $scope.haeTekstikappaleet = function() {
      muokkausUtils.fetchAllTopics($scope, PerusteenOsat, $sce);
    };
    $scope.haeTutkinnonOsat = function() {
      muokkausUtils.fetchAllTutkinnonOsat($scope, PerusteenOsat, $sce);
    };
    
    $scope.selectTopic = function(topic) {
      console.log(topic);
      $scope.thing = topic;
      $scope.thing.$get().then(function() {
        $scope.trustedThing = $sce.trustAsHtml($scope.thing.teksti.fi);
      });
    };
    
    $scope.selectTutkinnonOsa = function(currentTutkinnonOsa) {
      muokkausUtils.setTutkinnonOsaForModification($scope, currentTutkinnonOsa, $sce);
    };

    $scope.deleteTopic = function() {
      $scope.thing.$delete(function() {
        muokkausUtils.fetchAllTopics($scope, PerusteenOsat, $sce);
      });
    };
    
    $scope.deleteTutkinnonOsa = function() {
      $scope.tutkinnonOsa.$delete(function() {
        muokkausUtils.fetchAllTutkinnonOsat($scope, PerusteenOsat, $sce);
      });
    };

    $scope.addTopic = function() {
      PerusteenOsat.saveTekstikappale({
        nimi: {fi : $scope.topicText },
        teksti : {fi : '<p>Uusi kappale</p>' }
      }, function(t) {
        $scope.topics.push(t);
      });
      $scope.topicText = '';
    };
    
    $scope.addTutkinnonOsa = function() {
      PerusteenOsat.saveTutkinnonOsa({
        nimi: {fi: $scope.tutkinnonOsanNimi},
        tavoitteet: {fi: '<p>Tavoitteet</p>'},
        opintoluokitus: $scope.tutkinnonOsanOpintoluokitus
      });
      $scope.tutkinnonOsanNimi = '';
      $scope.tutkinnonOsanOpintoluokitus = '';
    };

    $scope.addComment = function() {
      $scope.comments.push($scope.commentText);
      $scope.commentText = '';
    };
    
    $scope.addArviointi = function(arviointi) {
      console.log('Lisätään arviointi #' + arviointi.id);
      
      $scope.tutkinnonOsa.arviointi = arviointi;
      $scope.tutkinnonOsa.$saveTutkinnonOsa();
      
      muokkausUtils.updateArvioinnit($scope);
    };

    $scope.$on('edited', function() {
      console.log('EDITED!');
      if($scope.valittuTyyppi === 'perusteen-osat-tekstikappale') {
        $scope.thing.$saveTekstikappale();
      }
      if($scope.valittuTyyppi === 'perusteen-osat-tutkinnon-osa') {
        console.log($scope.tutkinnonOsa);
        
        if($scope.tutkinnonOsa.nimi.fi === '&nbsp;') {
          $scope.tutkinnonOsa.nimi.fi = '';
        }
        if($scope.tutkinnonOsa.tavoitteet.fi === '&nbsp;') {
          $scope.tutkinnonOsa.tavoitteet.fi = '';
        }
        if($scope.tutkinnonOsa.ammattitaitovaatimukset.fi === '&nbsp;') {
          $scope.tutkinnonOsa.ammattitaitovaatimukset.fi = '';
        }
        $scope.tutkinnonOsa.$saveTutkinnonOsa();
      }
    });
  });