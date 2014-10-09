'use strict';

describe('Directive: ohje', function () {

  // load the directive's module
  beforeEach(module('eperusteApp',
                    'views/aloitussivu.html',
                    'views/partials/ohje.html'
                    ));

  var element, scope, $compile, $http, $timeout;


  beforeEach(inject(function ($rootScope, _$compile_, $httpBackend, _$timeout_) {
    $http = $httpBackend;
    $httpBackend.when('GET', /localisation.+/).respond({});
    $httpBackend.when('GET', /eperusteet-service\/api.+/).respond({});
    scope = $rootScope.$new();
    $compile = _$compile_;
    $timeout = _$timeout_;
  }));

  function kaannaElementti(el) {
    element = angular.element(el);
    $compile(element)(scope);
    scope.$digest();
  }

  it('constructs a badge for mouseover', function () {
    kaannaElementti('<ohje></ohje>');
    expect(element.find('.badge').length).toBe(1);
    expect(element.text()).toMatch(/\s*\?\s*/);
  });

  it('it wraps an existing element with custom title', function () {
    scope.model = {
      otsikko: {fi: 'My title'}
    };
    kaannaElementti('<span ohje="false" otsikko="{{model.otsikko}}"><p class="my-text">Text</p></span>');
    expect(element.find('.popover-element').attr('popover-title')).toBe('My title');
    expect(element.find('.my-text').length).toBe(1);
    expect(element.find('.badge').length).toBe(0);
  });

  it('allows custom appended content', function () {
    scope.model = {
      teksti: {fi: 'Joku teksti'},
      extra: '<div ng-repeat="num in [1,2]" class="extra-item">{{num}}</div>'
    };
    kaannaElementti('<ohje teksti="model.teksti" extra="model.extra"></ohje>');
    element.isolateScope().show();
    $timeout.flush();
    expect(element.find('.extra-item').length).toBe(2);
    expect(element.find('.popover-content').text()).toMatch(/Joku teksti/);
  });

});
