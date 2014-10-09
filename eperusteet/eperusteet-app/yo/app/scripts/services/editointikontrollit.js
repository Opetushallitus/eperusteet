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
  .factory('Editointikontrollit', function($rootScope, $q, $timeout) {
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

    return {
      startEditing: function() {
        if(scope.editingCallback) {
          scope.editingCallback.edit();
          setEditMode(true);
        }
        $rootScope.$broadcast('enableEditing');
      },
      saveEditing: function(kommentti) {
        function after() {
          if (!scope.editingCallback.validate || scope.editingCallback.validate()) {
            scope.editingCallback.save(kommentti);
            setEditMode(false);
            $rootScope.$broadcast('disableEditing');
          }
        }

        if (scope.editingCallback) {
          if (_.isFunction(scope.editingCallback.asyncValidate)) {
            scope.editingCallback.asyncValidate(after);
          }
          else { after(); }
        }

        $rootScope.$broadcast('notifyCKEditor');
      },
      cancelEditing: function(tilanvaihto) {
        function doCancel() {
          setEditMode(false);
          if (scope.editingCallback) {
            scope.editingCallback.cancel();
          }
          $rootScope.$broadcast('disableEditing');
          $rootScope.$broadcast('notifyCKEditor');
        }
        if (scope.editingCallback) {
          if (_.isFunction(scope.editingCallback.canCancel) && !tilanvaihto) {
            scope.editingCallback.canCancel().then(function () {
              doCancel();
            });
          } else {
            doCancel();
          }
        }
      },
      registerCallback: function(callback) {
        if(!callback ||
            !angular.isFunction(callback.edit) ||
            !angular.isFunction(callback.save) ||
            !angular.isFunction(callback.cancel)) {
          console.error('callback-function invalid');
          throw 'editCallback-function invalid';
        }
        if (!angular.isFunction(callback.notify)) {
          callback.notify = angular.noop;
        }
        editmodeListener = null;
        $timeout(function() {
          scope.editingCallback = callback;
          scope.editModeDefer.resolve(scope.editMode);
          cbListener();
        }, 0);

      },
      unregisterCallback: function() {
        scope.editingCallback = null;
        setEditMode(false);
      },
      editingEnabled: function() {
        if(scope.editingCallback) {
          return true;
        } else {
          return false;
        }
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
