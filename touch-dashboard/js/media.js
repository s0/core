define(['listeners', 'server'], function(listeners, server){
  'use strict';

  var _elems;
  var _state = null;
  var _listenable = listeners.new_listenable();

  function init(elems){
    _elems = elems;
    server.setup_listener('media', function(payload){
      _state = payload;
      _listenable.visit(function(callback) {
        callback(_state);
      });
    });
    _listenable.add(listeners.single_listener(lock_screen_info_updater));
  }

  function add_state_listener(listener) {
    _listenable.add(listener);
    if (_state !== null)
      listener.call(_state);
  }

  function lock_screen_info_updater(state){
    var $icon = _elems.media_info.find('.glyphicon'),
        $artist = _elems.media_info.find('.media-artist'),
        $title = _elems.media_info.find('.media-title'),
        $album = _elems.media_info.find('.media-album');

    // icon
    if (state.state === 'playing') {
      $icon.removeClass('glyphicon-pause').addClass('glyphicon-play');
    } else if (state.state === 'paused') {
      $icon.addClass('glyphicon-pause').removeClass('glyphicon-play');
    } else {
      $icon.removeClass('glyphicon-pause').removeClass('glyphicon-play');
    }

    if (state.state === 'playing' || state.state === 'paused') {
      $artist.text(state.artist);
      $title.text(state.title);
      $album.text(state.album);
    } else {
      $artist.text('');
      $title.text('');
      $album.text('');
    }
    console.log("update lock screen info");
  }

  return {
    init: init,
    add_state_listener: add_state_listener
  }

});
