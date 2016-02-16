define(['listeners', 'server'], function(listeners, server){

  var _state = null;
  var _listenable = listeners.new_listenable();

  function init(){
    server.setup_listener('media', function(payload){
      _state = payload;
      _listenable.visit(function(callback) {
        callback(_state);
      });
    });
  }

  function add_state_listener(listener) {
    _listenable.add(listener);
    if (_state !== null)
      listener.call(_state);
  }

  return {
    init: init,
    add_state_listener: add_state_listener
  }

});
