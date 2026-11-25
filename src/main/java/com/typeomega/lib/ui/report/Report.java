package com.typeomega.lib.ui.report;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * A long result of a tool, shown in {@link ReportScreen}: groups of lines, each group with a colour, each line
 * optionally tied to a block position (click copies it). Built on the server, sent in one packet; the line count is
 * capped so a giant structure cannot build a giant packet.
 */
public record Report(Component title, List<Group> groups) {

    /** Lines kept per report; the rest are counted in the group header only. */
    public static final int MAX_LINES = 4096;

    public record Line(Component text, @Nullable BlockPos pos) {}

    /** {@code total} counts every line found, {@code lines} holds the ones kept. */
    public record Group(Component name, int colour, int total, List<Line> lines) {}

    public int lineCount() {
        return groups.stream().mapToInt(g -> g.lines.size()).sum();
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeComponent(title);
        buf.writeVarInt(groups.size());
        for (Group group : groups) {
            buf.writeComponent(group.name);
            buf.writeInt(group.colour);
            buf.writeVarInt(group.total);
            buf.writeVarInt(group.lines.size());
            for (Line line : group.lines) {
                buf.writeComponent(line.text);
                buf.writeBoolean(line.pos != null);
                if (line.pos != null) buf.writeBlockPos(line.pos);
            }
        }
    }

    public static Report read(FriendlyByteBuf buf) {
        Component title = buf.readComponent();
        int n = buf.readVarInt();
        List<Group> groups = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            Component name = buf.readComponent();
            int colour = buf.readInt(), total = buf.readVarInt(), count = buf.readVarInt();
            List<Line> lines = new ArrayList<>(count);
            for (int j = 0; j < count; j++) {
                Component text = buf.readComponent();
                lines.add(new Line(text, buf.readBoolean() ? buf.readBlockPos() : null));
            }
            groups.add(new Group(name, colour, total, List.copyOf(lines)));
        }
        return new Report(title, List.copyOf(groups));
    }

    public static Builder builder(Component title) {
        return new Builder(title);
    }

    public static final class Builder {

        private final Component title;
        private final List<Group> groups = new ArrayList<>();
        private int kept;

        private Builder(Component title) {
            this.title = title;
        }

        /** Adds a group; lines past {@link #MAX_LINES} over the whole report are dropped, the total still counts. */
        public Builder group(Component name, int colour, List<Line> lines) {
            int room = Math.max(0, MAX_LINES - kept);
            List<Line> keep = lines.size() <= room ? lines : lines.subList(0, room);
            kept += keep.size();
            groups.add(new Group(name, colour, lines.size(), List.copyOf(keep)));
            return this;
        }

        public Report build() {
            return new Report(title, List.copyOf(groups));
        }
    }
}
