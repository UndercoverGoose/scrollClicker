package io.github.undercovergoose.scrollClicker.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.InventoryPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Date;

@Mixin(InventoryPlayer.class)
public class MixinInventoryPlayer {
    private int keyCode = Minecraft.getMinecraft().gameSettings.keyBindUseItem.getKeyCode();
    private int cps = 35;
    private int rms = 3;
    private boolean doClick = false;
    private boolean running = false;

    @Inject(method = "changeCurrentItem", at = @At("HEAD"), cancellable = true)
    public void onChangeItem(int direction, CallbackInfo ci) {
        ci.cancel();
        doClick = true;
        if(!running) start();
    }
    public void setCps(int newCps) {
        cps = newCps;
    }
    public void setCps(int newCps, int newRms) {
        cps = newCps;
        rms = newRms;
    }
    public void setKey(int newKeyCode) {
        keyCode = newKeyCode;
    }
    public void start() {
        running = true;
        new Thread(() -> {
            long lastClickTime = 0;
            int randFactor = 0;
            while(running) { // could switch to new Thread + thread.sleep(ms);
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                if(!doClick) continue;
                float lowerDelay = 1000 / (float)(cps - rms);
                if(lastClickTime == 0) {
                    KeyBinding.onTick(keyCode);
                    lastClickTime = new Date().getTime();
                    randFactor = (int)Math.floor(Math.random() * (2*rms + 1));
//                    System.out.println("[!] Clicked! Delay set to " + (lowerDelay + randFactor) + "ms or " + (1000 / (lowerDelay + randFactor)) + " cps");
                    continue;
                }
                long now = new Date().getTime();
                if(now - lastClickTime < lowerDelay + randFactor) continue;
                lastClickTime = 0;
                doClick = false;
            }
            doClick = false;
        }).start();
    }
}
