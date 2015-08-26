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

  return {
    is_over: is_over,
    createSvgElem: createSvgElem,
    close_and_delete: close_and_delete,
    set_position_to_touch: set_position_to_touch
  }

});
