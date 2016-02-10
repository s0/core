define(['constants'], function(C){

  var _socket = new WebSocket(C.SERVER.WS_URL);

  _socket.onopen = function (event) {
    _socket.send("Here's some text that the server is urgently awaiting!");
  };

  _socket.onmessage = function (event) {
    console.log("message from server: " + event.data);
  }

  return {
  }
});
