const merge = require("webpack-merge");
const path = require("path");
const webpack = require("webpack");
const UglifyJSPlugin = require('uglifyjs-webpack-plugin');
const CleanWebpackPlugin = require("clean-webpack-plugin");
const Visualizer = require("webpack-visualizer-plugin");

const basePath = __dirname;

module.exports = merge(require("./webpack.common.js"), {
    output: {
        path: path.join(__dirname, "dist"),
        filename: "[name].js",
        // publicPath: "/eperusteet-app/"
    },
    devtool: "nosources-source-map",
    plugins: [
        new CleanWebpackPlugin(["dist"]),
        new UglifyJSPlugin({
            sourceMap: true,
            mangle: false,
            uglifyOptions: {
                mangle: false,
            }
        }),
        new webpack.optimize.ModuleConcatenationPlugin(),
        new webpack.optimize.OccurrenceOrderPlugin(),
        new Visualizer({
            filename: "../.bundle_stats.html"
        }),
    ]
});
