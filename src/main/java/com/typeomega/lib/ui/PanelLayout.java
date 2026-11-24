package com.typeomega.lib.ui;

import net.minecraft.resources.ResourceLocation;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Where things go on an illustrated recipe panel (GTIA-D-435): the picture of the process, the slots and tanks placed
 * inside it, and room for the text lines below, GTNH style. Plain data, so a layout is checked by unit tests and by
 * GameTests without opening a screen. Coordinates are in GUI pixels, relative to the top-left corner of the picture.
 */
public final class PanelLayout {

    /** Border of the GTCEu bevelled background around the picture. */
    public static final int PAD = 4;
    public static final int SLOT = 18;
    public static final int LINE_HEIGHT = 10;

    public enum Kind {
        ITEM,
        FLUID
    }

    public enum Role {
        INPUT,
        OUTPUT,
        /** Shown and indexed, never consumed (a target cartridge, the controller). */
        CATALYST
    }

    /** One slot; {@code index} counts slots of the same kind and role, as GTCEu numbers its recipe slots. */
    public record Slot(Kind kind, Role role, int index, int x, int y) {}

    /** A decoration placed over the picture: an icon, an arrow or a progress bar, by name. */
    public record Mark(String id, int x, int y, int width, int height) {}

    private final @Nullable ResourceLocation art;
    private final int width, height;
    private final List<Slot> slots;
    private final List<Mark> marks;

    private PanelLayout(@Nullable ResourceLocation art, int width, int height, List<Slot> slots, List<Mark> marks) {
        this.art = art;
        this.width = width;
        this.height = height;
        this.slots = List.copyOf(slots);
        this.marks = List.copyOf(marks);
    }

    public static Builder builder(int width, int height) {
        return new Builder(width, height);
    }

    public @Nullable ResourceLocation art() {
        return art;
    }

    /** Width and height of the picture. */
    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public List<Slot> slots() {
        return slots;
    }

    public List<Slot> slots(Kind kind, Role role) {
        return slots.stream().filter(s -> s.kind == kind && s.role == role).toList();
    }

    public List<Mark> marks() {
        return marks;
    }

    /** Width of the whole panel, background border included. */
    public int panelWidth() {
        return width + 2 * PAD;
    }

    /** Height of the whole panel with {@code lines} text lines under the picture. */
    public int panelHeight(int lines) {
        return linesTop() + lines * LINE_HEIGHT + PAD;
    }

    /** Y of the first text line, relative to the panel. */
    public int linesTop() {
        return PAD + height + 3;
    }

    /**
     * What is wrong with the layout: a slot or mark outside the picture, two slots on top of each other, a slot index
     * repeated. Empty when the layout is sound.
     */
    public List<String> problems() {
        List<String> out = new ArrayList<>();
        for (int i = 0; i < slots.size(); i++) {
            Slot a = slots.get(i);
            if (!inside(a.x, a.y, SLOT, SLOT)) out.add("slot " + describe(a) + " is outside the picture");
            for (int j = i + 1; j < slots.size(); j++) {
                Slot b = slots.get(j);
                if (Math.abs(a.x - b.x) < SLOT && Math.abs(a.y - b.y) < SLOT)
                    out.add("slots " + describe(a) + " and " + describe(b) + " overlap");
                if (a.kind == b.kind && a.role == b.role && a.index == b.index)
                    out.add("slot index repeated: " + describe(a));
            }
        }
        for (Mark m : marks) {
            if (!inside(m.x, m.y, m.width, m.height)) out.add("mark " + m.id + " is outside the picture");
        }
        return out;
    }

    private boolean inside(int x, int y, int w, int h) {
        return x >= 0 && y >= 0 && x + w <= width && y + h <= height;
    }

    private static String describe(Slot s) {
        return s.kind.name().toLowerCase() + "_" + s.role.name().toLowerCase() + "_" + s.index + "@" + s.x + "," + s.y;
    }

    public static final class Builder {

        private final int width, height;
        private @Nullable ResourceLocation art;
        private final List<Slot> slots = new ArrayList<>();
        private final List<Mark> marks = new ArrayList<>();
        private final Map<Kind, Map<Role, Integer>> next = new EnumMap<>(Kind.class);

        private Builder(int width, int height) {
            if (width <= 0 || height <= 0) throw new IllegalArgumentException("panel needs a positive size");
            this.width = width;
            this.height = height;
        }

        /** The picture, drawn 1:1 (one texture pixel per GUI pixel), so the PNG should be {@code width x height}. */
        public Builder art(ResourceLocation texture) {
            this.art = texture;
            return this;
        }

        public Builder item(Role role, int x, int y) {
            return slot(Kind.ITEM, role, x, y);
        }

        public Builder fluid(Role role, int x, int y) {
            return slot(Kind.FLUID, role, x, y);
        }

        public Builder slot(Kind kind, Role role, int x, int y) {
            int index = next.computeIfAbsent(kind, k -> new EnumMap<>(Role.class)).merge(role, 1, Integer::sum) - 1;
            slots.add(new Slot(kind, role, index, x, y));
            return this;
        }

        public Builder mark(String id, int x, int y, int w, int h) {
            marks.add(new Mark(id, x, y, w, h));
            return this;
        }

        public PanelLayout build() {
            return new PanelLayout(art, width, height, slots, marks);
        }
    }
}
