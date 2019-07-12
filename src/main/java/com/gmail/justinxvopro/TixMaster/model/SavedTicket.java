package com.gmail.justinxvopro.TixMaster.model;

import java.util.Collection;
import java.util.stream.Collectors;

import com.gmail.justinxvopro.TixMaster.Util;
import com.gmail.justinxvopro.TixMaster.ticketsystem.Ticket;

import lombok.Getter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

public class SavedTicket {
    @Getter
    private String id;
    @Getter
    private String channelId;
    @Getter
    private String category;
    @Getter
    private String description;
    @Getter
    private String originalposter;
    @Getter
    private String assignee;
    @Getter
    private Collection<String> memberids;
    
    public static SavedTicket fromTicket(Ticket ticket) {
	SavedTicket st = new SavedTicket();
	st.id = ticket.getId();
	st.channelId = ticket.getChannel().getId();
	st.description = ticket.getDescription();
	st.category = ticket.getCategory();
	st.originalposter = Util.getOrNullWithMapper(ticket.getOriginalposter(), Member::getId);
	st.assignee = Util.getOrNullWithMapper(ticket.getAssignee(), Member::getId);
	st.memberids = ticket.getMembers().stream().map(Member::getId).collect(Collectors.toSet());
	
	return st;
    }
    
    public static Ticket toTicket(Guild g, SavedTicket st) {
	Ticket ticket = new Ticket(st.getCategory(), st.getDescription());
	
	ticket.setId(st.getId());
	ticket.setChannel(g.getTextChannelById(st.getChannelId()));
	ticket.setAssignee(Util.getOrNullWithMapper(st.getAssignee(), g::getMemberById));
	ticket.setOriginalPoster(Util.getOrNullWithMapper(st.getOriginalposter(), g::getMemberById));
	ticket.setMembers(st.getMemberids().stream().map(mid -> g.getMemberById(mid)).filter(m -> m != null).collect(Collectors.toSet()));
	
	return ticket;
    }
}
