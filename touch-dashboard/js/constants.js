define([], function(){
  'use strict';

  var SVG_NAMESPACE = 'http://www.w3.org/2000/svg';
  var HEX_WIDTH = 150;
  var HEX_HEIGHT = Math.sqrt(3)/2 * HEX_WIDTH | 0;

  var SERVER = {
    WS_URL: "ws://localhost:10001/"
  };

  var COMBINATION_LOCK = {
    SEGMENTS: 10,
    SEGMENTS_SPACING: 5, // in degrees
    TIMEOUT: null,
    MOVE_AMNT: 20
  };

  var ENUMS = {
    MODE: {
      LOCKED: 0
    },
    LOCK_STATE: {
      NONE: 0,
      TOUCH_POINTS: 1,
      DIALOG: 2,
      INPUTTING: 3
    }
  }

  return {
    SVG_NAMESPACE: SVG_NAMESPACE,
    HEX_WIDTH: HEX_WIDTH,
    HEX_HEIGHT: HEX_HEIGHT,
    SERVER: SERVER,
    COMBINATION_LOCK: COMBINATION_LOCK,
    ENUMS: ENUMS,
  }

});
