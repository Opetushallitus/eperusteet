'use strict';

describe('Controller: PerusteprojektiCtrl', function () {

  // load the controller's module
  beforeEach(module('eperusteApp'));

  var PerusteprojektiCtrl,
    scope;

  // Initialize the controller and a mock scope
  beforeEach(inject(function ($controller, $rootScope) {
    var mockKoulutusalaService = {
      query: function() {
        queryDeferred = $q.defer();
        return {$promise: queryDeferred.promise};
      }
    };
    spyOn(mockKoulutusalaService, 'query').andCallThrough();

    var mockOpintoalaService = {
      query: function() {
        queryDeferred = $q.defer();
        return {$promise: queryDeferred.promise};
      }
    };
    spyOn(mockOpintoalaService, 'query').andCallThrough();

    var mockPerusteprojektiTiedot = {
      query: function() {
        queryDeferred = $q.defer();
        return {$promise: queryDeferred.promise};
      },
      getProjekti: function () {return {};},
      getPeruste: function () {return {};},
      getSisalto: function () {return {};}
    };
    spyOn(mockPerusteprojektiTiedot, 'query').andCallThrough();

    var mockPerusteprojektiOikeudet = {
      noudaOikeudet: function() {
        queryDeferred = $q.defer();
        return {$promise: queryDeferred.promise};
      },
      getOikeudet: function () {return {};},
      onkoOikeudet: function () {return true;},
    };
    spyOn(mockPerusteprojektiOikeudet, 'noudaOikeudet').andCallThrough();

    scope = $rootScope.$new();
    PerusteprojektiCtrl = $controller('PerusteprojektiCtrl', {
      $scope: scope,
      koulutusalaService: mockKoulutusalaService,
      opintoalaService: mockOpintoalaService,
      perusteprojektiTiedot: mockPerusteprojektiTiedot,
      perusteprojektiOikeudet: mockPerusteprojektiOikeudet
    });
  }));

  it('should work', function () {
    //TODO
  });
});
