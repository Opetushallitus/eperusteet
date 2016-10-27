module Helper {
    export const allEnabled = (x) => {
        element.all(x).each((el) => {
            expect(el.isEnabled()).toBeTruthy();
        });
    };

    export const allDisabled = (x) => {
        element.all(x).each((el) => {
            expect(el.isEnabled()).toBeFalsy();
        });
    };
}
