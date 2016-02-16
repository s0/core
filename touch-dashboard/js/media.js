define(['server'], function(server){

  var _state = null;
  var _listeners = [];

  function init(){
    server.setup_listener('media', function(payload){
      _state = payload;
      _listeners.forEach(function(callback) {
        callback(_state);
      });
    });
  }

  function add_state_listener(callback) {
    _listeners.push(callback);
    if (_state !== null)
      callback(_state);
  }

  return {
    init: init,
    add_state_listener: add_state_listener
  }

});
