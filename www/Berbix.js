var exec = require('cordova/exec');

exports.verify = function(success, error, config) {
  exec(success, error, 'Berbix', 'verify', [config]);
};
