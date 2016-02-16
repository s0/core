define(['constants'], function(C){
  'use strict';

  var _next_request_id = 1;
  var _open_requests = {};
  var _listeners = [];

  // Connection Specific State
  var _socket = null;
  var _listener_callbacks = {};


  function init(){
    _socket = new WebSocket(C.SERVER.WS_URL);
    _socket.onopen = on_socket_open;
    _socket.onmessage = on_socket_message;
  }

  function is_socket_ready(){
    return _socket !== null && _socket.readyState === WebSocket.OPEN;
  }

  function send_message(message){
    _socket.send(JSON.stringify(message));
  }

  function send_request(type, payload, success, error){
    if(success === undefined || error === undefined)
      throw new Error("success and error must be defined");
    var _id = _next_request_id ++;
    _open_requests[_id] = {
      success: success,
      error: error
    };
    send_message({
      type: 'request',
      request_type: type,
      request_id: _id,
      payload: payload
    });
  }

  function setup_listener(target, callback){
    var _listener = {
      target: target,
      callback: callback
    };
    _listeners.push(_listener);
    if (is_socket_ready())
      do_setup_listener(_listener);
  }

  function do_setup_listener(listener){
    send_request('listen', {target: listener.target}, function(payload){
      _listener_callbacks[payload.listener_id] = listener.callback;
    }, function(message){
      console.error("error requesting listener: ", message);
    });
  }

  function get_request(id){
    var _req = _open_requests[id];
    if (_req === undefined){
      throw new Error("No open request with id: " + id);
    }
    return _req;
  }

  function on_socket_open (event){
    // Re initialise connection-specific state
    _listener_callbacks = {};
    _listeners.forEach(do_setup_listener);
  }

  function on_socket_message (event){
    var _message = JSON.parse(event.data);
    switch(_message.type){
      case "error":
        if (_message.request_id !== undefined){
          get_request(_message.request_id).error(_message);
        } else {
          console.warn("Error from Server: ", _message);
        }
        return;
      case "response":
        get_request(_message.request_id).success(_message.payload);
        return;
      case "event":
        var _callback = _listener_callbacks[_message.listener_id];
        if(_callback === undefined)
          throw new Error("No listener with id: " + id);
        _callback(_message.payload);
        return;
    }
    console.warn("unhandled from server: ", _message);
  }

  return {
    init: init,
    send_request: send_request,
    setup_listener: setup_listener
  }
});
