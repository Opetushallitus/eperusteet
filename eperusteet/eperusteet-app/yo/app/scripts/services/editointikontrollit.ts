interface EditointiKontrollitCallbacks {
    edit: any;
    save: any;
    cancel: any;
    notify: any;
    // validate: (validator: any) => boolean;
    // validate: (validator: any) => Promise<boolean>;
}

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
.factory('Editointikontrollit', ($rootScope, $q, Utils, Notifikaatiot) => {
    let scope = $rootScope.$new(true);
    scope.editingCallback = null;
    scope.editMode = false;

    scope.editModeDefer = $q.defer();

    this.lastModified = null;
    let cbListener = _.noop;
    let editmodeListener = null;

    async function setEditMode(mode) {
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
        async startEditing() {
            try {
                await scope.editingCallback.edit();
                setEditMode(true);
                $rootScope.$broadcast('enableEditing');
                $rootScope.$$ekEditing = true;
            }
            catch (ex) {
                $rootScope.$$ekEditing = false;
            }
        },
        async saveEditing(kommentti) {
            $rootScope.$broadcast('editointikontrollit:preSave');
            $rootScope.$broadcast('notifyCKEditor');
            let err;

            function mandatoryFieldValidator(fields, target) {
                err = undefined;
                const fieldsf = _.filter(fields || [], function (field) {
                    return field.mandatory;
                });

                $rootScope.$$ekEditing = false;

                if (!target) {
                    return false;
                }
                else if (_.isString(target)) {
                    return !_.isEmpty(target);
                }
                else if (_.isObject(target) && !_.isEmpty(target) && !_.isEmpty(fieldsf)) {
                    return _.all(fieldsf, function (field) {
                        var valid = Utils.hasLocalizedText(target[field.path]);
                        if (!valid) {
                            err = field.mandatoryMessage;
                        }
                        return valid;
                    });
                }
                else {
                    return true;
                }
            }

            async function afterSave() {
                setEditMode(false);
                $rootScope.$broadcast('disableEditing');
                $rootScope.$$ekEditing = false;
            }

            function after() {
                if (scope.editingCallback.validate(mandatoryFieldValidator)) {
                    if (scope.editingCallback.asyncSave) {
                        scope.editingCallback.asyncSave(kommentti, afterSave);
                    }
                    else {
                        afterSave();
                        scope.editingCallback.save(kommentti);
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
        },
        async cancelEditing() {
            setEditMode(false);
            $rootScope.$broadcast('disableEditing');
            $rootScope.$broadcast('notifyCKEditor');
            await scope.editingCallback.cancel();
            $rootScope.$$ekEditing = false;
        },
        registerCallback: function (callback) {
            if (!callback || !_.isFunction(callback.edit) ||
                (!_.isFunction(callback.save) && !_.isFunction(callback.asyncSave)) || !_.isFunction(callback.cancel)) {
                console.error('callback-function invalid');
                throw 'editCallback-function invalid';
            }

            callback.validate = callback.validate || _.constant(true);

            if (callback.asyncSave) {
                callback.save = _.noop;
            }

            if (!_.isFunction(callback.notify)) {
                callback.notify = _.noop;
            }

            editmodeListener = null;
            scope.editingCallback = callback;
            scope.editModeDefer.resolve(scope.editMode);
            cbListener();
        },
        unregisterCallback: function () {
            scope.editingCallback = null;
            setEditMode(false);
        },
        editingEnabled: function () {
            return !!scope.editingCallback;
        },
        registerCallbackListener: function (callbackListener) {
            cbListener = callbackListener;
        },
        registerEditModeListener: function (listener) {
            editmodeListener = listener;
        },
        getEditModePromise: function () {
            return scope.editModeDefer.promise;
        },
        getEditMode: function () {
            return scope.editMode;
        }
    };
});
