define(['constants'], function(C){
  'use strict';

  var _elems;
  var _center_state = C.ENUMS.CENTER_STATE.NONE;
  var _states = {};

  function init(elems){
    _elems = elems;
    setup_none_state();
    setup_clock_state();

    switch_state(C.ENUMS.CENTER_STATE.CLOCK);
  }

  function setup_none_state() {
    _states[C.ENUMS.CENTER_STATE.NONE] = {
      hide: function(){},
      show: function(){}
    };
  }

  function setup_clock_state() {
    var $clock = $('.widget.clock');
    var $media_info = $('.widget.media-info');
    _states[C.ENUMS.CENTER_STATE.CLOCK] = {
      hide: function(){
        $clock.addClass('hidden');
        $media_info.addClass('hidden');
      },
      show: function(){
        $clock.removeClass('hidden');
        $media_info.removeClass('hidden');
      }
    };
  }

  function switch_state(state){
    console.log("switching to: " + state);
    if (_states[state] === undefined)
      throw new Error("State " + state + " undefined");
    if (_center_state === state)
      return;
    var _animation_delay = _states[_center_state].hide();
    _center_state = state;
    if (_animation_delay === undefined)
      _animation_delay = 0;
    setTimeout(_states[_center_state].show, _animation_delay);
  }

  function get_state(){
    return _center_state;
  }

  return {
    init: init,
    switch_state: switch_state,
    get_state: get_state
  };

});
