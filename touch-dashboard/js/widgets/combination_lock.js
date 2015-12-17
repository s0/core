define(['constants', 'util'], function(C, util){

  function create(){

    var $lock = $('#templates .combination-lock:first').clone();
    var $segments = $lock.find('.segments-1:first');
    var $input = $lock.find('.input:first');
    var _angle = 0;

    create_segments($segments, C.COMBINATION_LOCK.SEGMENTS, C.COMBINATION_LOCK.SEGMENTS_SPACING)

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

    // Return true if the given touch is in the "touch" area
    function activate_touch_area(touch){
      if(util.is_over(touch, $input, 0)){
        $input.addClass('show');
        $lock.addClass('inputting');
        return true;
      }
      return false;
    }

    function animate(move){
      var _step = 360 / C.COMBINATION_LOCK.SEGMENTS;
      switch(move){
        case "u":
        case "l":
          _angle -= _step;
          break;
        case "d":
        case "r":
          _angle += _step;
          break;
      }
      $segments.css("transform", "rotate(" + _angle + "deg)");
    }

    function create_segments($parent, num_segments, spacing){
      var $segment_one = $parent.children().first();

      // Create Segments
      var _segment_step = 360 / num_segments;
      var _segment_size = _segment_step - spacing;
      var _offset = (-_segment_size / 2);

      $segment_one.css("transform", "rotate(" + _offset + "deg)");
      $segment_one.children().css("transform", "rotate(" + _segment_size + "deg)");

      // Create others
      for(var i = 1; i < num_segments; i++){
        $segment_one.clone().appendTo($parent).css("transform", "rotate(" + (_offset + _segment_step * i) + "deg)");
      }
    }


    return {
      attach: attach,
      del: del,
      activate_touch_area: activate_touch_area,
      animate: animate
    };

  }

  return {
    create: create
  };
});
