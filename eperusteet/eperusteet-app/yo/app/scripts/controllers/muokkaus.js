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

    $scope.comments = [];

    $scope.topics = PerusteenOsat.query(function() {
      $scope.thing = $scope.topics[0];
      $scope.trustedThing = $sce.trustAsHtml($scope.thing.teksti.fi);
    });

    $scope.selectTopic = function(topic) {
      console.log(topic);
      $scope.thing = topic;
      $scope.thing.$get().then(function() {
        $scope.trustedThing = $sce.trustAsHtml($scope.thing.teksti.fi);
      });
    };

    $scope.deleteTopic = function() {
      $scope.thing.$delete(function() {
        PerusteenOsat.query(function(result) {
          $scope.topics = result;
          $scope.thing = $scope.topics[0];
          $scope.trustedThing = $sce.trustAsHtml($scope.thing.teksti.fi);
        });
      });
    };

    $scope.addTopic = function() {
      PerusteenOsat.save({
        tyyppi : 'tekstiosa',
        otsikko: {fi : $scope.topicText },
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
      $scope.thing.$save();
    });
  });
