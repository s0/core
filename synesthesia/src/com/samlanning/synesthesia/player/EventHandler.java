package com.samlanning.synesthesia.player;

public interface EventHandler<Event> {
    public void started();
    public void stopped();
    public void handle(Event event);
}