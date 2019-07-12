package com.gmail.justinxvopro.TixMaster.ticketsystem;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.gmail.justinxvopro.TixMaster.BotCore;
import com.gmail.justinxvopro.TixMaster.Util;
import com.gmail.justinxvopro.TixMaster.model.Config;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

@RequiredArgsConstructor
public class TicketCreator extends ListenerAdapter {
    private static Map<PrivateChannel, TicketCreator> mapped = new HashMap<>();
    @NonNull
    private Member guildMember;
    @NonNull
    private PrivateChannel pc;
    private String category;
    private String description;
    private Consumer<Message> onInput;

    public void start() {
	register();
	this.chooseCategory();
    }
    
    public void finish() {
	unregister();
	Ticket ticket = new Ticket(category, description);
	ticket.setOriginalPoster(guildMember);
	Util.sendFormattedMessage(pc, "Success", "Your ticket was created #" + ticket.getId() + " at " + guildMember.getGuild().getName());
	TicketManager.getManager(guildMember.getGuild()).newTicket(ticket);
	mapped.remove(pc);
    }
    
    public void register() {
	BotCore.BOT_JDA.addEventListener(this);
    }
    
    public void unregister() {
	BotCore.BOT_JDA.removeEventListener(this);
    }
    
    public void chooseCategory() {
	String[] validCategories = Config.getInstance().getCategories();
	Util.sendFormattedMessage(pc, "Choose Category (Type one)",
		Stream.of(validCategories).collect(Collectors.joining("\n")), (msg) -> {
		    onInput = (m) -> {
			Optional<String> category = Stream.of(validCategories)
				.filter(cat -> cat.equalsIgnoreCase(m.getContentRaw())).findAny();
			if (category.isPresent()) {
			    onInput = null;
			    Util.sendFormattedMessage(pc, "Success", "Chosen category " + category.get());
			    this.category = category.get();
			    this.typeDescription();
			} else {
			    Util.sendFormattedMessage(pc, "Error", "That category does not exist! Please try again.");
			}
		    };
		});
    }

    public void typeDescription() {
	Util.sendFormattedMessage(pc, "Type Description", "Provide a short description", (msg)->{
	    onInput = (m)->{
		onInput = null;
		this.description = m.getContentRaw();
		this.finish();
	    };
	});
    }
    
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
	if(!event.isFromType(ChannelType.PRIVATE) || !event.getChannel().equals(pc) || !event.getAuthor().equals(guildMember.getUser()))
	    return;
	
	if(onInput != null) {
	    onInput.accept(event.getMessage());
	}
    }

    public static void prompt(Member member) {
	member.getUser().openPrivateChannel().queue(pc -> {
	    TicketCreator tc = new TicketCreator(member, pc);
	    mapped.put(pc, tc);
	    tc.start();
	});
    }
    
    public static boolean inPrompt(Member member) {
	return mapped.values().stream().map(tx -> tx.guildMember).anyMatch(member::equals);
    }
}
