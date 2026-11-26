package com.typeomega.lib.ui.tool;

import com.lowdragmc.lowdraglib.gui.widget.Widget;

import net.minecraft.network.FriendlyByteBuf;

import java.util.function.BooleanSupplier;

/**
 * An invisible widget that carries one server-side fact to the client (a job is running, a link is online) so
 * client-drawn controls can look disabled. The server evaluates the supplier; LDLib sends it when it changes.
 * <p>
 * EN: server side {@link #isOn()} reads the supplier directly, so a click is never judged on a stale value.
 * PT: no servidor {@link #isOn()} lê o supplier direto, então um clique nunca é julgado com valor velho.
 * ZH: 服务端的 {@link #isOn()} 直接读取 supplier，点击不会基于过期的值判断。
 */
public final class SyncedFlag extends Widget {

    private final BooleanSupplier source;
    private boolean value;

    public SyncedFlag(BooleanSupplier source) {
        super(0, 0, 0, 0);
        this.source = source;
    }

    public boolean isOn() {
        return isRemote() ? value : source.getAsBoolean();
    }

    @Override
    public void writeInitialData(FriendlyByteBuf buffer) {
        super.writeInitialData(buffer);
        value = source.getAsBoolean();
        buffer.writeBoolean(value);
    }

    @Override
    public void readInitialData(FriendlyByteBuf buffer) {
        super.readInitialData(buffer);
        value = buffer.readBoolean();
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        boolean now = source.getAsBoolean();
        if (now != value) {
            value = now;
            writeUpdateInfo(1, buf -> buf.writeBoolean(now));
        }
    }

    @Override
    public void readUpdateInfo(int id, FriendlyByteBuf buffer) {
        if (id == 1) value = buffer.readBoolean();
        else super.readUpdateInfo(id, buffer);
    }
}
