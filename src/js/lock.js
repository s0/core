define(['audio', 'constants', 'util', 'widgets/combination_lock'], function(audio, C, util, lock_widget){

  var _state,
      _elems,
      _close_dialog_timeout;

  var _lock_state = {
    state: C.ENUMS.LOCK_STATE.NONE,
    dialog: null,
    x: 0,
    y: 0
  }

  function init(state, elems){
    _state = state;
    _elems = elems;
  }


  function show_unlock_overlay_points(){
    // Check not already open
    if(_lock_state.state !== C.ENUMS.LOCK_STATE.NONE)
      return;

    // Check decent layout
    var _bounds = util.compute_coordinate_bounds(
      _state.touch.touches,
      function(t){
        return {x: t.touch.clientX, y: t.touch.clientY}
      });

    if(_bounds.max_y - _bounds.min_y < 250 ||
       _bounds.max_y - _bounds.min_y > 900 ||
       _bounds.max_x - _bounds.min_x < 250 ||
       _bounds.max_x - _bounds.min_x > 900)
      // Bad placement
      return;

    // Show Overlay Points
    audio.play("ready1");
    _lock_state.state = C.ENUMS.LOCK_STATE.TOUCH_POINTS;

    _lock_state.touch_points = [];
    _state.touch.touches.forEach(function(t){
      // replace overlay with lock-finger
      util.close_and_delete(t.overlay);
      var $touch_point = _elems.touch_overlay_templates["lock-finger"].clone();
      $touch_point.appendTo(_elems.lock_underlays);
      util.set_position_to_touch($touch_point, t.touch);
      t.overlay = $touch_point;
    });
  }

  function show_unlock_dialog(){
    if(_lock_state.state === C.ENUMS.LOCK_STATE.DIALOG)
      return;

    audio.play("ready2");

    _lock_state.state = C.ENUMS.LOCK_STATE.DIALOG

    _lock_state.widget = lock_widget.create();

    var _bounds = util.compute_coordinate_bounds(
      _state.touch.last_touches,
      function(t){
        return {x: t.clientX, y: t.clientY}
      });

    var _x = (_bounds.min_x + _bounds.max_x) / 2;
    var _y = (_bounds.min_y + _bounds.max_y) / 2;

    _lock_state.widget.attach(_elems.interaction, _x, _y);

    if(C.COMBINATION_LOCK.TIMEOUT !== null){
      _close_dialog_timeout = setTimeout(function(){
        if(_lock_state.state === C.ENUMS.LOCK_STATE.DIALOG)
          close_unlock_dialog();
      }, C.COMBINATION_LOCK.TIMEOUT);
    }
  }

  function close_unlock_dialog(){
    if(_lock_state.state !== C.ENUMS.LOCK_STATE.DIALOG)
      return;

    audio.play("close1");

    _lock_state.state = C.ENUMS.LOCK_STATE.NONE;
    _lock_state.widget.del();
    _lock_state.widget = null;
  }

  return {
    state: _lock_state,
    init: init,
    show_unlock_overlay_points: show_unlock_overlay_points,
    show_unlock_dialog: show_unlock_dialog,
    close_unlock_dialog: close_unlock_dialog
  }

});
