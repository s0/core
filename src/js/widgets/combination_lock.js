define(['constants', 'util'], function(C, util){

  function create(){

    var $lock = $('#templates .combination-lock:first').clone();
    var $segments = $lock.find('.segments-1:first');
    var $segment_one = $segments.children().first();

    create_segments($lock.find('.segments-1:first'), C.COMBINATION_LOCK.SEGMENTS, C.COMBINATION_LOCK.SEGMENTS_SPACING)

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
      del: del
    };

  }

  return {
    create: create
  };
});
