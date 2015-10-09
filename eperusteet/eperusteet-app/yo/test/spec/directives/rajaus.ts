'use strict';

describe('Directive: rajaus', function () {

  // load the directive's module
  beforeEach(module('eperusteApp',
                    'views/aloitussivu.html',
                    'views/partials/rajaus.html'
                    ));

  var element, scope, $compile, $http;


  beforeEach(inject(function ($rootScope, _$compile_, $httpBackend) {
    $http = $httpBackend;
    $httpBackend.when('GET', /localisation.+/).respond({});
    $httpBackend.when('GET', /eperusteet-service\/api.+/).respond({});
    scope = $rootScope.$new();
    $compile = _$compile_;
  }));

  function kaannaElementti(el) {
    element = angular.element(el);
    $compile(element)(scope);
    scope.$digest();
  };

  it('accepts placeholder as string', function () {
    scope.model = {};
    kaannaElementti('<rajaus model="model" placeholder="TESTERSTRING"></rajaus>');
    expect(element.find('input').attr('placeholder')).toBe('TESTERSTRING');
  });

  it('accepts placeholder as model', function () {
    scope.model = {};
    scope.search = {
      placeholder: 'MODELSTRING'
    };
    kaannaElementti('<rajaus model="model" placeholder="{{search.placeholder}}"></rajaus>');
    expect(element.find('input').attr('placeholder')).toBe('MODELSTRING');
  });
});
