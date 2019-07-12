package com.gmail.justinxvopro.TixMaster.commands;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import com.gmail.justinxvopro.TixMaster.Loggable;
import com.gmail.justinxvopro.TixMaster.Util;
import com.gmail.justinxvopro.TixMaster.model.Config;
import com.gmail.justinxvopro.TixMaster.ticketsystem.Ticket;
import com.gmail.justinxvopro.TixMaster.ticketsystem.TicketCreator;
import com.gmail.justinxvopro.TixMaster.ticketsystem.TicketManager;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class TicketCommand implements Command, Loggable {

    @Override
    public boolean execute(MessageReceivedEvent e, String[] args) {
	TicketManager manager = TicketManager.getManager(e.getGuild());
	TextChannel channel = e.getTextChannel();
	boolean isAdmin = TicketCommand.hasPermission(e.getMember());
	if (args.length == 1) {
	    if (manager.getCreatorChannel() != null && manager.getCreatorChannel().equals(channel)) {
		if (!TicketCommand.canMakeTicket(e.getMember())) {
		    e.getMember().getUser().openPrivateChannel().queue(pc -> {
			Util.sendFormattedMessage(pc, "Error in Making Ticket",
				"You are already in prompt, or you reached the limit on open tickets!");
		    });
		} else {
		    TicketCreator.prompt(e.getMember());
		}
		e.getMessage().delete().queue();
	    } else {
		if (isAdmin) {
		    e.getTextChannel().sendMessage(this.getHelpMessage()).queue();
		} else {
		    Util.sendFormattedMessage(channel, "Error", "You are not in the correct channel!", msg -> {
			msg.delete().queueAfter(2, TimeUnit.SECONDS);
		    });
		}
	    }
	    return true;
	}
	if (args[1].equalsIgnoreCase("defaultsetup") && isAdmin) {
	    manager.defaultSetup();
	    Util.sendFormattedMessage(channel, "Success", "Ran default setup!");
	} else if (args[1].equalsIgnoreCase("createchannel") && isAdmin) {
	    manager.setCreatorChannel(channel);
	    Util.sendFormattedMessage(channel, "Success", "Set Creator Channel",
		    msg -> msg.delete().queueAfter(2, TimeUnit.SECONDS));
	} else if (args[1].equalsIgnoreCase("claimchannel") && isAdmin) {
	    manager.setClaimChannel(channel);
	    Util.sendFormattedMessage(channel, "Success", "Set Claim Channel",
		    msg -> msg.delete().queueAfter(2, TimeUnit.SECONDS));
	} else if (args[1].equalsIgnoreCase("close")) {
	    Optional<Ticket> ticket = manager.findTicketByChannelId(channel.getId());
	    if (ticket.isPresent()) {
		manager.closeTicket(ticket.get());
	    } else {
		Util.sendFormattedMessage(channel, "Failure", "This is not a ticket channel.",
			msg -> msg.delete().queueAfter(2, TimeUnit.SECONDS));
	    }
	} else if (args[1].equalsIgnoreCase("save") && isAdmin) {
	    Util.sendFormattedMessage(channel, "Ticket Persistence System", "Saving. . .", msg -> {
		try {
		    TicketManager.save();
		    msg.editMessage(Util.formatMessage("Success", "Save successful!")).queue();
		    msg.delete().queueAfter(2, TimeUnit.SECONDS);
		} catch (IOException exception) {
		    msg.editMessage(Util.formatMessage("Error", exception.getMessage())).queue();
		}
	    });
	} else {
	    if (manager.getCreatorChannel() != null && manager.getCreatorChannel().equals(channel)) {
		if (!TicketCommand.canMakeTicket(e.getMember())) {
		    e.getMember().getUser().openPrivateChannel().queue(pc -> {
			Util.sendFormattedMessage(pc, "Error in Making Ticket",
				"You are already in prompt, or you reached the limit on open tickets!");
		    });
		} else {
		    Ticket ticket = new Ticket("Other", Command.joinArguments(args));
		    ticket.setOriginalPoster(e.getMember());
		    manager.newTicket(ticket);
		}
		e.getMessage().delete().queue();
	    } else {
		Util.sendFormattedMessage(channel, "Error", "Unknown subcommand.", msg -> {
		    msg.delete().queueAfter(2, TimeUnit.SECONDS);
		});
	    }
	}

	return true;
    }

    @Override
    public String getCommand() {
	return "ticket";
    }

    @Override
    public String getDescription() {
	return "Base ticket command";
    }

    @Override
    public String[] getAlias() {
	return null;
    }

    @Override
    public String getCategory() {
	return "general";
    }

    private static boolean hasPermission(Member member) {
	return member.hasPermission(Permission.ADMINISTRATOR);
    }

    public static boolean canMakeTicket(Member member) {
	return !TicketCreator.inPrompt(member) && (hasPermission(member) || TicketManager.getTicketAmount(member) < Config.getInstance().getTicketlimit());
    }

    private Message getHelpMessage() {
	MessageBuilder mBuilder = new MessageBuilder();
	EmbedBuilder embedBuilder = new EmbedBuilder(
		Util.formatMessage("Set up Commands", "Info on subcommands").getEmbeds().get(0));

	embedBuilder.addField("!ticket defaultsetup", "One time easy default setup", false)
		.addField("!ticket createchannel", "Sets a channel to create tickets", false)
		.addField("!ticket claimchannel", "Sets a channel to claim tickets", false)
		.addField("!ticket save", "Saves tickets using Ticket Persistence System", false);

	return mBuilder.setEmbed(embedBuilder.build()).build();
    }

}
