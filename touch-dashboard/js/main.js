require(['buttons', 'clocks', 'constants', 'lock', 'server', 'stage', 'touch', 'util'],
  function(buttons, clocks, C, lock, server, stage, touch, util){

  var _state = {
    mode: C.ENUMS.MODE.LOCKED,
    touch: {
      touches: new Map(),
      last_touches: new Map()
    }
  };

  var _elems = {
    templates: null,
    lock_dialog_template: null,
    stage: null,
    hex_background: null,
    interaction: null,
    touch_overlays: null,
    lock_underlays: null,
    clock_text: null,
    touch_overlay_templates: {}
  };

  $(document).ready(function(){

    // Collect Elements
    _elems.templates = $('#templates');
    _elems.stage = $('#stage');
    _elems.hex_background = _elems.stage.children('.hex-background:first');
    _elems.interaction = _elems.stage.children('.interaction:first');
    _elems.lock_underlays = _elems.interaction.children('.lock-underlays:first');
    _elems.touch_overlays = _elems.interaction.children('.touch-overlays:first');
    _elems.clock_text = $('.clock .text');

    // Collect Templates
    _elems.templates.children('.touch-overlays').children().each(function(){
      var $this = $(this);
      _elems.touch_overlay_templates[$this.data('overlay')] = $this;
    });

    // Initialise Modules
    stage.init(_elems, $(window));
    clocks.init(_elems);
    lock.init(_state, _elems);
    touch.init(_state, _elems, $(window));
    buttons.init(_state, _elems);
    server.init();

  });


});
