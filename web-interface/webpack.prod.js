const { merge } = require('webpack-merge');
const common = require('./webpack.config.js');
const CopyWebpackPlugin = require('copy-webpack-plugin');
const { WebpackManifestPlugin } = require('webpack-manifest-plugin');

module.exports = merge(common, {
    bail: true,
    mode: 'production',
    plugins: [
        new WebpackManifestPlugin({
            fileName: 'asset-manifest.json',
        }),
        new CopyWebpackPlugin({
            patterns: [
                {from: "public/static", to: "static"}
            ]
        })
    ]
});