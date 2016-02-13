define([], function(){

  function create_button_icon(icon){
    var $icon = $('<span/>');
    $icon.addClass('glyphicon').addClass('glyphicon-' + icon);
    $icon.addClass('button');
    return $icon;
  }

  return {
    create_button_icon: create_button_icon
  }

});
