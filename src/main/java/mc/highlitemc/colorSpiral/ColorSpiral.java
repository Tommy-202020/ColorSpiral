package mc.highlitemc.colorSpiral;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

public class ColorSpiral extends JavaPlugin implements Listener {

    private final HashMap<UUID, Integer> playerNumbers = new HashMap<>();
    private final Random random = new Random();
    private DatabaseHelper databaseHelper;
    private double spiralSpeed;
    private double spiralRadius;
    private int spiralDuration;
    private String particleEffect;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadConfigValues();

        File pluginFolder = new File(getDataFolder(), "SpiralParticles");
        if (!pluginFolder.exists()) {
            pluginFolder.mkdirs();
        }

        String dbFilePath = new File(pluginFolder, "spiral_data.db").getAbsolutePath();
        databaseHelper = new DatabaseHelper(dbFilePath);
        databaseHelper.createTableIfNotExists();
        getServer().getPluginManager().registerEvents(this, this);
    }

    private void loadConfigValues() {
        spiralDuration = getConfig().getInt("spiral.duration");
        spiralSpeed = getConfig().getDouble("spiral.speed");
        spiralRadius = getConfig().getDouble("spiral.radius");
        particleEffect = getConfig().getString("spiral.particle_effect");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();
        Integer savedNumber = databaseHelper.loadPlayerData(playerUUID.toString());

        int number;
        if (savedNumber != null) {
            number = savedNumber;
        } else {
            number = random.nextInt(8) + 1; //
            databaseHelper.savePlayerData(playerUUID.toString(), number);
        }

        playerNumbers.put(playerUUID, number);

        Color color = getColorFromNumber(number);
        startSpiralEffect(player, color);

        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aYou have been assigned number &" + number + " (" + number + ")"));
    }

    private void startSpiralEffect(Player player, Color color) {
        new BukkitRunnable() {
            double t = 0;
            int ticks = 0;

            @Override
            public void run() {
                if (!player.isOnline() || ticks >= spiralDuration * 20) { // Duration in ticks
                    this.cancel();
                    return;
                }

                Location loc = player.getLocation().add(0, 1.5, 0); // Center above player
                for (int i = 0; i < 3; i++) { // Multiple spirals
                    double x = spiralRadius * Math.cos(t + (i * Math.PI / 1.5));
                    double z = spiralRadius * Math.sin(t + (i * Math.PI / 1.5));
                    Location particleLoc = loc.clone().add(x, t / 10, z);
                    spawnColoredParticle(particleLoc, color);
                }
                t += spiralSpeed;
                ticks += 2;
            }
        }.runTaskTimer(this, 0, 2);
    }

    private void spawnColoredParticle(Location loc, Color color) {
        Particle.DustTransition dustTransition = new Particle.DustTransition(color, Color.WHITE, 1.2F);
        loc.getWorld().spawnParticle(Particle.DUST_COLOR_TRANSITION, loc, 1, 0, 0, 0, 0.01, dustTransition);
    }

    private Color getColorFromNumber(int number) {
        return switch (number) {
            case 1 -> Color.fromRGB(0, 0, 170);      // Dark Blue
            case 2 -> Color.fromRGB(0, 170, 0);      // Dark Green
            case 3 -> Color.fromRGB(0, 170, 170);    // Dark Aqua
            case 4 -> Color.fromRGB(170, 0, 0);      // Dark Red
            case 5 -> Color.fromRGB(170, 0, 170);    // Dark Purple
            case 6 -> Color.fromRGB(255, 170, 0);    // Gold
            case 7 -> Color.fromRGB(170, 170, 170);  // Gray
            case 8 -> Color.fromRGB(85, 85, 255);    // Light Blue
            default -> Color.WHITE;
        };
    }
}
