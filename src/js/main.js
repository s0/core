$(document).ready(function(){

  var SVG_NAMESPACE = 'http://www.w3.org/2000/svg';
  var HEX_WIDTH = 300;
  var HEX_HEIGHT = Math.sqrt(3)/2 * HEX_WIDTH;

  var $templates = $('#templates');
  var $hex = $templates.children('.hex polygon');

  var $stage = $('#stage');
  var $hex_background = $stage.children('.hex-background:first');

  function redraw(){

    clear();

    var _stage_width = $stage.width();
    var _stage_height = $stage.height();
    var _centre_x = _stage_width / 2 | 0;
    var _centre_y = _stage_height / 2 | 0;

    // Draw Hexagons
    (function(){
      var $h = $hex.clone();

      draw_hexagon();
    })();


  }

  function draw_hexagon(x, y){
    var $polygon = $(createNSElem('polygon'));
    $polygon.attr('class', 'hex-polygon');
    $polygon.attr('points', '310,140 235,270 85,270 10,140 85,10 235,10');

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
