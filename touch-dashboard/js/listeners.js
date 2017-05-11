/*
  This listener framework aims to reduce leaking / listener references lying
  around by collecting listeners into ListenerGroups that can be cleared in
  one go (removing any references to them from Listenables).
*/
define([], function(){
  'use strict';

  var _next_listener_id = 0;

  // A "group" of listeners, that should be able to be cleared at once
  function ListenerGroup(){
    this.listeners = [];
  }

  ListenerGroup.prototype.add = function(callback) {
    var _listener = new Listener(callback);
    this.listeners.push(_listener);
    return _listener;
  }

  ListenerGroup.prototype.clear = function() {
    this.listeners.forEach(function(listener){
      listener.clear();
    });
    this.listeners = [];
  }

  function Listener(callback){
    this.callback = callback;
    this.id = _next_listener_id ++;
    this.attached_to = [];
    this.cleared = false;
  }

  Listener.prototype.call = function() {
    this.callback.apply(null, arguments);
  }

  Listener.prototype.clear = function() {
    this.attached_to.forEach(function(listenable) {
      listenable.remove(this);
    }.bind(this));
    this.cleared = true;
  }

  // Something that can have listeners attached to it
  function Listenable(){
    this.listeners = {};
  }

  Listenable.prototype.add = function(listener) {
    if (!(listener instanceof Listener))
      throw new Error("listener is not a Listener");
    if (listener.cleared)
      throw new Error("listener has already been cleared");
    this.listeners[listener.id] = listener;
    listener.attached_to.push(this);
  }

  Listenable.prototype.remove = function(listener) {
    if (!(listener instanceof Listener))
      throw new Error("listener is not a Listener");
    delete this.listeners[listener.id];
  }

  Listenable.prototype.visit = function(callback) {
    $.each(this.listeners, function(id, listener) {
      callback(listener.callback);
    });
  }

  return {
    new_group: function() {
      return new ListenerGroup();
    },
    new_listenable: function() {
      return new Listenable();
    },
    single_listener: function(callback) {
      return new Listener(callback);
    }
  }

});
