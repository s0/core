define(['constants', 'util'], function(C, util){

  function create(){

    var $lock = $('#templates .combination-lock:first').clone();
    var $segments = $lock.find('.segments-1:first');
    var $segment_one = $segments.children().first();

    // Create Segments
    var _segment_step = 360 / C.COMBINATION_LOCK_SEGMENTS;
    var _segment_size = _segment_step - C.COMBINATION_LOCK_SEGMENTS_SPACING;
    var _offset = (-_segment_size / 2);

    $segment_one.css("transform", "rotate(" + _offset + "deg)");
    $segment_one.children().css("transform", "rotate(" + _segment_size + "deg)");

    // Create others
    for(var i = 1; i < C.COMBINATION_LOCK_SEGMENTS; i++){
      $segment_one.clone().appendTo($segments).css("transform", "rotate(" + (_offset + _segment_step * i) + "deg)");
    }

    function attach($elem, x, y){
      $lock.appendTo($elem);
      $lock.css({
        top: y,
        left: x
      });
    }

    function del(){
      util.close_and_delete($lock);
    }




    return {
      attach: attach,
      del: del
    };

  }

  return {
    create: create
  };
});
