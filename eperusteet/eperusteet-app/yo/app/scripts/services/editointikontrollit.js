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
/* global _ */

/**
 * Editointikontrollit
 * required callbacks:
 * - edit Called when starting to edit
 * - save Called when saving
 * - cancel Called when canceling edit mode
 * optional callbacks:
 * - notify Called when edit mode changes with boolean parameter editMode
 * - validate Called before saving, returning true results in save
 * - asyncValidate Called before save/validate, parameter is a function that
 *                 should be called on successful validation (results in save)
 * - canCancel Called before cancel, must return a promise.
 *             If promise is resolved, canceling continues.
 */
angular.module('eperusteApp')
  .factory('Editointikontrollit', function($rootScope, $q, Utils, Notifikaatiot) {
    var scope = $rootScope.$new(true);
    scope.editingCallback = null;
    scope.editMode = false;

    scope.editModeDefer = $q.defer();

    this.lastModified = null;
    var cbListener = null;
    var editmodeListener = null;

    function setEditMode(mode) {
      scope.editMode = mode;
      scope.editModeDefer = $q.defer();
      scope.editModeDefer.resolve(scope.editMode);
      if (scope.editingCallback) {
        scope.editingCallback.notify(mode);
      }
      if (editmodeListener) {
        editmodeListener(mode);
      }
    }

    function createEntry(name, argamount, doneFn) {
      return function() {
        if (scope.editingCallback) {
          var len = scope.editingCallback[name].length;
          if (len === argamount) {
            console.error('You should use the async version with "' + name + '" callback');
            doneFn(scope.editingCallback[name]());
          }
          else if (len === argamount + 1) {
            scope.editingCallback[name](doneFn);
          }
          else {
            console.error('Using editing callback "' + name + '" wrong with', len, 'arguments when it should be', argamount, 'or', argamount + 1);
          }
        }
        else {
          console.error('No callbacks registered');
        }
      };
    }

    return {
      startEditing: createEntry('edit', 0, function() {
        setEditMode(true);
        $rootScope.$broadcast('enableEditing');
      }),
      saveEditing: function(kommentti) {
        $rootScope.$broadcast('editointikontrollit:preSave');
        var err;

        function mandatoryFieldValidator(fields, target) {
          err = undefined;
          var fieldsf = _.filter(fields || [], function(field) { return field.mandatory; });

          if (!target) { return false; }
          else if (_.isString(target)) { return !_.isEmpty(target); }
          else if (_.isObject(target) && !_.isEmpty(target) && !_.isEmpty(fieldsf)) {
            return _.all(fieldsf, function(field) {
              var valid = Utils.hasLocalizedText(target[field.path]);
              if (!valid) { err = field.mandatoryMessage; }
              return valid;
            });
          }
          else { return true; }
        }

        function after() {
          function done() {
            setEditMode(false);
            $rootScope.$broadcast('disableEditing');
          }

          if (!scope.editingCallback.validate || scope.editingCallback.validate(mandatoryFieldValidator)) {
            if (scope.editingCallback.save.length === 2) {
              scope.editingCallback.save(kommentti, done);
            }
            else {
              scope.editingCallback.save(kommentti);
              done();
            }
          }
          else {
            Notifikaatiot.varoitus(err || 'mandatory-odottamaton-virhe');
          }
        }

        if (scope.editingCallback) {
          if (_.isFunction(scope.editingCallback.asyncValidate)) {
            scope.editingCallback.asyncValidate(after);
          }
          else {
            after();
          }
        }

        $rootScope.$broadcast('notifyCKEditor');
      },
      cancelEditing: createEntry('cancel', 0, function(isOk) {
        if (isOk !== false) {
          setEditMode(false);
          $rootScope.$broadcast('disableEditing');
          $rootScope.$broadcast('notifyCKEditor');
        }
      }),
      registerCallback: function(callbacks) {
        if (!callbacks ||
            !angular.isFunction(callbacks.edit) ||
            !angular.isFunction(callbacks.save) ||
            !angular.isFunction(callbacks.cancel)) {
          console.error('callback-function invalid');
          throw 'editCallback-function invalid';
        }

        if (!angular.isFunction(callbacks.notify)) {
          callbacks.notify = angular.noop;
        }

        editmodeListener = null;
        scope.editingCallback = callbacks;
        scope.editModeDefer.resolve(scope.editMode);
        (cbListener || _.noop)();
      },
      unregisterCallback: function() {
        scope.editingCallback = null;
        setEditMode(false);
      },
      editingEnabled: function() {
        return !!scope.editingCallback;
      },
      registerCallbackListener: function(callbackListener) {
        cbListener = callbackListener;
      },
      registerEditModeListener: function (listener) {
        editmodeListener = listener;
      },
      getEditModePromise: function() {
        return scope.editModeDefer.promise;
      },
      getEditMode: function() {
        return scope.editMode;
      }
    };
});
