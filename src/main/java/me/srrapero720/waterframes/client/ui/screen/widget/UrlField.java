package me.srrapero720.waterframes.client.ui.screen.widget;

import me.srrapero720.waterframes.DisplaysConfig;
import me.srrapero720.waterui.theme.Drawable;
import me.srrapero720.waterui.widget.InputText;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.watermedia.api.media.MRL;
import org.watermedia.api.media.MediaAPI;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

/** URL entry that colours its border by validity and explains why it is rejected. */
public class UrlField extends InputText {

    // VALIDITY SETTLED AT MOST ONCE PER TICK: tooltip() ASKS PER HOVERED FRAME AND MRL FETCHES ARE COSTLY
    private boolean checked;
    private boolean checkedValid;

    // EMPTY ON PURPOSE: THIS FIELD ADDS A SOURCE, IT IS NOT A VIEW OF WHAT IS PLAYING
    public UrlField() {
        super("url");
        // OWN FRAME WIDTH: THE ACCENT BORDER MUST SHOW EVEN ON THEMES WHOSE ROLE HAS NO BORDER
        this.border(1);
    }

    public String url() {
        String text = this.text();
        return text == null || text.isBlank() ? null : text.trim();
    }

    public boolean valid() {
        if (!checked) {
            this.checked = true;
            this.checkedValid = this.resolve();
        }
        return checkedValid;
    }

    @Override
    public void tick() {
        super.tick();
        this.checked = false;
    }

    private boolean resolve() {
        String url = this.url();
        if (url == null || !absolute(url)) return false;
        MRL mrl = MediaAPI.mrl(url);
        return mrl != null && (mrl.status() == MRL.Status.FETCHING || mrl.status().loaded());
    }

    // ASKING THE API IS A SIDE EFFECT: IT SPAWNS A FETCH JOB PER STRING, SO HALF TYPED TEXT
    // AND SEARCH TERMS MUST NEVER REACH IT; ONLY AN ABSOLUTE URI CAN EVER RESOLVE ANYWAY
    private static boolean absolute(String url) {
        try {
            return new URI(url).isAbsolute();
        } catch (URISyntaxException e) {
            return false;
        }
    }

    // STATIC FRAME: WHETHER THE TEXT IS USABLE IS THE BUTTON'S JOB TO SAY, NOT THE FIELD'S
    @Override
    protected Drawable borderDisplay() {
        return theme().accentBorder();
    }

    @Override
    protected Drawable faceDisplay() {
        return theme().field();
    }

    @Override
    public List<Component> tooltip() {
        if (this.url() == null) {
            return List.of(Component.literal(ChatFormatting.BLUE + translate("waterframes.gui.url.tooltip.empty")));
        }
        if (valid() && !DisplaysConfig.canSave(Minecraft.getInstance().player, this.text())) {
            return List.of(Component.literal(ChatFormatting.RED + translate("waterframes.gui.url.tooltip.not_whitelisted")));
        }
        // NOTHING OF ITS OWN: FALL BACK TO THE GENERIC tooltip= SUPPLIER INSTEAD OF DROPPING IT (M10)
        return super.tooltip();
    }
}
