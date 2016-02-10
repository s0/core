define(['constants'], function(C){

  var _next_request_id = 1;
  var _open_requests = {};
  var _listeners = {};
  var _socket = new WebSocket(C.SERVER.WS_URL);
  _socket.onopen = on_socket_open;
  _socket.onmessage = on_socket_message;

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
    send_request('listen', {target: target}, function(payload){
      _listeners[payload.listener_id] = callback;
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
    setup_listener('media', function(payload){
      console.log('media callback 1: ', payload);
    });
    setup_listener('media', function(payload){
      console.log('media callback 2: ', payload);
    });
  }

  function on_socket_message (event){
    message = JSON.parse(event.data);
    switch(message.type){
      case "error":
        if (message.request_id !== undefined){
          get_request(message.request_id).error(message);
        } else {
          console.warn("Error from Server: ", message);
        }
        return;
      case "response":
        get_request(message.request_id).success(message.payload);
        return;
      case "event":
        var _callback = _listeners[message.listener_id];
        if(_callback === undefined)
          throw new Error("No listener with id: " + id);
        _callback(message.payload);
        return;
    }
    console.warn("unhandled from server: ", message);
  }

  return {
    send_request: send_request,
    setup_listener: setup_listener
  }
});
