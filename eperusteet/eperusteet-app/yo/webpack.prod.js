const merge = require("webpack-merge");
const path = require("path");
const webpack = require("webpack");
const UglifyJSPlugin = require('uglifyjs-webpack-plugin');
const CleanWebpackPlugin = require("clean-webpack-plugin");


const basePath = __dirname;

module.exports = merge(require("./webpack.common.js"), {
    plugins: [
        new CleanWebpackPlugin(["dist"]),
        new UglifyJSPlugin()
    ]
});
