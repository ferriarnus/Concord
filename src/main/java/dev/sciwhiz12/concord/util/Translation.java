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

package dev.sciwhiz12.concord.util;

import dev.sciwhiz12.concord.ConcordConfig;
import dev.sciwhiz12.concord.features.ConcordFeatures;
import dev.sciwhiz12.concord.features.FeatureVersion;
import dev.sciwhiz12.concord.network.ConcordNetwork;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A message with a translation key and its corresponding default text in English ({@code en_us}).
 *
 * <p>This interface contains various utility methods for creating {@link Component} for ease of reference.
 * There are three kinds of these utility methods, with each kind having a no-arguments and a arguments-accepting
 * counterpart.</p>
 * <ul>
 *     <li>{@link #component()} for lazily translated components, whose translation key will be resolved on the client.</li>
 *     <li>{@link #eagerComponent()} for eagerly translated components, whose translation key (and those of all contained
 *     {@link Component}s) are resolved on the server.</li>
 *     <li>{@link #resolvedComponent(Entity)} and {@link #resolvedComponent(CommandSourceStack)} for components which
 *     will be either lazily or eagerly translated, depending on local configuration settings and remote mod presence.</li>
 * </ul>
 *
 * <p>This is an interface to allow hiding away its implementations and construction thereof to this package while
 * allowing the addition of utility methods. This interface is named {@code Translation} instead of {@code Message} to
 * avoid any conflict with Brigadier's {@link com.mojang.brigadier.Message} class.</p>
 *
 * @see ConcordConfig#LAZY_TRANSLATIONS
 * @see ConcordNetwork
 */
public interface Translation {
    /**
     * {@return the translation key}
     */
    String key();

    /**
     * {@return the default text of the message in English ({@code en_us})} This may contain argument specifiers as used
     * by {@link Component}. This is used by the data generation code for generating the English translation
     * files.
     */
    String englishText();

    /**
     * {@return the version this was last modified in a compatibility-breaking way} This is used by the messaging
     * subsystem alongside {@link IntelligentTranslator} to intelligently translate components.
     *
     * <p>For the purposes of compatibility checking, only the {@linkplain ArtifactVersion#getMajorVersion() major version}
     * and the {@linkplain ArtifactVersion#getMinorVersion() minor version} are considered. A version is considered
     * <em>incompatible</em> if it differs in major version or minor version.</p>
     *
     * <p>The last modified version only needs to track <em>compatibility-breaking</em> modifications, which are
     * changes that prevent previously working translations from working. For example, removing arguments breaks
     * compatibility with older versions which accept it.</p>
     *
     * <p>While the {@linkplain ArtifactVersion#getIncrementalVersion() incremental version} is unused by compatibility
     * logic, it may still be used for communicating fully-compatible changes if desired, though this is not required.</p>
     *
     * @see FeatureVersion#TRANSLATIONS
     */
    ArtifactVersion lastModifiedVersion();

    /**
     * {@return a lazily resolved component with no arguments} The translation key of this component will be resolved
     * lazily, on the client's end.
     *
     * @see #component(Object...)
     */
    default MutableComponent component() {
        // TODO: consider whether to keep the english text here as a fallback
        return Component.translatableWithFallback(key(), englishText());
    }

    /**
     * {@return a lazily resolved component with formatting arguments}
     *
     * @param formatArgs the formatting arguments for the message
     * @see #component()
     */
    default MutableComponent component(Object... formatArgs) {
        // TODO: consider whether to keep the english text here as a fallback
        return Component.translatableWithFallback(key(), englishText(), formatArgs);
    }

    /**
     * {@return an eagerly resolved component with no arguments} The translation key of this component will be resolved
     * eagerly, on the server side before transmission to the client.
     *
     * @see #eagerComponent(Object...)
     */
    default MutableComponent eagerComponent() {
        return TranslationUtil.eagerTranslate(component());
    }

    /**
     * {@return an eagerly resolved component with formatting arguments}
     *
     * @param formatArgs the formatting arguments for the message
     * @see #eagerComponent()
     */
    default MutableComponent eagerComponent(Object... formatArgs) {
        return TranslationUtil.eagerTranslate(component(formatArgs));
    }

    private boolean translateEagerly(CommandSourceStack source) {
        return !ConcordConfig.LAZY_TRANSLATIONS.get()
                || (source.getEntity() instanceof ServerPlayer player && ConcordFeatures.getOrNull(player) != null);
    }

    private boolean translateEagerly(@Nullable Entity sourceEntity) {
        return !ConcordConfig.LAZY_TRANSLATIONS.get()
                || (sourceEntity instanceof ServerPlayer player && ConcordFeatures.getOrNull(player) != null);
    }

    /**
     * {@return a component with no arguments which is either lazily or eagerly resolved} Its resolution depends on local
     * configuration settings and the remote mod presence on the given recipient entity if it is a {@link ServerPlayer}.
     *
     * @param recipientEntity the entity representing the receiver of the message
     * @see #resolvedComponent(Entity, Object...)
     * @see #resolvedComponent(CommandSourceStack)
     */
    default MutableComponent resolvedComponent(@Nullable Entity recipientEntity) {
        return translateEagerly(recipientEntity) ? eagerComponent() : component();
    }

    /**
     * {@return a component with formatting arguments which is either lazily or eagerly resolved}
     *
     * @param recipientEntity the entity representing the receiver of the message
     * @param formatArgs      the formatting arguments for the message
     * @see #resolvedComponent(Entity)
     */
    default MutableComponent resolvedComponent(@Nullable Entity recipientEntity, Object... formatArgs) {
        return translateEagerly(recipientEntity) ? eagerComponent(formatArgs) : component(formatArgs);
    }

    /**
     * {@return a component with no arguments which is either lazily or eagerly resolved} Its resolution depends on local
     * configuration settings and the remote mod presence on the given recipient if it represents a {@link ServerPlayer}.
     *
     * @param recipient the command source stack representing the recipient
     * @see #resolvedComponent(CommandSourceStack, Object...)
     * @see #resolvedComponent(Entity)
     */
    default MutableComponent resolvedComponent(CommandSourceStack recipient) {
        return translateEagerly(recipient) ? eagerComponent() : component();
    }

    /**
     * {@return a component with formatting arguments which is either lazily or eagerly resolved}
     *
     * @param recipient  the command source stack representing the recipient
     * @param formatArgs the formatting arguments for the message
     * @see #resolvedComponent(CommandSourceStack)
     */
    default MutableComponent resolvedComponent(CommandSourceStack recipient, Object... formatArgs) {
        return translateEagerly(recipient) ? eagerComponent(formatArgs) : component(formatArgs);
    }
}
