package com.gmail.justinxvopro.TixMaster.ticketsystem;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.gmail.justinxvopro.TixMaster.BotCore;
import com.gmail.justinxvopro.TixMaster.Util;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

@RequiredArgsConstructor
public class Ticket {
    @Setter
    private Set<Member> members = new HashSet<>();
    @Getter
    @Setter
    @NonNull
    private String category;
    @Getter
    @Setter
    @NonNull
    private String description;
    @Getter
    @Setter
    @Nullable
    private TextChannel channel;
    @Getter
    @Setter
    private String id = "ticket-" + System.currentTimeMillis();
    @Getter
    private Member originalposter;
    @Getter
    private Member assignee;
    @Getter
    @Setter
    private boolean closed = false;

    public Collection<Member> getMembers() {
	return Collections.unmodifiableCollection(members);
    }

    private void addRecepient(Member member) {
	if (member != null) {
	    members.add(member);
	    try {
		TicketManager.save();
	    } catch (IOException e) {
		e.printStackTrace();
	    }
	}
    }

    private void removeRecepient(Member member) {
	if (member == null)
	    return;
	channel.getPermissionOverride(member).delete();
	members.remove(member);
	try {
	    TicketManager.save();
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    public void setOriginalPoster(Member member) {
	if (this.originalposter != null)
	    this.removeRecepient(this.originalposter);
	this.originalposter = member;
	this.addRecepient(member);
    }

    public void setAssignee(Member member) {
	if (this.assignee != null)
	    this.removeRecepient(this.assignee);
	this.assignee = member;
	this.addRecepient(member);
    }

    public void assign(Member member) {
	setAssignee(member);
	Util.setTicketsRole(channel, member).queue();
	channel.retrievePinnedMessages().queue(lm -> {
	    if (!lm.isEmpty()) {
		if (lm.get(0).getAuthor().equals(BotCore.BOT_JDA.getSelfUser())) {
		    lm.get(0).editMessage(this.getFormatMessage()).queue();
		}
	    }
	});
    }

    public void unassign(Member member) {
	setAssignee(null);
	channel.retrievePinnedMessages().queue(lm -> {
	    if (!lm.isEmpty()) {
		if (lm.get(0).getAuthor().equals(BotCore.BOT_JDA.getSelfUser())) {
		    lm.get(0).editMessage(this.getFormatMessage()).queue();
		}
	    }
	});
    }

    public Message getFormatMessage() {
	MessageBuilder md = new MessageBuilder();
	EmbedBuilder eb = new EmbedBuilder();

	eb.setAuthor("Ticket Info " + id, null, BotCore.BOT_JDA.getSelfUser().getAvatarUrl());
	eb.addField("Category", category, false);
	eb.addField("Description", description, false);
	eb.addField("Involved", members.stream().map(Member::getEffectiveName).collect(Collectors.joining("\n")),
		false);
	eb.setFooter(
		this.assignee != null ? "Assigned: " + this.assignee.getEffectiveName() : "Waiting for assignee. . .",
		this.assignee != null ? this.assignee.getUser().getAvatarUrl() : null);

	return md.setEmbed(eb.build()).build();
    }
}
