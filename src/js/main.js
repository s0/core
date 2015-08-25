$(document).ready(function(){

  var SVG_NAMESPACE = 'http://www.w3.org/2000/svg';
  var HEX_WIDTH = 150;
  var HEX_HEIGHT = Math.sqrt(3)/2 * HEX_WIDTH | 0;

  // cached number values
  var _x_step = HEX_WIDTH / 4 * 3 | 0;
  var _y_step = HEX_HEIGHT / 2 | 0;
  var _half_w = HEX_WIDTH / 2 | 0;
  var _quater_w = HEX_WIDTH / 4 | 0;
  var _half_h = HEX_HEIGHT / 2 | 0;

  var $stage = $('#stage');
  var $hex_background = $stage.children('.hex-background:first');
  var $interaction = $stage.children('.interaction:first');
  var $clock_text = $('.clock .text');

  var touch_overlay_templates = {};

  var _touch_state = {
    touches: {}
  }

  // Collect Templates
  $('#templates .touch-overlays:first').children().each(function(){
    var $this = $(this);
    touch_overlay_templates[$this.data('overlay')] = $this;
  });

  // Write time to clocks
  function update_clocks(){
    var _now = new Date();
    var _time = [_now.getHours(), _now.getMinutes(), _now.getSeconds()];
    for (var i = 1; i < 3; i++) {
      if (_time[i] < 10)
        _time[i] = "0" + _time[i];
    }
    $clock_text.text(_time.join(':'));
  }
  setInterval(update_clocks, 100);
  update_clocks();

  function redraw(){

    clear();

    // alculate Values
    var _stage_width = $stage.width();
    var _stage_height = $stage.height();
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
      if(compute_offset(_pad_q, _pad_r).y > _stage_height - HEX_HEIGHT){
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
        y: _centre_y + q * _y_step + r * HEX_HEIGHT
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

    $hex_background.append($polygon);
    return $polygon;
  }

  function createNSElem(tag){
    return document.createElementNS(SVG_NAMESPACE, tag);
  }

  function clear(){
    $hex_background.html('');
  }

  $(window).resize(redraw);
  redraw();

  $(window).on("touchstart", function(e){
    for(var i =0; i < e.originalEvent.changedTouches.length; i++){
      var _touch = e.originalEvent.changedTouches[i];
      var $touch_point = touch_overlay_templates["multi-" + (_touch.identifier % 5)].clone();
      $touch_point.appendTo($interaction);
      $touch_point.css({
        top: _touch.clientY,
        left: _touch.clientX
      });
      _touch_state.touches[_touch.identifier] = {
        has_overlay: true,
        overlay: $touch_point
      };
    }
    e.preventDefault();
  }).on("touchmove", function(e){
    for(var i =0; i < e.originalEvent.changedTouches.length; i++){
      var _touch = e.originalEvent.changedTouches[i];
      var _t = _touch_state.touches[_touch.identifier];
      if(_t.has_overlay){
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
      var _t = _touch_state.touches[_touch.identifier];
      if(_t.has_overlay){
        _t.overlay.remove();
      }
      delete _touch_state.touches[_touch.identifier];
    }
    // cleanup everything
    if(e.originalEvent.touches.length === 0){
      _touch_state.touches = {};
      $interaction.html('');
    }
    e.preventDefault();
  });




});
