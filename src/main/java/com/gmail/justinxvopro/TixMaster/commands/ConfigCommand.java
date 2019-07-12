package com.gmail.justinxvopro.TixMaster.commands;

import java.io.File;
import java.io.IOException;

import com.gmail.justinxvopro.TixMaster.model.Config;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class ConfigCommand implements Command {

    @Override
    public boolean execute(MessageReceivedEvent e, String[] args) {
	if(e.getMember().hasPermission(Permission.ADMINISTRATOR)) {
	    e.getTextChannel().sendMessage("Reloading config. . .").queue(msg -> {
		try {
		    Config.loadConfig(new File("config.json"));
		} catch (IOException e1) {
		    e1.printStackTrace();
		    msg.editMessage("Failed to reload config " + e1.getMessage());
		    return;
		}
		msg.editMessage("Reloaded config!").queue();
	    });
	}
	return true;
    }

    @Override
    public String getCommand() {
	return "config";
    }

    @Override
    public String getDescription() {
	return "updates config";
    }

    @Override
    public String[] getAlias() {
	return new String[] {"reload"};
    }

    @Override
    public String getCategory() {
	return "admin";
    }

}
