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

angular.module('eperusteApp')
  .service('Editointicatcher', function() {
    var f = angular.noop;

    return {
      register: function(cb) {
        f = cb;
      },
      give: function(osa) {
        f(osa);
        f = angular.noop;
      }
    };
  })
  .factory('Editointikontrollit', function($rootScope, $q, $timeout) {
    var scope = $rootScope.$new(true);
    scope.editingCallback = null;
    scope.editMode = false;

    scope.editModeDefer = $q.defer();

    this.lastModified = null;
    var additionalCallbacks = {save: [], start: [], cancel: []};
    var cbListener = null;

    function setEditMode(mode) {
      scope.editMode = mode;
      scope.editModeDefer = $q.defer();
      scope.editModeDefer.resolve(scope.editMode);
      if (scope.editingCallback) {
        scope.editingCallback.notify(mode);
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
      saveEditing: function() {
        function after() {
          if (!scope.editingCallback.validate || scope.editingCallback.validate()) {
            scope.editingCallback.save();
            angular.forEach(additionalCallbacks.save, function(callback) {
              // Kutsutaan kaikkia callback listenereitä ja annetaan parametrina
              // viimeisin muutettu objecti ja tieto siitä, onko editointikontrollit ylipäätänsä
              // pääällä
              // callback(self.lastModified, scope.editingCallback !== null);
              callback(undefined, scope.editingCallback !== null);
            });
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
      cancelEditing: function() {
        if(scope.editingCallback) {
          scope.editingCallback.cancel();
          setEditMode(false);
        }
        $rootScope.$broadcast('disableEditing');
        $rootScope.$broadcast('notifyCKEditor');
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
        $timeout(function() {
          scope.editingCallback = callback;
          scope.editModeDefer.resolve(scope.editMode);
          cbListener();
        }, 0);

//        scope.$watch('editingCallback', function() {
//          angular.forEach(callbackListeners, function(listener) {
//            // Kutsutaan kaikkia callback listenereitä ja annetaan parametrina
//            // viimeisin muutettu objecti ja tieto siitä, onko editointikontrollit ylipäätänsä
//            // pääällä
//            listener(self.lastModified, scope.editingCallback !== null);
//          });
//        });
      },
      unregisterCallback: function() {
        scope.editingCallback = null;
        setEditMode(false);

        additionalCallbacks = {save: [], start: [], cancel: []};
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
//        callbackListeners.push(callbackListener);
      },
      getEditModePromise: function() {
        return scope.editModeDefer.promise;
      },
      getEditMode: function() {
        return scope.editMode;
      }
    };
});
