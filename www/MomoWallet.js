var exec = require('cordova/exec');

var PLUGIN_NAME = 'MomoWallet';

exports.requestPayment = function (arg0, success, error) {
    exec(success, error, PLUGIN_NAME, 'requestPayment', [arg0]);
};
