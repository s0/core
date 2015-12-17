define(['constants'], function(C){

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

  function createSvgElem(tag){
    return document.createElementNS(C.SVG_NAMESPACE, tag);
  }

  function close_and_delete($elem){
    $elem.addClass('close');
    setTimeout(function(){
      $elem.remove();
    }, 1000);
  }

  function set_position_to_touch($elem, touch){
    $elem.css({
      top: touch.clientY,
      left: touch.clientX
    });
  }

  function compute_coordinate_bounds(collection, mapper){
    var _min_x = null;
    var _max_x = null;
    var _min_y = null;
    var _max_y = null;

    collection.forEach(function(object){
      var _c = mapper(object);
      if(_min_x === null || _c.x < _min_x)
        _min_x = _c.x;
      if(_max_x === null || _c.x > _max_x)
        _max_x = _c.x;
      if(_min_y === null || _c.y < _min_y)
        _min_y = _c.y;
      if(_max_y === null || _c.y > _max_y)
        _max_y = _c.y;
    });

    return {
      min_x: _min_x,
      max_x: _max_x,
      min_y: _min_y,
      max_y: _max_y,
    };
  }

  return {
    is_over: is_over,
    createSvgElem: createSvgElem,
    close_and_delete: close_and_delete,
    set_position_to_touch: set_position_to_touch,
    compute_coordinate_bounds: compute_coordinate_bounds
  }

});
