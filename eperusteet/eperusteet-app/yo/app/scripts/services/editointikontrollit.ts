import _ from "lodash";
import * as angular from "angular";

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
angular.module("eperusteApp").factory("Editointikontrollit", ($rootScope, $q, Utils, Notifikaatiot,
                                                              YleinenData, $uibModal) => {
    let scope = $rootScope.$new(true);
    scope.editingCallback = null;
    scope.editMode = false;

    scope.editModeDefer = $q.defer();

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
        lastModified: null,
        async startEditing() {
            try {
                await scope.editingCallback.edit();
                setEditMode(true);
                $rootScope.$broadcast("enableEditing");
                $rootScope.$$ekEditing = true;
            } catch (ex) {
                $rootScope.$$ekEditing = false;
            }
        },

        notifySentenceCaseWarnings(obj) {
            const warnings = [];
            _.each(obj.paths, path => {
                if (!this.sentenceCaseValidator(_.get(obj.obj, path))) {
                    warnings.push(_.get(obj.obj, path));
                }
            });
            if (warnings.length > 0) {
                $uibModal.open({
                    template: require("views/modals/sisaltoMuotoiluVaroitusModal.html"),
                    resolve: {
                        warnings: () => warnings
                    },
                    controller: ($scope, $uibModalInstance, warnings) => {
                        $scope.warnings = warnings;
                        $scope.ok = () => {
                            $uibModalInstance.close();
                        };
                        $scope.cancel = () => {
                            $uibModalInstance.dismiss('cancel');
                        };
                    },
                }).result.then(() => {
                    if (_.isFunction(obj.after)) {
                            obj.after();
                    }
                });
            } else {
                if (_.isFunction(obj.after)) {
                    obj.after();
                }
            }
        },

        sentenceCaseValidator(obj) {
            function validateSentenceCase(input) {
                return !!(_.isString(input) && _.eq(input, _.capitalize(input.toLowerCase())));
            }

            if (validateSentenceCase(obj)) {
                return true;
            } else if (_.isObject(obj) && !_.isEmpty(obj)) {

                const langs = _(obj)
                    .keys()
                    .filter(key => _.includes(_.values(YleinenData.kielet), key))
                    .value();

                let valid = true;
                _.each(langs, (key: any) => {
                    if (!validateSentenceCase(obj[key])) {
                        valid = false;
                    }
                });

                return valid;
            }
            // Undefined is okay
            return true;
        },

        async saveEditing(kommentti) {
            $rootScope.$broadcast("editointikontrollit:preSave");
            $rootScope.$broadcast("notifyCKEditor");
            let err;

            function mandatoryFieldValidator(fields, target) {
                err = undefined;
                const fieldsf = _.filter(fields || [], function(field) {
                    return (field as any).mandatory;
                });

                $rootScope.$$ekEditing = false;

                if (!target) {
                    return false;
                } else if (_.isString(target)) {
                    return !_.isEmpty(target);
                } else if (_.isObject(target) && !_.isEmpty(target) && !_.isEmpty(fieldsf)) {
                    return _.all(fieldsf, function(field: any) {
                        var valid = Utils.hasLocalizedText(target[field.path]);
                        if (!valid) {
                            err = field.mandatoryMessage;
                        }
                        return valid;
                    });
                } else {
                    return true;
                }
            }

            async function afterSave() {
                setEditMode(false);
                $rootScope.$broadcast("disableEditing");
                $rootScope.$$ekEditing = false;
            }

            function after() {
                if (scope.editingCallback.validate(mandatoryFieldValidator)) {
                    if (scope.editingCallback.asyncSave) {
                        scope.editingCallback.asyncSave(kommentti, afterSave);
                    } else {
                        afterSave();
                        scope.editingCallback.save(kommentti);
                    }
                } else {
                    Notifikaatiot.varoitus(err || "mandatory-odottamaton-virhe");
                }
            }

            if (scope.editingCallback) {
                if (_.isFunction(scope.editingCallback.asyncValidate)) {
                    scope.editingCallback.asyncValidate(after);
                } else {
                    after();
                }
            }
        },
        async cancelEditing() {
            setEditMode(false);
            $rootScope.$broadcast("disableEditing");
            $rootScope.$broadcast("notifyCKEditor");
            await scope.editingCallback.cancel();
            $rootScope.$$ekEditing = false;
        },
        registerCallback: function(callback) {
            if (
                !callback ||
                !_.isFunction(callback.edit) ||
                (!_.isFunction(callback.save) && !_.isFunction(callback.asyncSave)) ||
                !_.isFunction(callback.cancel)
            ) {
                console.error("callback-function invalid");
                throw "editCallback-function invalid";
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
        registerEditModeListener: function(listener) {
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
