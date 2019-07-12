package com.gmail.justinxvopro.TixMaster;

import java.util.stream.Stream;

import com.gmail.justinxvopro.TixMaster.commands.Command;
import com.gmail.justinxvopro.TixMaster.commands.ConfigCommand;
import com.gmail.justinxvopro.TixMaster.commands.HelpCommand;
import com.gmail.justinxvopro.TixMaster.commands.TicketCommand;
import com.gmail.justinxvopro.TixMaster.model.Config;
import com.gmail.justinxvopro.TixMaster.ticketsystem.TicketManager;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class CommandListener extends ListenerAdapter {
    public static final Command[] COMMAND_LIST = {
	    new HelpCommand(),
	    new ConfigCommand(),
	    new TicketCommand()
    };
    
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
	Message msg = event.getMessage();
	
	if(!msg.isFromType(ChannelType.TEXT) || msg.getContentRaw().isEmpty() || !msg.getContentRaw().startsWith(Config.getInstance().getPrefix())) {
	    protectSpam(event);
	    return;
	}
	
	String[] split = event.getMessage().getContentRaw().substring(Config.getInstance().getPrefix().length(), event.getMessage().getContentRaw().length()).split("\\s+");
	String commandBase = split[0];
	
	Stream.of(COMMAND_LIST)
	.filter(c -> c.getCommand().equalsIgnoreCase(commandBase) || c.getAliasAsList().stream().anyMatch(a->commandBase.equalsIgnoreCase(a)))
	.forEach(c -> c.execute(event, split));
	
	protectSpam(event);
    }
    
    public static void protectSpam(MessageReceivedEvent event) {
	if(!event.getMessage().isFromType(ChannelType.TEXT))
	    return;
	TicketManager manager = TicketManager.getManager(event.getGuild());
	if(!event.getMember().hasPermission(Permission.ADMINISTRATOR) && manager.getCreatorChannel() != null  && Config.getInstance().isPreventspamcreate() && event.getChannel().equals(manager.getCreatorChannel())) {
	    event.getMessage().delete().queue(msg->{}, thro->{});
	}
    }
    
}