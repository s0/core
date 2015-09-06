define(['util'], function(util){

  var _combination_lock_template = $('#templates .lock-dialog:first');

  function create(){

    var $lock = _combination_lock_template.clone();

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
