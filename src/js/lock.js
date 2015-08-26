define(['constants', 'util'], function(C, util){

  var _state,
      _elems;

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
    var _min_x = null;
    var _max_x = null;
    var _min_y = null;
    var _max_y = null;

    _state.touch.touches.forEach(function(t){
      var _x = t.touch.clientX;
      var _y = t.touch.clientY;
      if(_min_x === null || _x < _min_x)
        _min_x = _x;
      if(_max_x === null || _x > _max_x)
        _max_x = _x;
      if(_min_y === null || _y < _min_y)
        _min_y = _y;
      if(_max_y === null || _y > _max_y)
        _max_y = _y;
    });

    if(_max_y - _min_y < 250 || _max_y - _min_y > 900 || _max_x - _min_x < 250 || _max_x - _min_x > 900)
      // Bad placement
      return;

    // Show Overlay Points
    _lock_state.state = C.ENUMS.LOCK_STATE.TOUCH_POINTS;
    _lock_state.x = (_min_x + _max_x) / 2;
    _lock_state.y = (_min_y + _max_y) / 2;

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

    _lock_state.state = C.ENUMS.LOCK_STATE.DIALOG

    _lock_state.dialog = _elems.lock_dialog_template.clone();
    _lock_state.dialog.appendTo(_elems.interaction);
    _lock_state.dialog.css({
      top: _lock_state.y,
      left: _lock_state.x
    });
  }

  function close_unlock_dialog(){
    if(_lock_state.state !== C.ENUMS.LOCK_STATE.DIALOG)
      return;

    _lock_state.state = C.ENUMS.LOCK_STATE.NONE;
    util.close_and_delete(_lock_state.dialog);
    _lock_state.dialog = null;
  }

  return {
    state: _lock_state,
    init: init,
    show_unlock_overlay_points: show_unlock_overlay_points,
    show_unlock_dialog: show_unlock_dialog,
    close_unlock_dialog: close_unlock_dialog
  }

});
