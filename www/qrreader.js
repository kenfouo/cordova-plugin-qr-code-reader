var exec = require('cordova/exec');

exports.decodeImage = function(base64Image, success, error) {
    exec(success, error, 'QRReader', 'decodeImage', [base64Image]);
};
