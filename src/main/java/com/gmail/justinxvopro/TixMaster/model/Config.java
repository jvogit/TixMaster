package com.gmail.justinxvopro.TixMaster.model;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.gmail.justinxvopro.TixMaster.BotCore;

import lombok.Getter;

public class Config {
    private static Config CONFIG;
    @Getter
    private String token;
    @Getter
    private String prefix;
    @Getter
    private String ticketscategory;
    @Getter
    private String[] categories;
    @Getter
    private String ticketmasterrolename;
    @Getter
    private String ticketsupportrolename;
    @Getter
    private int ticketlimit;
    @Getter
    private boolean preventspamcreate;
    @Getter
    private String createreaction;
    
    private Config() {}
    
    public static Config getInstance() {
	if(CONFIG == null) {
	    CONFIG = new Config();
	}
	
	return CONFIG;
    }
    
    public static void loadConfig(File file) throws JsonParseException, JsonMappingException, IOException {
	CONFIG = BotCore.OBJECT_MAPPER.readValue(file, Config.class);
    }
}
