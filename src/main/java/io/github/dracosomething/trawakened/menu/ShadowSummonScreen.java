package io.github.dracosomething.trawakened.menu;

import com.github.manasmods.manascore.api.skills.ManasSkillInstance;
import com.github.manasmods.manascore.api.skills.SkillAPI;
import com.github.manasmods.tensura.capability.ep.TensuraEPCapability;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.dracosomething.trawakened.ability.skill.ultimate.ShadowMonarch;
import io.github.dracosomething.trawakened.network.TRAwakenedNetwork;
import io.github.dracosomething.trawakened.network.play2server.SummonShadowPacket;
import io.github.dracosomething.trawakened.registry.skillRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;

import java.util.*;
import java.util.stream.Collectors;

public class ShadowSummonScreen extends Screen {
    private final ShadowMonarch skill;
    private final Map<String, List<ShadowData>> groupedShadows;
    private final List<String> selectedShadows;
    private final Map<String, Integer> summonQuantities;
    private final Map<String, Boolean> sortAscending;
    
    private Button summonButton;
    private int scrollOffset = 0;
    private int maxVisiblePerColumn;
    private int columns;
    private int maxVisibleTotal;

    public ShadowSummonScreen(ShadowMonarch skill, ManasSkillInstance instance) {
        super(Component.translatable("trawakened.screen.shadow_summon.title"));
        this.skill = skill;
        this.selectedShadows = new ArrayList<>();
        this.summonQuantities = new HashMap<>();
        this.sortAscending = new HashMap<>();
        this.groupedShadows = groupShadowsByName();
    }

    private Map<String, List<ShadowData>> groupShadowsByName() {
        Map<String, List<ShadowData>> grouped = new HashMap<>();
        CompoundTag storage = skill.getShadowStorage();
        
        for (String key : storage.getAllKeys()) {
            // Debug log removed for production
            CompoundTag shadowData = storage.getCompound(key);
            String entityType = shadowData.getString("entityType");
            String name = shadowData.getString("name");
            double ep = shadowData.getDouble("EP");
            
            if (name.isEmpty()) {
                EntityType<?> type = EntityType.byString(entityType).orElse(null);
                if (type != null) {
                    name = type.getDescription().getString();
                }
            }
            
            ShadowData data = new ShadowData(key, ep);
            grouped.computeIfAbsent(name, k -> new ArrayList<>()).add(data);
        }
        
        grouped.values().forEach(list -> list.sort(Comparator.comparingDouble(s -> s.ep)));
        
        return grouped;
    }

    @Override
    protected void init() {
        super.init();
        
        this.columns = Math.max(1, Math.min(4, this.width / 220));
        this.maxVisiblePerColumn = Math.max(5, Math.min(12, (this.height - 220) / 22)); // Reduced maximum quantity and increased reserved space
        this.maxVisibleTotal = maxVisiblePerColumn * columns;

        int centerX = this.width / 2;
        int startY = Math.max(60, this.height / 8);
        
        int availableWidth = this.width - 80;
        int buttonWidth = Math.max(140, Math.min(200, availableWidth / columns - 20));
        int buttonHeight = Math.max(16, Math.min(20, this.height / 35));
        int spacing = Math.max(18, buttonHeight + 2);
        int columnSpacing = Math.max(160, availableWidth / columns);
        
        this.clearWidgets();
        selectedShadows.clear();
        
        int index = 0;
        for (Map.Entry<String, List<ShadowData>> entry : groupedShadows.entrySet()) {
            if (index < scrollOffset) {
                index++;
                continue;
            }
            if (index - scrollOffset >= maxVisibleTotal) break;
            
            String shadowName = entry.getKey();
            List<ShadowData> shadows = entry.getValue();
            
            int displayIndex = index - scrollOffset;
            int column = displayIndex / maxVisiblePerColumn;
            int row = displayIndex % maxVisiblePerColumn;
            
            int totalColumnsWidth = columns * columnSpacing;
            int startX = centerX - totalColumnsWidth / 2 + columnSpacing / 2;
            int x = startX + (column * columnSpacing) - buttonWidth / 2;
            int y = startY + row * spacing;
            
            if (shadows.size() == 1) {
                ShadowData shadow = shadows.get(0);
                Button shadowButton = new Button(
                    x, y, buttonWidth, buttonHeight,
                    Component.literal(shadowName + " (EP: " + (int)shadow.ep + ")"),
                    button -> toggleShadowSelection(shadow.key)
                );
                
                this.addRenderableWidget(shadowButton);
            } else {
                Button shadowButton = new Button(
                    x, y, buttonWidth - 50, buttonHeight,
                    Component.literal(shadowName + " (" + shadows.size() + "x)"),
                    button -> toggleGroupSelection(shadowName)
                );
                
                this.addRenderableWidget(shadowButton);
                
                EditBox quantityBox = new EditBox(this.font, x + buttonWidth - 45, y, 
                    Math.max(18, buttonWidth / 9), buttonHeight, Component.empty());
                quantityBox.setMaxLength(3);
                quantityBox.setValue(String.valueOf(summonQuantities.getOrDefault(shadowName, 1)));
                quantityBox.setResponder(text -> {
                    try {
                        int quantity = Math.max(1, Math.min(Integer.parseInt(text), shadows.size()));
                        summonQuantities.put(shadowName, quantity);
                    } catch (NumberFormatException ignored) {
                        summonQuantities.put(shadowName, 1);
                    }
                });
                this.addRenderableWidget(quantityBox);
                
                Button sortButton = new Button(
                    x + buttonWidth - Math.max(15, buttonWidth / 12), y, 
                    Math.max(12, buttonWidth / 15), buttonHeight,
                    Component.literal(sortAscending.getOrDefault(shadowName, true) ? "↑" : "↓"),
                    button -> {
                        boolean ascending = !sortAscending.getOrDefault(shadowName, true);
                        sortAscending.put(shadowName, ascending);
                        button.setMessage(Component.literal(ascending ? "↑" : "↓"));
                    }
                );
                
                this.addRenderableWidget(sortButton);
            }
            index++;
        }
        
        // Define main button variables first
        int mainButtonWidth = Math.max(80, buttonWidth / 2);
        int mainButtonHeight = Math.max(18, buttonHeight + 2);
        int bottomMargin = Math.max(35, this.height / 15); // Further increased bottom margin
        int buttonSpacing = Math.max(5, mainButtonHeight / 3);
        int mainButtonY = this.height - bottomMargin - (mainButtonHeight * 2) - buttonSpacing;
        
        int navButtonWidth = Math.max(40, buttonWidth / 4);
        int navButtonHeight = Math.max(16, buttonHeight);
        
        int navSpacing = Math.max(20, this.height / 25); // Further increased spacing
        int navY = mainButtonY - navButtonHeight - navSpacing;
        
        if (scrollOffset > 0) {
            this.addRenderableWidget(new Button(
                centerX - navButtonWidth - 10, navY, navButtonWidth, navButtonHeight,
                Component.literal("◀ Prev"),
                button -> {
                    scrollOffset = Math.max(0, scrollOffset - maxVisibleTotal);
                    init();
                }
            ));
        }
        
        if (scrollOffset + maxVisibleTotal < groupedShadows.size()) {
            this.addRenderableWidget(new Button(
                centerX + 10, navY, navButtonWidth, navButtonHeight,
                Component.literal("Next ▶"),
                button -> {
                    scrollOffset = Math.min(groupedShadows.size() - maxVisibleTotal, scrollOffset + maxVisibleTotal);
                    init();
                }
            ));
        }
        
        summonButton = new Button(
            centerX - mainButtonWidth / 2, mainButtonY, mainButtonWidth, mainButtonHeight,
            Component.translatable("trawakened.screen.shadow_summon.summon"),
            button -> summonSelectedShadows()
        );
        
        summonButton.active = !selectedShadows.isEmpty();
        this.addRenderableWidget(summonButton);
        
        this.addRenderableWidget(new Button(
            centerX - (mainButtonWidth * 2/3) / 2, mainButtonY + mainButtonHeight + buttonSpacing, 
            mainButtonWidth * 2/3, mainButtonHeight, // Use the same height as main button
            Component.translatable("gui.cancel"),
            button -> this.onClose()
        ));
        
        updateSummonButton();
    }

    private void toggleShadowSelection(String shadowKey) {
        if (selectedShadows.contains(shadowKey)) {
            selectedShadows.remove(shadowKey);
        } else {
            selectedShadows.add(shadowKey);
        }
        updateSummonButton();
    }

    private void toggleGroupSelection(String shadowName) {
        List<ShadowData> shadows = groupedShadows.get(shadowName);
        if (shadows == null) return;
        
        boolean hasSelected = shadows.stream().anyMatch(s -> selectedShadows.contains(s.key));
        
        selectedShadows.removeIf(key -> shadows.stream().anyMatch(s -> s.key.equals(key)));
        
        if (!hasSelected) {
            int quantity = summonQuantities.getOrDefault(shadowName, 1);
            boolean ascending = sortAscending.getOrDefault(shadowName, true);
            
            List<ShadowData> sortedShadows = new ArrayList<>(shadows);
            if (ascending) {
                sortedShadows.sort(Comparator.comparingDouble(s -> s.ep));
            } else {
                sortedShadows.sort((s1, s2) -> Double.compare(s2.ep, s1.ep));
            }
            
            for (int i = 0; i < Math.min(quantity, sortedShadows.size()); i++) {
                selectedShadows.add(sortedShadows.get(i).key);
            }
        }
        
        updateSummonButton();
    }

    private void updateSummonButton() {
        if (summonButton != null) {
            summonButton.active = !selectedShadows.isEmpty();
            if (!selectedShadows.isEmpty()) {
                summonButton.setMessage(Component.literal("Summon (" + selectedShadows.size() + ")"));
            } else {
                summonButton.setMessage(Component.translatable("trawakened.screen.shadow_summon.summon"));
            }
        }
    }

    private void summonSelectedShadows() {
        if (selectedShadows.isEmpty()) return;
        
        TRAwakenedNetwork.INSTANCE.sendToServer(new SummonShadowPacket(selectedShadows));
        this.onClose();
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(poseStack);
        
        super.render(poseStack, mouseX, mouseY, partialTick);
        
        drawCenteredString(poseStack, this.font, this.title, this.width / 2, Math.max(20, this.height / 25), 0xFFFFFF);
        
        int totalShadows = groupedShadows.size();
        int currentPage = (scrollOffset / maxVisibleTotal) + 1;
        int totalPages = (int) Math.ceil((double) totalShadows / maxVisibleTotal);
        
        int subtitleY = Math.max(35, this.height / 18);
        
        if (totalPages > 1) {
            drawCenteredString(poseStack, this.font, 
                Component.literal("Page " + currentPage + "/" + totalPages + " (" + totalShadows + " types)"),
                this.width / 2, subtitleY, 0xAAAAAA);
        } else {
            drawCenteredString(poseStack, this.font, 
                Component.literal(totalShadows + " shadow types"),
                this.width / 2, subtitleY, 0xAAAAAA);
        }
        
        this.renderables.forEach(renderable -> {
            if (renderable instanceof Button button) {
                String buttonText = button.getMessage().getString();
                
                if (buttonText.contains("Summon") || buttonText.contains("Cancel") || buttonText.equals("▲") || buttonText.equals("▼") || buttonText.equals("↑") || buttonText.equals("↓")) {
                    return;
                }
                
                boolean isSelected = false;
                
                for (Map.Entry<String, List<ShadowData>> entry : groupedShadows.entrySet()) {
                    String shadowName = entry.getKey();
                    List<ShadowData> shadows = entry.getValue();
                    
                    if (buttonText.contains(shadowName)) {
                        isSelected = shadows.stream().anyMatch(s -> selectedShadows.contains(s.key));
                        break;
                    }
                }
                
                if (isSelected) {
                    fill(poseStack, button.x - 2, button.y - 2, 
                         button.x + button.getWidth() + 2, button.y + button.getHeight() + 2, 
                         0x6600FF00);
                }
            }
        });
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private static class ShadowData {
        final String key;
        final double ep;

        ShadowData(String key, double ep) {
            this.key = key;
            this.ep = ep;
        }
    }
}
