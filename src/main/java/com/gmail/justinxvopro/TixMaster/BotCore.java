package com.gmail.justinxvopro.TixMaster;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import javax.security.auth.login.LoginException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gmail.justinxvopro.TixMaster.menusystem.MenuManager;
import com.gmail.justinxvopro.TixMaster.model.Config;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;

public class BotCore {
    private static String TOKEN;
    private final static Logger LOGGER = LoggerFactory.getLogger(BotCore.class);
    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    public static JDA BOT_JDA;
    public static final MenuManager MENU_MANAGER = new MenuManager();

    public static void main(String args[]) {
	File configFile = new File("config.json");
	if (!configFile.exists()) {
	    try {
		Files.copy(BotCore.class.getResourceAsStream("/config.json"), configFile.toPath());
	    } catch (IOException e) {
		LOGGER.error("Unable to create config.json " + e.getMessage());
		return;
	    }
	}
	
	try {
	    Config.loadConfig(configFile);
	} catch (IOException e1) {
	    e1.printStackTrace();
	    LOGGER.error("Unable to load config " + e1.getMessage());
	}
	
	if (args.length >= 2 && args[0].equalsIgnoreCase("-token")) {
	    LOGGER.info("Detected -token arguments using token provided");
	    TOKEN = args[1];
	} else {
	    LOGGER.info("Using config.json token");
	    TOKEN = Config.getInstance().getToken();
	}

	try {
	    BOT_JDA = new JDABuilder().addEventListeners(new CommandListener(), BotCore.MENU_MANAGER, new EventsListener(), new CreateTicketReactionListener()).setToken(TOKEN).build();
	    LOGGER.info(BOT_JDA.getInviteUrl(Permission.MANAGE_EMOTES, Permission.MESSAGE_MANAGE, Permission.MANAGE_CHANNEL, Permission.MANAGE_PERMISSIONS, Permission.MESSAGE_ADD_REACTION, Permission.MANAGE_ROLES));
	} catch (LoginException e) {
	    e.printStackTrace();
	    LOGGER.error("Unable to login: " + e.getMessage());
	}
    }
}
