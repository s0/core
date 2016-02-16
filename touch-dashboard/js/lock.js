define(['audio', 'center', 'constants', 'util', 'widgets/combination_lock'],
  function(audio, center, C, util, lock_widget){
  'use strict';

  var _state,
      _elems,
      _close_dialog_timeout;

  var _lock_state = {
    state: C.ENUMS.LOCK_STATE.NONE,
    dialog: null,
    x: 0,
    y: 0,
    last_touch_x: 0,
    last_touch_y: 0,
    input: '',
    last_move: null
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

    center.switch_state(C.ENUMS.CENTER_STATE.NONE);
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
    if(_lock_state.state !== C.ENUMS.LOCK_STATE.DIALOG &&
       _lock_state.state !== C.ENUMS.LOCK_STATE.INPUTTING)
      return;

    audio.play("close1");

    _lock_state.state = C.ENUMS.LOCK_STATE.NONE;
    _lock_state.widget.del();
    _lock_state.widget = null;
  }

  function lock_touch_started(touch){
    if(_lock_state.state !== C.ENUMS.LOCK_STATE.DIALOG)
      return;

    if(_lock_state.widget.activate_touch_area(touch)){
      _lock_state.state = C.ENUMS.LOCK_STATE.INPUTTING;
      audio.play("beep1");
      _state.touch.touches.set(touch.identifier, {
        touch: touch,
        is_lock_input: true
      });
      _lock_state.last_touch_x = touch.clientX;
      _lock_state.last_touch_y = touch.clientY;
      _lock_state.input = '';
      _lock_state.last_move = null;
    } else {
      // Touched outside of lock
      close_unlock_dialog();
      center.switch_state(C.ENUMS.CENTER_STATE.CLOCK);
    }
  }

  function lock_touch_moved(touch){
    var _diff_x = _lock_state.last_touch_x - touch.clientX;
    var _diff_y = _lock_state.last_touch_y - touch.clientY;
    var _move_amt = C.COMBINATION_LOCK.MOVE_AMNT;
    var _move = null;
    if(_diff_x < -_move_amt && _lock_state.last_move !== 'r'){
      _move = 'r';
    } else if(_diff_x > _move_amt && _lock_state.last_move !== 'l'){
      _move = 'l';
    } else if(_diff_y < -_move_amt && _lock_state.last_move !== 'd'){
      _move = 'd';
    } else if(_diff_y > _move_amt && _lock_state.last_move !== 'u'){
      _move = 'u';
    }
    if(_move === null)
      return;
    // A move has been made
    _lock_state.input += _move;
    _lock_state.last_move = _move;
    _lock_state.last_touch_x = touch.clientX;
    _lock_state.last_touch_y = touch.clientY;
    _lock_state.widget.animate(_move);
    audio.play("beep1");
  }

  function lock_touch_stopped(){
    // Check if lock code is correct
    close_unlock_dialog();
    center.switch_state(C.ENUMS.CENTER_STATE.CLOCK);
  }

  return {
    state: _lock_state,
    init: init,
    show_unlock_overlay_points: show_unlock_overlay_points,
    show_unlock_dialog: show_unlock_dialog,
    close_unlock_dialog: close_unlock_dialog,
    lock_touch_started: lock_touch_started,
    lock_touch_moved: lock_touch_moved,
    lock_touch_stopped: lock_touch_stopped
  }

});
