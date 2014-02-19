'use strict';

angular.module('eperusteApp')
  .config(function($routeProvider) {
    $routeProvider
      .when('/muokkaus', {
        templateUrl: 'views/muokkaus.html',
        controller: 'MuokkausCtrl',
        navigaationimi: 'Muokkaus'
      });
  })
  .controller('MuokkausCtrl', function($scope, $sce, PerusteenOsat) {

    var fetchAllTopics = function(scope, service, sce) {
      var allPerusteenOsat = [];
      scope.topics = [];
      allPerusteenOsat = service.query(function() {
        angular.forEach(allPerusteenOsat, function(osa) {
          if(osa.nimi && osa.teksti) {
            scope.topics.push(osa);
            if(scope.topics.length < 2) {
              scope.thing = osa;
              scope.trustedThing = sce.trustAsHtml($scope.thing.teksti.fi);
            }
          }
        }, scope.topics);
      });
    };

    $scope.laatikkoOnKiinni = true;
    
    $scope.comments = [];

    fetchAllTopics($scope, PerusteenOsat, $sce);
    
    $scope.selectTopic = function(topic) {
      console.log(topic);
      $scope.thing = topic;
      $scope.thing.$get().then(function() {
        $scope.trustedThing = $sce.trustAsHtml($scope.thing.teksti.fi);
      });
    };

    $scope.deleteTopic = function() {
      $scope.thing.$delete(function() {
        fetchAllTopics($scope, PerusteenOsat, $sce);
      });
    };

    $scope.addTopic = function() {
      PerusteenOsat.save({
        tyyppi: "perusteen-osat-tekstikappale"
      },
      {
        nimi: {fi : $scope.topicText },
        teksti : {fi : '<p>Uusi kappale</p>' }
      }, function(t) {
        $scope.topics.push(t);
      });
      $scope.topicText = '';
    };

    $scope.addComment = function() {
      $scope.comments.push($scope.commentText);
      $scope.commentText = '';
    };

    $scope.$on('edited', function() {
      console.log('EDITED!');
      $scope.thing.$save({tyyppi: "perusteen-osat-tekstikappale"});
    });
  });