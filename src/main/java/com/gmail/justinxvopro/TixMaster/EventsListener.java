package com.gmail.justinxvopro.TixMaster;

import java.io.IOException;

import com.gmail.justinxvopro.TixMaster.ticketsystem.TicketManager;

import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.EventListener;

public class EventsListener implements EventListener, Loggable {

    @Override
    public void onEvent(GenericEvent event) {
	if(event instanceof ReadyEvent) {
	    TicketManager.init(event.getJDA());
	    try {
		TicketManager.load();
	    } catch (IOException e) {
		e.printStackTrace();
	    }
	}
    }
}
