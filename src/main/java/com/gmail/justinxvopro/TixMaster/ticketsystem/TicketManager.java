package com.gmail.justinxvopro.TixMaster.ticketsystem;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gmail.justinxvopro.TixMaster.BotCore;
import com.gmail.justinxvopro.TixMaster.Util;
import com.gmail.justinxvopro.TixMaster.model.Config;
import com.gmail.justinxvopro.TixMaster.model.SavedGuild;
import com.gmail.justinxvopro.TixMaster.model.SavedTicket;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;

@RequiredArgsConstructor
public class TicketManager {
    private static Map<Guild, TicketManager> managers = new HashMap<>();
    private List<Ticket> tickets = new ArrayList<>();
    @Getter
    @Setter
    private TextChannel creatorChannel;
    @Getter
    @Setter
    private Message createMessage;
    @Getter
    private TextChannel claimChannel;
    @NonNull
    private Guild guild;
    @Getter
    private Role ticketMaster;
    @Getter
    @Setter
    private Role ticketSupport;

    private static final Logger LOGGER = LoggerFactory.getLogger(TicketManager.class);

    public List<Ticket> getTickets() {
	return Collections.unmodifiableList(tickets);
    }

    public void newCreatorChannel(TextChannel text) {
	this.setCreatorChannel(text);
	try {
	    save();
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    public void newClaimChannel(TextChannel text) {
	this.setClaimChannel(text);
	try {
	    save();
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    public void setClaimChannel(TextChannel text) {
	this.claimChannel = text;
	Util.clearAndSetRole(text, this.getTicketSupport(), this.getTicketMaster())
		.putPermissionOverride(text.getGuild().getSelfMember(), Arrays.asList(Permission.MESSAGE_READ), null)
		.queue();
    }

    public void setTicketMaster(Role role) {
	guild.addRoleToMember(guild.getSelfMember(), role).queue();
	this.ticketMaster = role;
    }

    public void newTicket(Ticket ticket) {
	LOGGER.info("Add new ticket " + ticket.getId());
	addTicket(ticket);
	Category cat = guild.getCategoriesByName(Config.getInstance().getTicketscategory(), true).stream().findAny()
		.orElseGet(() -> guild.createCategory(Config.getInstance().getTicketscategory()).complete());

	cat.createTextChannel(ticket.getId()).queue(tc -> {
	    ticket.setChannel(tc);
	    Util.clearAndSetRole(tc, this.getTicketMaster(), ticket.getOriginalposter()).queue(v -> {
		tc.sendMessage(ticket.getFormatMessage()).queue(msg -> {
		    msg.getTextChannel().pinMessageById(msg.getId()).queue();
		});
		Optional.ofNullable(claimChannel).ifPresent(txc -> {
		    ConfirmBox.acceptTicket(txc, ticket);
		});
	    });
	    try {
		save();
	    } catch (IOException e) {
		e.printStackTrace();
	    }
	});
    }

    public void closeTicket(Ticket ticket) {
	LOGGER.info("Closing ticket " + ticket.getId());
	tickets.remove(ticket);
	ticket.setClosed(true);
	Util.sendFormattedMessage(ticket.getChannel(), "Success", "Ticket is closed!", (msg) -> {
	    Util.clearAndSetRole(ticket.getChannel(), this.getTicketMaster()).queue();
	    ConfirmBox.confirm(ticket.getChannel(), "Delete the ticket", (accept) -> {
		accept.getTextChannel().delete().queue();
	    }, (deny) -> {
		deny.clearReactions().queue();
		deny.editMessage("Manual deletion required.").queue();
	    });
	});
	try {
	    save();
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    public Optional<Ticket> findTicketById(String byId) {
	return tickets.stream().filter(ticket -> ticket.getId().equals(byId)).findAny();
    }

    public Optional<Ticket> findTicketByChannelId(String id) {
	return tickets.stream().filter(ticket -> ticket.getChannel().getId().equals(id)).findAny();
    }

    public static long getTicketAmount(Member op) {
	return managers.get(op.getGuild()).tickets.stream().map(Ticket::getOriginalposter).filter(op::equals).count();
    }

    public void addTicket(Ticket ticket) {
	tickets.add(ticket);
    }

    public void removeTicket(Ticket ticket) {
	tickets.remove(ticket);
    }

    public void defaultSetup() {
	Category tCategory;
	if (guild.getCategoriesByName(Config.getInstance().getTicketscategory(), true).size() == 0) {
	    tCategory = guild.createCategory(Config.getInstance().getTicketscategory()).complete();
	} else {
	    tCategory = guild.getCategoriesByName(Config.getInstance().getTicketscategory(), true).get(0);
	}
	if (this.getTicketMaster() == null) {
	    LOGGER.info("Creating ticket master role. . .");
	    this.setTicketMaster(guild.createRole().setName(Config.getInstance().getTicketmasterrolename()).complete());
	}
	if (this.getTicketSupport() == null) {
	    LOGGER.info("Creating ticket support role. . .");
	    this.setTicketSupport(
		    guild.createRole().setName(Config.getInstance().getTicketsupportrolename()).complete());
	}
	if (this.getCreatorChannel() == null) {
	    LOGGER.info("Creating ticket create channel. . .");
	    this.setCreatorChannel(tCategory.createTextChannel("ticket-create").complete());
	}
	if (this.getClaimChannel() == null) {
	    LOGGER.info("Creating ticket claim channel. . .");
	    this.setClaimChannel(tCategory.createTextChannel("ticket-claim").complete());
	}

	try {
	    save();
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    public static synchronized void save() throws IOException {
	File file = new File("saved.json");
	LOGGER.info("Saving tickets");
	if (!file.exists())
	    file.createNewFile();
	List<SavedGuild> guilds = managers.entrySet().stream()
		.map(entry -> SavedGuild.from(entry.getKey(), entry.getValue())).collect(Collectors.toList());
	BotCore.OBJECT_MAPPER.writeValue(file, guilds);
	LOGGER.info("Saved tickets");
    }

    public static void load() throws IOException {
	File file = new File("saved.json");
	LOGGER.info("Loading from saved.json");
	if (!file.exists()) {
	    Files.copy(BotCore.class.getResourceAsStream("/saved.json"), file.toPath());
	}

	Stream.of(BotCore.OBJECT_MAPPER.readValue(file, SavedGuild[].class)).forEach(sg -> {
	    Optional.ofNullable(BotCore.BOT_JDA.getGuildById(sg.getId())).ifPresent(guild -> {
		TicketManager manager = new TicketManager(guild);

		manager.claimChannel = Util.getOrNullWithMapper(sg.getClaimid(), guild::getTextChannelById);
		manager.creatorChannel = Util.getOrNullWithMapper(sg.getCreateid(), guild::getTextChannelById);
		if (manager.creatorChannel != null) {
		    manager.createMessage = Util.getOrNullWithMapper(sg.getCreatemsgid(),
			    msg -> manager.creatorChannel.retrieveMessageById(msg).complete());
		}
		manager.ticketMaster = Util.getOrNullWithMapper(sg.getTmasterid(), guild::getRoleById);
		manager.ticketSupport = Util.getOrNullWithMapper(sg.getTsid(), guild::getRoleById);
		manager.tickets = sg.getTickets().stream().map(st -> SavedTicket.toTicket(guild, st))
			.filter(tic -> tic.getChannel() != null).collect(Collectors.toList());
		if (manager.claimChannel != null) {
		    manager.tickets.stream().filter(ticket -> ticket.getAssignee() == null)
			    .forEach(t -> ConfirmBox.acceptTicket(manager.claimChannel, t));
		}
		managers.put(guild, manager);
	    });
	});
	LOGGER.info("Loaded from saved.json");
    }

    public static void init(JDA jda) {
	jda.getGuilds().forEach(g -> managers.put(g, new TicketManager(g)));
    }

    public static TicketManager getManager(Guild g) {
	return managers.get(g);
    }
}
