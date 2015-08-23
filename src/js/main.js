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

  function redraw(){

    clear();

    // alculate Values
    var _stage_width = $stage.width();
    var _stage_height = $stage.height();
    var _centre_x = _stage_width / 2 | 0;
    var _centre_y = _stage_height / 2 | 0;

    // Draw Hexagons
    (function(){

      fill_screen_with_hexagons();

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
              draw_hexagon(compute_offset(_q, _r));
        }
    }

    function compute_offset(q, r){
      return {
        x: _centre_x + q * _x_step,
        y: _centre_y + q * _y_step + r * HEX_HEIGHT
      };
    }
  }

  function draw_hexagon(props){
    var _x = props.x;
    var _y = props.y;
    console.log(_x, _y);

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
    $polygon.attr('class', 'hex-polygon');
    $polygon.attr('points', _points);

    $hex_background.append($polygon);
  }

  function createNSElem(tag){
    return document.createElementNS(SVG_NAMESPACE, tag);
  }

  function clear(){
    $hex_background.html('');
  }

  $(window).resize(redraw);
  redraw();


});
