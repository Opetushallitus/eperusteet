'use strict';

/// <reference path="../../ts_packages/tsd.d.ts" />

declare var beforeEach: any;
declare var describe: any;
declare var expect: any;
declare var inject: any;
declare var it: any;
declare var module: any; // FIXME: Tämä ei ole varmaan viisasta
declare var queryDeferred: any;
declare var spyOn: any;

describe('Controller: EsitysCtrl', function() {

  // load the controller's module
  beforeEach(module('eperusteApp'));

  var EsitysCtrl,
    scope;

  // Initialize the controller and a mock scope
  beforeEach(inject(function($controller, $rootScope) {
    scope = $rootScope.$new();
    EsitysCtrl = $controller('EsitysCtrl', {
      $scope: scope
    });
  }));

  /*it('should attach a list of awesomeThings to the scope', function () {
   expect(scope.awesomeThings.length).toBe(3);
   });*/
});
