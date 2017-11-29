const path = require("path");
const webpack = require("webpack");
const HtmlWebpackPlugin = require("html-webpack-plugin");
const ManifestPlugin = require("webpack-manifest-plugin");
const CopyWebpackPlugin = require("copy-webpack-plugin");

// FIX https://github.com/webpack-contrib/copy-webpack-plugin/issues/59
// Fails on linux too
const fs = require('fs');
const gracefulFs = require('graceful-fs');
gracefulFs.gracefulify(fs);

// FIXME: import-loader
// https://github.com/angular-ui/ui-sortable/issues/518

module.exports = {
    resolve: {
        extensions: [".ts", ".js", ".css", ".html", ".pug"],
        alias: {
            views: path.resolve(__dirname, "./app/views/"),
            scripts: path.resolve(__dirname, "./app/scripts/"),
            "eperusteet-esitys": path.resolve(__dirname, "./app/eperusteet-esitys/"),
            styles: path.resolve(__dirname, "./app/styles/"),
            images: path.resolve(__dirname, "./app/images/")
        }
    },
    entry: {
        app: "./app/index.ts"
        // vendor: [
        //   "lodash",
        //   "angular",
        //   "jquery",
        //   "jquery-ui",
        // ]
    },
    module: {
        rules: [
            {
                test: /\.tsx?$/,
                exclude: /node_modules/,
                use: [
                    "imports-loader?$UI=jquery-ui/ui/widgets/sortable",
                    {
                        loader: "ts-loader",
                        options: {
                            transpileOnly: true,
                        }
                    }
                ]
            },
            {
                test: /\.js$/,
                use: [
                    {
                        loader: "babel-loader",
                    }
                ]
            },
            {
                test: /\.(scss|css)$/,
                use: [
                    {
                        loader: "style-loader"
                    },
                    {
                        loader: "css-loader"
                    },
                    {
                        loader: "sass-loader"
                    }
                ]
            },
            {
                test: /\.(html)$/,
                use: {
                    loader: "html-loader",
                    options: {
                        attrs: [":data-src"],
                        minimize: true,
                        removeComments: true,
                        collapseWhitespace: true
                    }
                }
            },
            {
                test: /\.pug$/,
                use: ["pug-loader"]
            },
            {
                test: /\.(woff|woff2|eot|ttf|otf)$/,
                use: [
                    {
                        loader: "url-loader",
                        options: {
                            limit: 8192
                        }
                    }
                ]
            },
            {
                test: /\.(jpe?g|png|gif|svg)$/i,
                use: ["file-loader"]
            }
        ]
    },
    plugins: [
        new webpack.ProvidePlugin({
            $: "jquery",
            jQuery: "jquery",
            "window.jQuery": "jquery",
        }),
        new HtmlWebpackPlugin({
            filename: "index.html",
            template: "app/index.html",
            hash: true
        }),

        new CopyWebpackPlugin(
            [
                {
                    context: path.resolve(__dirname, "./node_modules/mathjax/"),
                    from: "**/*",
                    to: "bower_components/MathJax"
                },
                {
                    context: path.resolve(__dirname, "./node_modules/ckeditor/"),
                    from: "**/*",
                    to: "bower_components/ckeditor"
                },
                {
                    context: path.resolve(__dirname, "./app/ckeditor-plugins/"),
                    from: "**",
                    to: "ckeditor-plugins"
                },
                {
                    context: path.resolve(__dirname, "./app/images/"),
                    from: "**/*",
                    to: "images"
                },
                {
                    context: path.resolve(__dirname, "./app/views/"),
                    from: "**/*.html",
                    to: "views"
                }
            ],
            {}
        ),
        new ManifestPlugin()
    ]
};
