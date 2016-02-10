define(['audio', 'constants', 'hex', 'stage'], function(audio, C, hex, stage){

  var _state,
      _elems,
      _buttons;

  function init(state, elems){
    _state = state;
    _elems = elems;

    stage.add_redraw_listener(on_stage_redraw);
    draw_buttons();
  }

  function on_stage_redraw(){
    draw_buttons();
  }

  function draw_buttons(){
    _buttons = {};

    // calculate bottom-right corner coordinates
    var _stage_width = _elems.stage.width();
    var _stage_height = _elems.stage.height();
    var _padding_x = C.HEX_WIDTH * 1.3 | 0;
    var _padding_y = C.HEX_HEIGHT * 1.5 | 0;

    var _max_q = 0;
    while(stage.compute_hex_offset(_max_q, 0).x < _stage_width - _padding_x)
      _max_q ++;

    var _max_r = 0;
    while(stage.compute_hex_offset(_max_q, _max_r).y < _stage_height - _padding_y)
      _max_r ++;

    add_hex_button(_max_q, _max_r, function(){
      console.log("button pressed");
    });
  }

  function add_hex_button(q, r, callback){
    var _offset = stage.compute_hex_offset(q, r);

    var $hex = hex.draw_hexagon(_offset, 'button');
    _elems.hex_background.append($hex);
    var _class_normal = $hex.attr('class');
    var _class_touching = _class_normal + ' touching';

    var $inner_hex = hex.draw_hexagon(_offset, 'button-inner');
    _elems.hex_background.append($inner_hex);
    var _inner_class_normal = $inner_hex.attr('class');
    var _inner_class_touching = _inner_class_normal + ' touching';

    var _button = {
      q: q,
      r: r,
      touching: false,
      offset: _offset,
      touchstart: function(){
        _button.touching = true;
        $hex.attr('class', _class_touching);
        $inner_hex.attr('class', _inner_class_touching);
        audio.play("beep1");
      },
      touchend: function(){
        _button.touching = false;
        $hex.attr('class', _class_normal);
        $inner_hex.attr('class', _inner_class_normal);
      },
      callback: callback
    };
    _buttons[q + ',' + r] = _button;
  }

  function get_button_by_hex(coordinates){
    return _buttons[coordinates.q + ',' + coordinates.r];
  }

  return {
    init: init,
    get_button_by_hex: get_button_by_hex
  }

});
