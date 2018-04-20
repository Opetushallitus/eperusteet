'use strict';
import _ from "lodash";

describe('Directive: numberinput', function () {

  // load the directive's module
  beforeEach(module('eperusteApp',
                    'views/partials/numberinput.html'));

  var element, scope, $compile, $timeout;

  beforeEach(inject(function ($rootScope, _$compile_, $httpBackend, _$timeout_) {
    $httpBackend.when('GET', /localisation.+/).respond({});
    $httpBackend.when('GET', /eperusteet-service\/api.+/).respond({});
    $httpBackend.when('GET', /views\/aloitussivu.+/).respond({});
    $httpBackend.when('GET', /cas\/me/).respond({});
    scope = $rootScope.$new();
    $compile = _$compile_;
    $timeout = _$timeout_;
  }));

  function kaanna(el) {
    element = angular.element(el);
    element = $compile(element)(scope);
    scope.$digest();
  }

  describe('without type', function () {
    var input;
    beforeEach(function () {
      scope.data = {number: 1};
      var html = '<form name="myform"><numberinput name="numero" model="data.number" form="myform"></numberinput></form>';
      kaanna(html);
      input = element.find('input');
    });

    it('should render itself as input type=number', function () {
      expect(input.attr('type')).toBe('number');
    });

    it('should accept integers', function () {
      scope.data.number = 5;
      scope.$digest();
      expect(scope.myform.$valid).toBeTruthy();
    });

    it('should not accept text', function () {
      scope.data.number = 'asdf';
      expect(function () {
        scope.$digest();
      }).toThrow();
    });

    it('should accept floats', function () {
      scope.data.number = 123.45;
      scope.$digest();
      expect(scope.myform.$valid).toBeTruthy();
    });
  });

  describe('with type integer', function () {
    var input;
    beforeEach(function () {
      scope.data = {number: 1};
      var html = '<form name="myform"><numberinput type="integer" name="numero" model="data.number" form="myform"></numberinput></form>';
      kaanna(html);
      input = element.find('input');
    });

    it('should render itself as input type=number', function () {
      expect(input.attr('type')).toBe('number');
    });

    it('should accept integers', function () {
      scope.data.number = 5;
      scope.$digest();
      expect(scope.myform.$valid).toBeTruthy();
    });

    /*
    it('should not accept text', function () {
      scope.data.number = 'asdf';
      scope.$digest();
      expect(scope.myform.$error.number).toBeTruthy();
    });
    */
    it('should not accept floats', function () {
      scope.data.number = 123.45;
      scope.$digest();
      expect(scope.myform.$error.integer).toBeTruthy();
    });
  });

  describe('with type float', function () {
    var input;
    beforeEach(function () {
      scope.data = {number: 1};
      var html = '<form name="myform">' +
          '<numberinput type="float" step="0.2" name="numero" model="data.number" form="myform" min="0" max="200">' +
          '</numberinput></form>';
      kaanna(html);
      input = element.find('input');
    });

    it('should accept integers', function () {
      scope.data.number = 5;
      scope.$digest();
      expect(scope.myform.$valid).toBeTruthy();
    });

    it('should not accept text', function () {
      scope.data.number = 'asdf';
      scope.$digest();
      expect(scope.myform.$error.float).toBeTruthy();
    });

    it('should accept floats on steps', function () {
      scope.data.number = 100.4;
      scope.$digest();
      expect(scope.myform.$valid).toBeTruthy();
    });

    it('should not accept floats between steps', function () {
      $timeout.flush();
      scope.data.number = 123.3;
      scope.$digest();
      expect(scope.myform.$error.step).toBeTruthy();
    });

    it('should respect min/max limits', function () {
      $timeout.flush();
      var field = scope.myform.innerForm.tmpName;
      field.$setViewValue('-1');
      expect(scope.myform.$error.min).toBeTruthy();
      field.$setViewValue('201');
      expect(scope.myform.$error.max).toBeTruthy();
      field.$setViewValue('199');
      expect(scope.myform.$error.min).toBeFalsy();
      expect(scope.myform.$error.max).toBeFalsy();
    });
  });
});
