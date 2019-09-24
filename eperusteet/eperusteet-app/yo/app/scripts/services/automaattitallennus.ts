import * as angular from "angular";
import _ from "lodash";

interface IAutomaattitallennusService {
    isSupported: () => boolean;
    save: (id: string, data: any) => void;
    load: (id: string) => any;
    remove: (id: string) => void;
    start: (id: string, fn: () => void, restoreFn: (data: any) => void) => void;
    stop: () => void;
}

angular
    .module("eperusteApp")
    .service("AutomaattitallennusService", (
        Varmistusdialogi,
    ): IAutomaattitallennusService => {
        const data = {
            updaterId: null as any,
        };

        function getId(): string {
            return window.localStorage.getItem("editableId");
        }

        function setId(id: string) {
            window.localStorage.setItem("editableId", id);
        }

        function clearId() {
            const id = getId();
            if (id) {
                window.localStorage.removeItem(id);
                window.localStorage.removeItem("editableId");
            }
        }

        async function restoreOld() {
            return new Promise((resolve) => {
                Varmistusdialogi.dialogi({
                    otsikko: "palautetaanko-vanha-versio",
                    teksti: "peruttu-versio-loytyi",
                    primaryBtn: "muokkaa",
                    successCb: () => resolve(true),
                    failureCb: () => resolve(false),
                })();
            });
        }

        return {
            isSupported() {
                return !!window.localStorage;
            },
            async start(id: string, fn: () => any, restoreFn: (data: any) => void) {
                const old = getId();
                if (old === id) {
                    const oldData = this.load(old);
                    if (oldData && await restoreOld()) {
                        restoreFn(JSON.parse(oldData));
                    }
                }
                else {
                    this.stop();
                }

                setId(id);
                this.save(id, fn());
                data.updaterId = setInterval(() => {
                    this.save(id, fn());
                }, 5000);
            },
            stop() {
                const id = getId();
                if (id && data.updaterId) {
                    clearInterval(data.updaterId);
                    clearId();
                    data.updaterId = 0;
                }
            },
            save(id: string, data: any) {

                const dataStr = JSON.stringify(data, (key, value) => {
                    if (_.startsWith(key, '$')) {
                        return;
                    }
                    return value;
                });

                window.localStorage.setItem(id, dataStr);
            },
            load(id: string) {
                return window.localStorage.getItem(id);
            },
            remove(id: string) {
                window.localStorage.removeItem(id);
            },
        };
    });
