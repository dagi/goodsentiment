var path = require('path');
var webpack = require('webpack');

module.exports = function getWebpackConfig() {
    return {
        entry: {
            app: ['./app/app']
        },

        output: {},

        module: {
            loaders: [
                {
                    test: /\.js$/,
                    loader: 'babel-loader?stage=0',
                    exclude: /node_modules/
                },

                {
                    test: /\.jsx$/,
                    loader: 'babel-loader?stage=0',
                    exclude: /node_modules/
                },

                {
                    test: /jquery\.js$/,
                    loader: 'expose?jQuery'
                },

                {
                    test: /\.styl$/,
                    loader: 'style!css?sourceMap!autoprefixer!stylus'
                },

                {
                    test: /\.scss$/,
                    loader: 'style!css?sourceMap!autoprefixer!sass'
                },

                {
                    test: /\.css$/,
                    loader: 'style!css?sourceMap!autoprefixer'
                },

                // https://msdn.microsoft.com/en-us/library/cc848897(v=vs.85).aspx
                {
                    test: /\.png$/,
                    loader: 'url-loader?limit=32768&mimetype=image/png'
                },

                {
                    test: /\.(jpg|eot|woff|ttf|svg)/,
                    loader: 'file-loader'
                }
            ]
        },

        resolve: {
            // Allow to omit extensions when requiring these files
            extensions: ['', '.js', '.jsx', '.styl', '.scss', '.css'],
            modulesDirectories: ['node_modules', 'bower_components'],

            alias: {
                react: path.join(__dirname, 'node_modules/react/'),
                jquery: path.join(__dirname, 'node_modules/jquery/dist/jquery')
            }
        },

        plugins: [
            new webpack.ProvidePlugin({
                React: 'react',
                $: 'jquery'
            })
        ]
    };
};
