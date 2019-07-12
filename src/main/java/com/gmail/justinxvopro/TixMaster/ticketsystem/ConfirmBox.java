package com.gmail.justinxvopro.TixMaster.ticketsystem;

import java.util.function.Consumer;

import com.gmail.justinxvopro.TixMaster.BotCore;
import com.gmail.justinxvopro.TixMaster.Util;
import com.gmail.justinxvopro.TixMaster.menusystem.MenuBuilder;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

public class ConfirmBox {
    public static void confirm(TextChannel text, String initial, Consumer<Message> onAccept, Consumer<Message> onDeny) {
	MenuBuilder mBuilder = MenuBuilder.builder(text.getGuild());
	mBuilder.setMessage(Util.formatMessage("Please confirm", initial));
	if (onAccept != null) {
	    mBuilder.assign("U+2705", (param) -> {
		onAccept.accept(param.getMessage());
		return true;
	    });
	}
	if (onDeny != null) {
	    mBuilder.assign("U+274C", (param) -> {
		onDeny.accept(param.getMessage());
		return true;
	    });
	}
	BotCore.MENU_MANAGER.submit(mBuilder.build(), text);
    }
    
    public static void acceptTicket(TextChannel text, Ticket ticket) {
	MenuBuilder mBuilder = MenuBuilder.builder(text.getGuild());
	EmbedBuilder eBuilder = new EmbedBuilder(Util.formatMessage("Available Ticket", null).getEmbeds().get(0));
	
	eBuilder
	.addField("Category", ticket.getCategory(), false)
	.addField("Description", ticket.getDescription(), false);
	
	mBuilder
	.setMessage(new MessageBuilder().setEmbed(eBuilder.build()).build())
	.assign("U+2705", (param)->{
	    param.getMessage().clearReactions().queue(v -> {
		if(ticket.isClosed()) {
		    eBuilder.setFooter("This ticket has been closed!", BotCore.BOT_JDA.getSelfUser().getAvatarUrl());
		    param.getMessage().editMessage(new MessageBuilder().setEmbed(eBuilder.build()).build()).queue();
		    return;
		}
		ticket.assign(param.getMember());
		eBuilder.setFooter("Assigned to " + param.getMember().getEffectiveName(), param.getMember().getUser().getAvatarUrl());
		param.getMessage().editMessage(new MessageBuilder().setEmbed(eBuilder.build()).build()).queue();
	    });
	    return true;
	});
	
	BotCore.MENU_MANAGER.submit(mBuilder.build(), text);
    }
}
