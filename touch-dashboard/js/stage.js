define(['constants', 'hex', 'util'], function(C, hex, util){

  var _elems,
      _on_redraw_listeners = [];

  // cached number values
  var _x_step = C.HEX_WIDTH / 4 * 3 | 0;
  var _y_step = C.HEX_HEIGHT / 2 | 0;
  var _half_w = C.HEX_WIDTH / 2 | 0;
  var _quater_w = C.HEX_WIDTH / 4 | 0;
  var _half_h = C.HEX_HEIGHT / 2 | 0;

  var _centre_x;
  var _centre_y;

  function init(elems, $window){
    _elems = elems;

    $window.resize(redraw);
    redraw();
  }

  function redraw(){

    clear();

    // calculate Values
    var _stage_width = _elems.stage.width();
    var _stage_height = _elems.stage.height();
    var _left_pad = [];

    _centre_x = _stage_width / 2 | 0;
    _centre_y = _stage_height / 2 | 0;

    // Draw Hexagons
    (function(){

      fill_screen_with_hexagons();

    })();

    function fill_screen_with_hexagons(){
        // Work out the min q value
        var _min_q = 0;
        while(compute_hex_offset(_min_q, 0).x > -_half_w)
          _min_q --;

        for(var _q = _min_q; compute_hex_offset(_q, 0).x < _stage_width + _half_w; _q++){
            // Work out the min r value for this q
            var _min_r = 0;
            while(compute_hex_offset(_q, _min_r).y > -_half_h)
              _min_r --;

            for(var _r = _min_r; compute_hex_offset(_q, _r).y < _stage_height + _half_h; _r++)
              draw_hexagon_as_required(_q, _r);

        }
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

      var $hex = hex.draw_hexagon(compute_hex_offset(q, r), _class);
      _elems.hex_background.append($hex);
    }

    _on_redraw_listeners.forEach(function(listener){
      listener();
    });

  }

  function clear(){
    _elems.hex_background.html('');
    _elems.hex_overlays.html('');
  }

  function add_redraw_listener(listener){
    _on_redraw_listeners.push(listener);
  }

  function compute_hex_offset(q, r){
    return {
      x: _centre_x + q * _x_step,
      y: _centre_y + q * _y_step + r * C.HEX_HEIGHT
    };
  }

  function pixel_to_hex(x, y){
    x -= _centre_x;
    y -= _centre_y;
    q = x * 4/ 3 / C.HEX_WIDTH;
    r = (-x / 3 + Math.sqrt(3)/3 * y) / C.HEX_WIDTH * 2;
    return {
      q: Math.round(q),
      r: Math.round(r)
    }
  }

  return {
    init: init,
    redraw: redraw,
    add_redraw_listener: add_redraw_listener,
    compute_hex_offset: compute_hex_offset,
    pixel_to_hex: pixel_to_hex
  }

});
