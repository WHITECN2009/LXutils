package org.WHITECN;

import org.WHITECN.commands.CBtoFunction.tofunction;
import org.WHITECN.commands.CBtoFunction.tofunctionconfirm;
import org.WHITECN.commands.DamageMeter.dmgmeter;
import org.WHITECN.commands.SizeCalculator.sizecalc;
import org.WHITECN.commands.fakeop.fakeop;
import org.WHITECN.utils.DamageMeter.damageListener;
import org.WHITECN.utils.tagUtils;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;
import java.util.logging.Logger;

public final class lxutils extends JavaPlugin {
    private static Logger logger;

    @Override
    public void onEnable() {
        tagUtils.init(this);
        logger = getLogger();
        this.getLogger().info("插件已启用");
        getServer().getPluginManager().registerEvents(new damageListener(),this);
        Objects.requireNonNull(this.getCommand("dmgmeter")).setExecutor(new dmgmeter());
        Objects.requireNonNull(this.getCommand("tofunction")).setExecutor(new tofunction());
        Objects.requireNonNull(this.getCommand("tofunctionconfirm")).setExecutor(new tofunctionconfirm());
        Objects.requireNonNull(this.getCommand("sizecalc")).setExecutor(new sizecalc());
        Objects.requireNonNull(this.getCommand("sizecalculator")).setExecutor(new sizecalc());
        Objects.requireNonNull(this.getCommand("fakeop")).setExecutor(new fakeop());
    }

    @Override
    public void onDisable() {
        this.getLogger().info("插件已禁用");
    }
}
