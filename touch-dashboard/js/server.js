define(['constants'], function(C){

  var _socket = new WebSocket(C.SERVER.WS_URL);

  _socket.onopen = function (event) {
    _socket.send('{"type": "ping"}');
  };

  _socket.onmessage = function (event) {
    console.log("message from server: " + event.data);
  }

  return {
  }
});
