define(["constants", "lock", "util"], function(C, lock, util){

  var _state,
      _elems;

  function init(state, elems, $window){
    _state = state;
    _elems = elems;

    $window.on("touchstart", touchstart)
           .on("touchmove", touchmove)
           .on("touchend", touchend);
  }

  function touchstart(e){
    for(var i =0; i < e.originalEvent.changedTouches.length; i++){
      var _touch = e.originalEvent.changedTouches[i];

      if(_state.mode === C.ENUMS.MODE.LOCKED){

        if(lock.state.state === C.ENUMS.LOCK_STATE.DIALOG) {
          // check if touch is outside element (to close)
          var $inner = lock.state.dialog.children('.inner:first');
          if(!util.is_over(_touch, $inner)){
            lock.close_unlock_dialog();
          }
        }

        if(lock.state.state === C.ENUMS.LOCK_STATE.NONE){
          // Maximum 5
          if(_state.touch.touches.size >= 5)
            return;

          add_touch_with_overlay(_touch, "multi-2");

          if(_state.touch.touches.size === 5){
            lock.show_unlock_overlay_points();
          }
        }
      }
    }
    e.preventDefault();
  }

  function touchmove(e){
    for(var i =0; i < e.originalEvent.changedTouches.length; i++){
      var _touch = e.originalEvent.changedTouches[i];
      var _t = _state.touch.touches.get(_touch.identifier);
      if(_t === undefined)
        continue;
      _t.touch = _touch;
      if(_t.has_overlay){
        util.set_position_to_touch(_t.overlay, _touch);
        _t.overlay.css({
          top: _touch.clientY,
          left: _touch.clientX
        });
      }
      e.preventDefault();
    }
  }

  function touchend(e){
    for(var i =0; i < e.originalEvent.changedTouches.length; i++){
      var _touch = e.originalEvent.changedTouches[i];
      var _t = _state.touch.touches.get(_touch.identifier);
      if(_t === undefined)
        continue;
      if(_t.has_overlay){
        util.close_and_delete(_t.overlay);
      }
      _state.touch.touches.delete(_touch.identifier);
    }
    // cleanup everything
    if(e.originalEvent.touches.length === 0){
      _state.touch.touches.clear();
      _elems.touch_overlays.children().each(function(){
        util.close_and_delete($(this));
      });
      _elems.lock_underlays.children().each(function(){
        util.close_and_delete($(this));
      });
      if(lock.state.state === C.ENUMS.LOCK_STATE.TOUCH_POINTS)
        lock.show_unlock_dialog();
    }
    e.preventDefault();
  }

  function add_touch_with_overlay(touch, template){
    var $touch_point = _elems.touch_overlay_templates[template].clone();
    $touch_point.appendTo(_elems.touch_overlays);
    util.set_position_to_touch($touch_point, touch);
    _state.touch.touches.set(touch.identifier, {
      touch: touch,
      has_overlay: true,
      overlay: $touch_point
    });
  }

  return {
    init: init
  }

});
