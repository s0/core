define(['constants', 'util'], function(C, util){

  // cached number values
  var _x_step = C.HEX_WIDTH / 4 * 3 | 0;
  var _y_step = C.HEX_HEIGHT / 2 | 0;
  var _half_w = C.HEX_WIDTH / 2 | 0;
  var _quater_w = C.HEX_WIDTH / 4 | 0;
  var _half_h = C.HEX_HEIGHT / 2 | 0;

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

    var $polygon = $(util.createSvgElem('polygon'));
    var _class = cls === null ? 'hex-polygon' : ('hex-polygon ' + cls);
    $polygon.attr('class', _class);
    $polygon.attr('points', _points);

    return $polygon;
  }

  return {
    draw_hexagon: draw_hexagon
  }

});
