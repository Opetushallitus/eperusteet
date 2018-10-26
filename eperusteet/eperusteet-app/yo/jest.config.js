module.exports = {
    globals: {
        "ts-jest": {
            useBabelrc: false
        },
        __TRANSFORM_HTML__: true
    },
    moduleFileExtensions: ["ts", "tsx", "js", "jsx", "json"],
    coverageReporters: ["json", "text"],
    coveragePathIgnorePatterns: [
        "/node_modules/",
        "/e2e",
        "/dist/",
        "/etc/",
        "/node/",
        "/typings/",
        "/test/",
        "/coverage/",
        "/ckeditor-plugins/"
    ],
    setupFiles: ["<rootDir>/app/testsetup.ts"],
    moduleNameMapper: {
        "^app/(.*)": "<rootDir>/app/$1",
        "^scripts/(.*)": "<rootDir>/app/scripts/$1",
        "^views/(.*)": "<rootDir>/app/views/$1",
        "^styles/(.*)": "<rootDir>/app/styles/$1",
        "^images/(.*)": "<rootDir>/app/images/$1"
    },
    testPathIgnorePatterns: ["/node_modules/"],
    watchPathIgnorePatterns: ["/node_modules/"],
    testMatch: ["<rootDir>/app/**/__tests__/**/*.ts?(x)", "<rootDir>/app/**/?(*.)(spec|test).ts?(x)"],
    transform: {
        "^.+\\.(js|ts|html)$": "<rootDir>/node_modules/ts-jest/preprocessor.js",
        "^.+\\.(jade|pug)$": "<rootDir>/node_modules/pug-jest"
    }
};
