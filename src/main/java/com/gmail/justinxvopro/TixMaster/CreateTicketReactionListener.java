package com.gmail.justinxvopro.TixMaster;

import java.io.IOException;

import com.gmail.justinxvopro.TixMaster.commands.TicketCommand;
import com.gmail.justinxvopro.TixMaster.model.Config;
import com.gmail.justinxvopro.TixMaster.ticketsystem.TicketCreator;
import com.gmail.justinxvopro.TixMaster.ticketsystem.TicketManager;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageUpdateEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.EventListener;

public class CreateTicketReactionListener implements EventListener, Loggable {

    @Override
    public void onEvent(GenericEvent event) {
	if (event instanceof GuildMessageUpdateEvent) {
	    GuildMessageUpdateEvent gm = (GuildMessageUpdateEvent) event;
	    Message message = gm.getMessage();
	    TicketManager tm = TicketManager.getManager(gm.getGuild());
	    if (!gm.getChannel().equals(tm.getCreatorChannel())
		    || !gm.getMember().hasPermission(Permission.ADMINISTRATOR))
		return;

	    if (message.getContentRaw().endsWith("createticket")) {
		getLogger().info("Detected reaction add create channel!");
		tm.setCreateMessage(gm.getMessage());
		try {
		    TicketManager.save();
		} catch (IOException e) {
		    e.printStackTrace();
		}
		gm.getChannel().putPermissionOverride(gm.getGuild().getSelfMember())
			.setAllow(Permission.MESSAGE_ADD_REACTION).queue(v -> {
			    gm.getMessage().addReaction(Config.getInstance().getCreatereaction()).queue();
			});
	    }
	} else if (event instanceof GuildMessageReactionAddEvent) {
	    GuildMessageReactionAddEvent gr = (GuildMessageReactionAddEvent) event;
	    TicketManager tm = TicketManager.getManager(gr.getGuild());
	    if (gr.getMember().equals(gr.getGuild().getSelfMember()) || !gr.getChannel().equals(tm.getCreatorChannel()) || tm.getCreateMessage() == null
		    || !gr.getMessageId().equals(tm.getCreateMessage().getId())) {
		return;
	    }

	    if (gr.getReactionEmote().isEmoji() && gr.getReactionEmote().getAsCodepoints()
		    .equalsIgnoreCase(Config.getInstance().getCreatereaction())) {
		gr.getReaction().removeReaction(gr.getUser()).queue(v -> {
		    if (!TicketCommand.canMakeTicket(gr.getMember())) {
			gr.getMember().getUser().openPrivateChannel().queue(pc -> {
			    Util.sendFormattedMessage(pc, "Error in Making Ticket",
				    "You are already in prompt, or you reached the limit on open tickets!");
			});
		    } else {
			TicketCreator.prompt(gr.getMember());
		    }
		});
	    }
	}
    }

}
