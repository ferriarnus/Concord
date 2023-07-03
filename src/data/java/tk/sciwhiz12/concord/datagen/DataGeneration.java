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

package tk.sciwhiz12.concord.datagen;

import com.google.common.collect.Maps;
import net.minecraft.SharedConstants;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.metadata.PackMetadataGenerator;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import tk.sciwhiz12.concord.Concord;

import java.util.Set;

@EventBusSubscriber(modid = Concord.MODID, bus = EventBusSubscriber.Bus.MOD)
public class DataGeneration {
    @SubscribeEvent
    static void onGatherData(GatherDataEvent event) {
        final DataGenerator gen = event.getGenerator();
        final PackOutput output = gen.getPackOutput();

        gen.addProvider(event.includeClient(), new EnglishLanguage(output));
        gen.addProvider(event.includeClient() || event.includeServer(), new PackMetadataGenerator(output)
                .add(PackMetadataSection.TYPE, new PackMetadataSection(
                        Component.literal("concord resources"),
                        SharedConstants.getCurrentVersion().getPackVersion(PackType.CLIENT_RESOURCES),
                        Maps.asMap(Set.of(PackType.values()), SharedConstants.getCurrentVersion()::getPackVersion)
                )));
    }
}
