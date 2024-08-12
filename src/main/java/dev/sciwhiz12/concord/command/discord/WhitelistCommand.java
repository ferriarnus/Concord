/*
 * Concord - Copyright (c) 2020 SciWhiz12
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package dev.sciwhiz12.concord.command.discord;

import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.mojang.authlib.GameProfile;
import dev.sciwhiz12.concord.Concord;
import dev.sciwhiz12.concord.ConcordConfig;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.requests.restaction.CommandCreateAction;
import net.minecraft.server.players.UserWhiteListEntry;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLLoader;

import java.util.Optional;


/**
 * This command takes the form:
 *  /whitelist <add|remove> <user>
 *
 * Depending on the second term, it will add or remove the specified user from the server whitelist.
 * This command is disabled on integrated servers, even if enable_integrated is specified.
 *
 * @author Curle
 */
public class WhitelistCommand extends SlashCommand {
    private static final OptionData USER_OPTION = new OptionData(OptionType.STRING, "user", "The user to change", true);
    private static final SubcommandData ADD_SUBCOMMAND = new SubcommandData("add", "Add a user to the whitelist").addOptions(USER_OPTION);
    private static final SubcommandData REMOVE_SUBCOMMAND = new SubcommandData("remove", "Remove a user from the whitelist").addOptions(USER_OPTION);

    public static WhitelistCommand INSTANCE = new WhitelistCommand();

    public WhitelistCommand() {
        setName("whitelist");
        setDescription("Add or remove a player from the server's whitelist.");
        setHelpString("Contains two subcommands; add and remove. Each takes a user argument and will add or remove the player from the whitelist respectively.");
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        var roleConfig = ConcordConfig.MODERATOR_ROLE_ID.get();
        if (!roleConfig.isEmpty()) {
            var role = Concord.BOT.getDiscord().getRoleById(roleConfig);
            // If no role, then it's non-empty and invalid; disable the command
            if (role == null) {
                event.reply("Sorry, but this command is disabled by configuration. Check the moderator_role_id option in the config.").setEphemeral(true).queue();
                return;
            } else {
                // If the member doesn't have the moderator role, then deny them the ability to use the command.
                if (!event.getMember().getRoles().contains(role)) {
                    event.reply("Sorry, but you don't have permission to use this command.").setEphemeral(true).queue();
                    return;
                }
                // Fall-through; member has the role, so they can use the command.
            }
            // Fall-through; the role is empty, so all permissions are handled by Discord.
        }

        var server = Concord.BOT.getServer();

        // Short circuit for singleplayer worlds
        if (FMLLoader.getDist() == Dist.CLIENT) {
            event.reply("Sorry, but this command is disabled on Integrated Servers").setEphemeral(true).queue();
            return;
        }

        // Figure out which subcommand we're running
        var subcommand = event.getSubcommandName();
        switch (subcommand) {
            case "add":
                var player = event.getOption(USER_OPTION.getName()).getAsString();
                var whitelist = server.getPlayerList().getWhiteList();
                Optional<GameProfile> optional = server.getProfileCache().get(player);
                var profile = optional.orElseThrow();

                if (!whitelist.isWhiteListed(profile)) {
                    UserWhiteListEntry userwhitelistentry = new UserWhiteListEntry(profile);
                    whitelist.add(userwhitelistentry);

                    event.reply("User " + player + " successfully added to the whitelist.").setEphemeral(true).queue();
                    return;
                }

                event.reply("User " + player + " is already whitelisted.").setEphemeral(true).queue();
                return;
            case "remove":
                player = event.getOption(USER_OPTION.getName()).getAsString();
                whitelist = server.getPlayerList().getWhiteList();
                optional = server.getProfileCache().get(player);
                profile = optional.orElseThrow();

                if (whitelist.isWhiteListed(profile)) {
                    whitelist.remove(profile);

                    event.reply("User " + player + " successfully removed from the whitelist.").setEphemeral(true).queue();
                    return;
                }

                event.reply("User " + player + " is not whitelisted.").setEphemeral(true).queue();
                return;
        }

        // No recognized subcommand. Fall through to a safe default.
        event.reply("Unrecognized subcommand.").setEphemeral(true).queue();

    }

    @Override
    public CommandCreateAction setup(CommandCreateAction action) {
        return action.addSubcommands(ADD_SUBCOMMAND).addSubcommands(REMOVE_SUBCOMMAND);
    }
}
