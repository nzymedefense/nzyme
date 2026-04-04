const { merge } = require('webpack-merge');
const common = require('./webpack.config.js');
const CopyWebpackPlugin = require('copy-webpack-plugin');

module.exports = async () => {
    const { WebpackManifestPlugin } = await import('webpack-manifest-plugin');

    return merge(common, {
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
};
