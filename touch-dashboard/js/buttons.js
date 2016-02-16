define(['audio', 'constants', 'hex', 'icons', 'listeners', 'media', 'server', 'stage'],
  function(audio, C, hex, icons, listeners, media, server, stage){

  var _state,
      _elems,
      _buttons,
      _button_listeners = listeners.new_group();

  function init(state, elems){
    _state = state;
    _elems = elems;

    stage.add_redraw_listener(on_stage_redraw);
    draw_buttons();
  }

  function on_stage_redraw(){
    _button_listeners.clear();
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

    var _toggle_button =
        add_action_button(_max_q, _max_r - 2, 'media_toggle', 'play');

    media.add_state_listener(_button_listeners.add(function(state){
      if (state.state === 'playing') {
        _toggle_button.icon.removeClass('glyphicon-play').addClass('glyphicon-pause');
      } else {
        _toggle_button.icon.removeClass('glyphicon-pause').addClass('glyphicon-play');
      }
    }));

    add_action_button(_max_q, _max_r - 1, 'turn_off_screen', 'power');
  }

  function add_action_button(q, r, action, icon){
    return add_hex_button(q, r, function(button){
      function error(message){
        console.error("Error performing button action:", message);
        button.action_error();
      }
      server.send_request('action',
        {
          action: action
        },
        function(){},
        error);
    }, icon);
  }

  function add_hex_button(q, r, callback, icon){
    var _offset = stage.compute_hex_offset(q, r);

    var $hex = hex.draw_hexagon(_offset, 'button');
    _elems.hex_background.append($hex);
    var _class_normal = $hex.attr('class');
    var _class_touching = _class_normal + ' touching';
    var _class_error = _class_normal + ' error';

    var $inner_hex = hex.draw_hexagon(_offset, 'button-inner');
    _elems.hex_background.append($inner_hex);
    var _inner_class_normal = $inner_hex.attr('class');
    var _inner_class_touching = _inner_class_normal + ' touching';

    var $icon = null;
    if (icon !== undefined){
      var $icon = icons.create_button_icon(icon);
      $icon.css({
        top: _offset.y,
        left: _offset.x
      });
      $icon.appendTo(_elems.hex_overlays);
    }

    var _error_timeout;

    var _button = {
      q: q,
      r: r,
      touching: false,
      offset: _offset,
      icon: $icon,
      touchstart: function(){
        clearTimeout(_error_timeout);
        _button.touching = true;
        $hex.attr('class', _class_touching);
        $inner_hex.attr('class', _inner_class_touching);
        if($icon !== null)
          $icon.addClass('touching');
        audio.play("beep1");
      },
      touchend: function(){
        clearTimeout(_error_timeout);
        _button.touching = false;
        $hex.attr('class', _class_normal);
        $inner_hex.attr('class', _inner_class_normal);
        if($icon !== null)
          $icon.removeClass('touching');
      },
      action_error: function(){
        var _old_class = $hex.attr('class');
        $hex.attr('class', _class_error);
        _error_timeout = setTimeout(function(){
          $hex.attr('class', _old_class);
        }, 1000);
      },
      callback: function(){
        callback(_button);
      }
    };
    _buttons[q + ',' + r] = _button;
    return _button;
  }

  function get_button_by_hex(coordinates){
    return _buttons[coordinates.q + ',' + coordinates.r];
  }

  return {
    init: init,
    get_button_by_hex: get_button_by_hex
  }

});
