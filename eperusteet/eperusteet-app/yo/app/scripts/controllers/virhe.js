'use strict';

angular.module('eperusteApp')
  .config(function($stateProvider) {
    $stateProvider
      .state('virhe', {
        url: '/virhe',
        templateUrl: '/views/virhe.html',
        controller: 'virheCtrl'
      });
  })
  .controller('virheCtrl', function ($scope, virheService) {
    $scope.$watch(virheService.getData, function (value) {
      $scope.data = value;
    });
  })
  .service('virheService', function () {
    this.data = {};
    var that = this;
    this.setData = function (data) {
      that.data = data;
    };
    this.getData = function () {
      return that.data;
    };
  });