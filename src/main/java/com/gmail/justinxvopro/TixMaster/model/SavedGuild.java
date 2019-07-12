package com.gmail.justinxvopro.TixMaster.model;

import java.util.List;
import java.util.stream.Collectors;

import com.gmail.justinxvopro.TixMaster.Util;
import com.gmail.justinxvopro.TixMaster.ticketsystem.TicketManager;

import lombok.Getter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;

public class SavedGuild {
    @Getter
    private String id;
    @Getter
    private String createid;
    @Getter
    private String createmsgid;
    @Getter
    private String claimid;
    @Getter
    private String tmasterid;
    @Getter
    private String tsid;
    @Getter
    private List<SavedTicket> tickets;
    
    public static SavedGuild from(Guild g, TicketManager tm) {
	SavedGuild sg = new SavedGuild();
	
	sg.id = g.getId();
	sg.createid = Util.getOrNullWithMapper(tm.getCreatorChannel(), TextChannel::getId);
	sg.claimid = Util.getOrNullWithMapper(tm.getClaimChannel(), TextChannel::getId);
	sg.createmsgid = Util.getOrNullWithMapper(tm.getCreateMessage(), Message::getId);
	sg.tmasterid = Util.getOrNullWithMapper(tm.getTicketMaster(), Role::getId);
	sg.tsid = Util.getOrNullWithMapper(tm.getTicketSupport(), Role::getId);
	sg.tickets = tm.getTickets().stream().map(SavedTicket::fromTicket).collect(Collectors.toList());
	
	return sg;
    }
}
