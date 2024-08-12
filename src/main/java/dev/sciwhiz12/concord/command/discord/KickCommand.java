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
import dev.sciwhiz12.concord.Concord;
import dev.sciwhiz12.concord.ConcordConfig;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.restaction.CommandCreateAction;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLLoader;

import java.util.List;

/**
 * This command takes the form:
 *  /kick <user> [reason]
 *
 * It removes a user from the server, optionally with the specified reason.
 *
 * @author Curle
 */
public class KickCommand extends SlashCommand {
    private static final OptionData USER_OPTION = new OptionData(OptionType.STRING, "user", "The username of the Minecraft user to kick from the server", true);
    private static final OptionData REASON_OPTION = new OptionData(OptionType.STRING, "reason", "Why the user is being kicked from the server.", false);

    // Static instance.
    public static KickCommand INSTANCE = new KickCommand();

    public KickCommand() {
        setName("kick");
        setDescription("Kick a user from your Minecraft server");
        setHelpString("Remove a user from the server, optionally with a reason. The reason will be shown to the user in the disconnection screen.");
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        // Check permissions.
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

        var user = event.getOption(USER_OPTION.getName()).getAsString();
        var server = Concord.BOT.getServer();

        // Short-circuit for integrated servers.
        if (!ConcordConfig.ENABLE_INTEGRATED.get() && FMLLoader.getDist() == Dist.CLIENT) {
            event.reply("Sorry, but this command is disabled on Integrated Servers. Check the enable_integrated option in the Concord Config.").setEphemeral(true).queue();
            return;
        }

        var reasonMapping = event.getOption(REASON_OPTION.getName());

        // Check whether the user is online
        if (List.of(server.getPlayerNames()).contains(user)) {
            var player = server.getPlayerList().getPlayerByName(user);
            // If they are, kick them with the message.
            player.connection.disconnect(
                    reasonMapping == null ?
                            Component.translatable("multiplayer.disconnect.kicked") :
                            Component.literal(reasonMapping.getAsString())
            );

            // Reply to the user.
            event.reply("User " + user + " kicked successfully.").queue();
            return;
        }

        // Reply with a failure message.
        event.reply("The user " + user + " is not connected to the server.").queue();
    }

    @Override
    public CommandCreateAction setup(CommandCreateAction action) {
        return action.addOptions(USER_OPTION, REASON_OPTION);
    }
}
