package com.gmail.justinxvopro.TixMaster;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.IPermissionHolder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.managers.ChannelManager;

public class Util {
    public static Message formatMessage(String title, String content) {
	MessageBuilder mb = new MessageBuilder();
	EmbedBuilder eb = new EmbedBuilder();
	eb.setAuthor(BotCore.BOT_JDA.getSelfUser().getName(), null, BotCore.BOT_JDA.getSelfUser().getAvatarUrl());
	eb.setTitle(title);
	eb.setDescription(content);

	return mb.setEmbed(eb.build()).build();
    }

    public static void sendFormattedMessage(MessageChannel channel, String title, String content) {
	channel.sendMessage(formatMessage(title, content)).queue();
    }

    public static void sendFormattedMessage(MessageChannel channel, String title, String content,
	    Consumer<Message> consumer) {
	channel.sendMessage(formatMessage(title, content)).queue(consumer);
    }

    public static <T, R> R getOrNullWithMapper(T nullableobject, Function<? super T, ? extends R> mapper) {
	return Optional.ofNullable(nullableobject).map(mapper).orElse(null);
    }

    public static <T> T getOrNull(T t) {
	return Optional.ofNullable(t).orElse(null);
    }

    public static ChannelManager clearAndSetRole(TextChannel text, IPermissionHolder... to) {
	ChannelManager manager = text.getManager();
	text.getRolePermissionOverrides().forEach(role -> {
	    manager.removePermissionOverride(role.getRole());
	});
	text.getMemberPermissionOverrides().forEach(role -> {
	    manager.removePermissionOverride(role.getMember());
	});
	manager.putPermissionOverride(text.getGuild().getPublicRole(), null,
		Arrays.asList(Permission.VIEW_CHANNEL));

	if (to != null) {
	    setTicketsRole(text, to);
	}
	
	return manager;
    }
    
    public static ChannelManager setTicketsRole(TextChannel text, IPermissionHolder...roles) {
	ChannelManager manager = text.getManager();
	if (roles != null) {
	    Stream.of(roles).forEach(role -> {
		manager.putPermissionOverride(role, Arrays.asList(Permission.VIEW_CHANNEL), null);
	    });
	}
	
	return manager;
    }
}
