var exec = require('cordova/exec');

var PLUGIN_NAME = 'MomoWallet'

exports.coolMethod = function (arg0, success, error) {
    exec(success, error, PLUGIN_NAME, 'coolMethod', [arg0]);
};

exports.requestPayment = function (arg0, success, error) {
    exec(success, error, PLUGIN_NAME, 'requestPayment', [arg0]);
};
