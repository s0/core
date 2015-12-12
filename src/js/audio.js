define([], function(){

  var _audio = {
    beep1: audio_pool("audio/freesound/123105__dj-chronos__gui-2.wav", 5, 0.2, 0),
    beep2: audio_pool("audio/freesound/123112__dj-chronos__gui-8.wav", 5, 0.2, 0.1),
    beep3: audio_pool("audio/freesound/220206__gameaudio__beep-space-button.wav", 5, 0.6, 0),

    ready1: audio_pool("audio/freesound/220172__gameaudio__flourish-spacey-2.wav", 1, 0.6, 0),
    ready2: audio_pool("audio/lcars/201.wav", 1, 0.2, 0),

    close1: audio_pool("audio/freesound/264763__farpro__guiclick2.ogg", 1, 0.6, 0),
  };

  function play(sound){
    _audio[sound].play();
  }

  function audio_pool(file, count, volume, time){

    var _pool = [];
    for(var i = 0; i < count; i++){
      var _audio = new Audio(file);
      _audio.volume = volume;
      _audio.currentTime = time;
      _pool.push(_audio);
    }
    var _j = 0;

    return {
      play: function(){
        _pool[_j].currentTime = time;
        _pool[_j].play();
        _j++;
        if(_j >= _pool.length)
          _j = 0;
      }
    }

  }

  return {
    play: play
  }
});
