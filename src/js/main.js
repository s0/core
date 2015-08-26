require(['constants'], function(C){

  // cached number values
  var _x_step = C.HEX_WIDTH / 4 * 3 | 0;
  var _y_step = C.HEX_HEIGHT / 2 | 0;
  var _half_w = C.HEX_WIDTH / 2 | 0;
  var _quater_w = C.HEX_WIDTH / 4 | 0;
  var _half_h = C.HEX_HEIGHT / 2 | 0;

  var _mode = C.ENUMS.MODE.LOCKED;
  var _lock_state = {
    state: C.ENUMS.LOCK_STATE.NONE,
    dialog: null,
    x: 0,
    y: 0
  }

  var elems = {
    templates: null,
    lock_dialog_template: null,
    stage: null,
    hex_background: null,
    interaction: null,
    touch_overlays: null,
    lock_underlays: null,
    clock_text: null
  };

  var touch_overlay_templates = {};

  var _touch_state = {
    touches: new Map()
  }

  $(document).ready(function(){

    elems.templates = $('#templates');
    elems.lock_dialog_template = elems.templates.children('.lock-dialog:first');
    elems.stage = $('#stage');
    elems.hex_background = elems.stage.children('.hex-background:first');
    elems.interaction = elems.stage.children('.interaction:first');
    elems.touch_overlays = elems.interaction.children('.touch-overlays:first');
    elems.lock_underlays = elems.interaction.children('.lock-underlays:first');
    elems.clock_text = $('.clock .text');

    // Collect Templates
    elems.templates.children('.touch-overlays:first').children().each(function(){
      var $this = $(this);
      touch_overlay_templates[$this.data('overlay')] = $this;
    });

    // Start clocks
    setInterval(update_clocks, 100);
    update_clocks();

    $(window).resize(redraw);
    redraw();

    $(window).on("touchstart", function(e){
      for(var i =0; i < e.originalEvent.changedTouches.length; i++){
        var _touch = e.originalEvent.changedTouches[i];

        if(_mode === C.ENUMS.MODE.LOCKED){

          if(_lock_state.state === C.ENUMS.LOCK_STATE.DIALOG) {
            // check if touch is outside element (to close)
            var $inner = _lock_state.dialog.children('.inner:first');
            if(!is_over(_touch, $inner)){
              close_unlock_dialog();
            }
          }

          if(_lock_state.state === C.ENUMS.LOCK_STATE.NONE){
            // Maximum 5
            if(_touch_state.touches.size >= 5)
              return;

            add_touch_with_overlay(_touch, "multi-2");

            if(_touch_state.touches.size === 5){
              show_unlock_overlay_points();
            }
          }
        }
      }
      e.preventDefault();
    }).on("touchmove", function(e){
      for(var i =0; i < e.originalEvent.changedTouches.length; i++){
        var _touch = e.originalEvent.changedTouches[i];
        var _t = _touch_state.touches.get(_touch.identifier);
        if(_t === undefined)
          continue;
        _t.touch = _touch;
        if(_t.has_overlay){
          set_position_to_touch(_t.overlay, _touch);
          _t.overlay.css({
            top: _touch.clientY,
            left: _touch.clientX
          });
        }
        e.preventDefault();
      }
    }).on("touchend", function(e){
      for(var i =0; i < e.originalEvent.changedTouches.length; i++){
        var _touch = e.originalEvent.changedTouches[i];
        var _t = _touch_state.touches.get(_touch.identifier);
        if(_t === undefined)
          continue;
        if(_t.has_overlay){
          close_and_delete(_t.overlay);
        }
        _touch_state.touches.delete(_touch.identifier);
      }
      // cleanup everything
      if(e.originalEvent.touches.length === 0){
        _touch_state.touches.clear();
        elems.touch_overlays.children().each(function(){
          close_and_delete($(this));
        });
        elems.lock_underlays.children().each(function(){
          close_and_delete($(this));
        });
        if(_lock_state.state === C.ENUMS.LOCK_STATE.TOUCH_POINTS)
          show_unlock_dialog();
      }
      e.preventDefault();
    });

  });

  // Write time to clocks
  function update_clocks(){
    var _now = new Date();
    var _time = [_now.getHours(), _now.getMinutes(), _now.getSeconds()];
    for (var i = 1; i < 3; i++) {
      if (_time[i] < 10)
        _time[i] = "0" + _time[i];
    }
    elems.clock_text.text(_time.join(':'));
  }

  function redraw(){

    clear();

    // alculate Values
    var _stage_width = elems.stage.width();
    var _stage_height = elems.stage.height();
    var _centre_x = _stage_width / 2 | 0;
    var _centre_y = _stage_height / 2 | 0;
    var _left_pad = [];

    // Draw Hexagons
    (function(){

      fill_screen_with_hexagons();

      // Draw left pad
      _left_pad.forEach(function(props){
        draw_hexagon(props, "pad");
      });

    })();

    function fill_screen_with_hexagons(){
        // Work out the min q value
        var _min_q = 0;
        while(compute_offset(_min_q, 0).x > -_half_w)
          _min_q --;

        for(var _q = _min_q; compute_offset(_q, 0).x < _stage_width + _half_w; _q++){
            // Work out the min r value for this q
            var _min_r = 0;
            while(compute_offset(_q, _min_r).y > -_half_h)
              _min_r --;

            for(var _r = _min_r; compute_offset(_q, _r).y < _stage_height + _half_h; _r++)
              draw_hexagon_as_required(_q, _r);

            if(_q === _min_q + 3 ){
              add_pad_area(_q, _r);
            }

        }
    }

    function add_pad_area(q, r){
      var _pad_q = q;
      var _pad_r = r - 2;
      var _switched = false;
      if(compute_offset(_pad_q, _pad_r).y > _stage_height - C.HEX_HEIGHT){
        _pad_q ++;
        _pad_r --;
        _switched = true;
      }

      _left_pad = [
        compute_offset(_pad_q, _pad_r),
        compute_offset(_pad_q, _pad_r - 1)
      ];
      if(!_switched){
        _left_pad.push(compute_offset(_pad_q + 1, _pad_r - 1));
        _left_pad.push(compute_offset(_pad_q + 1, _pad_r - 2));
      } else {
        _left_pad.push(compute_offset(_pad_q - 1, _pad_r));
        _left_pad.push(compute_offset(_pad_q - 1, _pad_r - 1));
      }
    }

    function compute_offset(q, r){
      return {
        x: _centre_x + q * _x_step,
        y: _centre_y + q * _y_step + r * C.HEX_HEIGHT
      };
    }

    function draw_hexagon_as_required(q, r){
      var _class = null;

      // Centre hexagons
      if(
        q === -2 && (r === 0 || r === 1 || r === 2) ||
        q === -1 && (r === -1 || r === 0 || r === 1 || r === 2) ||
        q === 0 && (r === -2 || r === -1 || r === 0 || r === 1 || r === 2) ||
        q === 1 && (r === -2 || r === -1 || r === 0 || r === 1) ||
        q === 2 && (r === -2 || r === -1 || r === 0))
        _class = "centre";

      var $hex = draw_hexagon(compute_offset(q, r), _class);
    }

  }

  function draw_hexagon(props, cls){
    var _x = props.x;
    var _y = props.y;

    // X Values
    var _x0 = _x - _half_w;
    var _x1 = _x - _quater_w;
    var _x2 = _x + _quater_w;
    var _x3 = _x + _half_w;

    // Y Values
    var _y0 = _y - _half_h;
    var _y1 = _y;
    var _y2 = _y + _half_h;

    var _points = _x3 + ',' + _y1 + ' ' + _x2 + ',' + _y2 + ' ' + _x1 + ',' + _y2 + ' ' + _x0 + ',' + _y1 + ' ' + _x1 + ',' + _y0 + ' ' + _x2 + ',' + _y0;

    var $polygon = $(createNSElem('polygon'));
    var _class = cls === null ? 'hex-polygon' : ('hex-polygon ' + cls);
    $polygon.attr('class', _class);
    $polygon.attr('points', _points);

    elems.hex_background.append($polygon);
    return $polygon;
  }

  function createNSElem(tag){
    return document.createElementNS(C.SVG_NAMESPACE, tag);
  }

  function clear(){
    elems.hex_background.html('');
  }

  function add_touch_with_overlay(touch, template){
    var $touch_point = touch_overlay_templates[template].clone();
    $touch_point.appendTo(elems.touch_overlays);
    set_position_to_touch($touch_point, touch);
    _touch_state.touches.set(touch.identifier, {
      touch: touch,
      has_overlay: true,
      overlay: $touch_point
    });
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

    _touch_state.touches.forEach(function(t){
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
    _touch_state.touches.forEach(function(t){
      // replace overlay with lock-finger
      close_and_delete(t.overlay);
      var $touch_point = touch_overlay_templates["lock-finger"].clone();
      $touch_point.appendTo(elems.lock_underlays);
      set_position_to_touch($touch_point, t.touch);
      t.overlay = $touch_point;
    });
  }

  function show_unlock_dialog(){
    if(_lock_state.state === C.ENUMS.LOCK_STATE.DIALOG)
      return;

    _lock_state.state = C.ENUMS.LOCK_STATE.DIALOG

    _lock_state.dialog = elems.lock_dialog_template.clone();
    _lock_state.dialog.appendTo(elems.interaction);
    _lock_state.dialog.css({
      top: _lock_state.y,
      left: _lock_state.x
    });
  }

  function close_unlock_dialog(){
    if(_lock_state.state !== C.ENUMS.LOCK_STATE.DIALOG)
      return;

    _lock_state.state = C.ENUMS.LOCK_STATE.NONE;
    close_and_delete(_lock_state.dialog);
    _lock_state.dialog = null;
  }

  function set_position_to_touch($elem, touch){
    $elem.css({
      top: touch.clientY,
      left: touch.clientX
    });
  }

  function close_and_delete($elem){
    $elem.addClass('close');
    setTimeout(function(){
      $elem.remove();
    }, 1000);
  }

  function is_over(touch, $elem, padding){
    if(padding === undefined)
      padding = 0;

    var _offset = $elem.offset();
    var _w = $elem.outerWidth();
    var _h = $elem.outerHeight();

    return (
      touch.clientX >= _offset.left - padding &&
      touch.clientX <= _offset.left + _w + padding &&
      touch.clientY >= _offset.top - padding &&
      touch.clientY <= _offset.top + _h + padding);
  }


});
