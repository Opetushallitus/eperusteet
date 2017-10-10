const merge = require("webpack-merge");
const path = require("path");
const webpack = require("webpack");
const FriendlyErrorsWebpackPlugin = require("friendly-errors-webpack-plugin");

const basePath = __dirname;

module.exports = merge(require("./webpack.common.js"), {
    plugins: [
        new webpack.DefinePlugin({
          "process.env.NODE_ENV": JSON.stringify("development")
        }),
        new FriendlyErrorsWebpackPlugin(),
        new webpack.HotModuleReplacementPlugin()
    ],
    devtool: "inline-source-map",
    devServer: {
        host: "127.0.0.1",
        port: 9000,
        hot: true,
        quiet: true,
        proxy: {
            "/eperusteet-service": {
                target: "http://localhost:8080",
                secure: false
            }
        }
    }
});
