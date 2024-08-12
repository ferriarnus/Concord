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
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.requests.restaction.CommandCreateAction;

/**
 * <p>Represents a primary Slash Command.</p>
 *
 * A command has optional additions:
 * <ul>
 *  <li>Sub-commands</li>
 *  <li>Options</li>
 * </ul>
 * A command also has required information:
 * <ul>
 *  <li>Name</li>
 *  <li>Description</li>
 *  <li>Help information</li>
 * </ul>
 *
 * <p>Pass this command to the CommandDispatcher for upsert.</p>
 *
 * <p>When this command is invoked by a user, all information will be passed to the execute method via the Event parameter.</p>
 *
 * @author Curle
 */
public abstract class SlashCommand {
    // The primary command string
    private String name;

    // Extra information shown to the user in the command list.
    private String description;

    // Information shown in the help command, about what this command does.
    private String helpString;

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setHelpString(String helpString) {
        this.helpString = helpString;
    }

    /**
     * @return the name of this command
     */
    public String getName() {
        return name;
    }

    /**
     * @return extra information about this command
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return a detailed description for the help command.
     */
    public String getHelpString() {
        return helpString;
    }

    /**
     * Called when a user invokes this command.
     * All options and extra information is provided via the event parameter.
     * @param event the SlashCommandEvent for extra information about the command invocation
     */
    public abstract void execute(SlashCommandInteractionEvent event);

    /**
     * Add extra things to the command - such as sub-commands and options.
     * @param action the action that represents this command
     * @return the modified action to submit
     */
    public abstract CommandCreateAction setup(CommandCreateAction action);

}
