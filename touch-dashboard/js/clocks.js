define([], function(){
  'use strict';

  var _elems;

  function init(elems){
    _elems = elems;

    setInterval(update_clocks, 100);
    update_clocks();
  }

  // Write time to clocks
  function update_clocks(){
    var _now = new Date();
    var _time = [_now.getHours(), _now.getMinutes(), _now.getSeconds()];
    for (var i = 1; i < 3; i++) {
      if (_time[i] < 10)
        _time[i] = "0" + _time[i];
    }
    _elems.clock_text.text(_time.join(':'));
  }

  return {
    init: init
  }
});
