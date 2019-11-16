package me.playbosswar.com;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class Tools {
	public static Plugin pl = Main.getPlugin();

	public static void printDate() {
		LocalDate date = LocalDate.now();
		DayOfWeek dow = date.getDayOfWeek();
		if (Main.getPlugin().getConfig().getBoolean("timeonload")) {
			Bukkit.getConsoleSender().sendMessage("�aServer time : " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
			Bukkit.getConsoleSender().sendMessage("�aServer day : " + dow);
		}
	}

	public static void initConfig() {
		pl.saveDefaultConfig();
		pl.getConfig().options().copyDefaults(false);
	}

	public static void reloadTaks() {
		Bukkit.getScheduler().cancelTasks(Main.getPlugin());
		pl.reloadConfig();
		TaskRunner.startTasks();
	}

	public static String getGender(final String task) {
		if(!pl.getConfig().contains("tasks." + task + ".gender")) {
			return "console";
		}
		return pl.getConfig().getString("tasks." + task + ".gender").toLowerCase();
	}

	public static void easyCommandRunner(final String task, final long seconds, final String gender) {
		Bukkit.getScheduler().scheduleSyncRepeatingTask(pl, new CommandTask(pl.getConfig()
				.getStringList("tasks." + task + ".commands"), gender, task), seconds, seconds);
	}

	public static void simpleCommandRunner(String task, String gender) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), () -> {
			for (String next : pl.getConfig().getStringList("tasks." + task + ".commands")) {
				Tools.executeCommand(task, next, gender);
			}
		}, 50L);
	}

	public static void complexCommandRunner(final String task, final String gender) {
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				final Date date = new Date();
				final SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
				final String formattedDate = df.format(date);
				for (final String hour : pl.getConfig().getStringList("tasks." + task + ".time")) {
					if (formattedDate.equals(hour)) {
						Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), () -> {
							int i = pl.getConfig().getStringList("tasks." + task + ".time").toArray().length;
							for (final String next : pl.getConfig().getStringList("tasks." + task + ".commands")) {
								if (i > 0) {
									Tools.executeCommand(task, next, gender);
									--i;
								}
							}
						}, 50L);
					}
				}
			}
		}, 1L, 1000L);
	}


	public static void executeCommand(String task, String cmd, String gender) {
		if (gender.equals("console")) {
			if (pl.getConfig().getBoolean("tasks." + task + ".useRandom")) {
				final double d = pl.getConfig().getDouble("tasks." + task + ".random");
				if (randomCheck(d)) {
					Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), cmd);
				}
			}
			else {
				Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), cmd);
			}
		}
		else if (gender.equals("operator")) {
			for (final Player p : Bukkit.getOnlinePlayers()) {
				int i = 0;
				if (p.isOp()) {
					i = 1;
				}
				try {
					p.setOp(true);
					if (pl.getConfig().getBoolean("tasks." + task + ".useRandom")) {
						final double d2 = pl.getConfig().getDouble("tasks." + task + ".random");
						if (!randomCheck(d2)) {
							continue;
						}
						p.performCommand(cmd);
					}
					else {
						p.performCommand(cmd);
					}
				}
				finally {
					if (i == 0) {
						p.setOp(false);
					}
				}
			}
		}
		else if (gender.equals("player")) {
			final String perm = pl.getConfig().getString("tasks." + task + ".permission");
			for (final Player p2 : Bukkit.getOnlinePlayers()) {
				if (perm != null) {
					if (!p2.hasPermission(perm)) {
						continue;
					}
					if (pl.getConfig().getBoolean("tasks." + task + ".useRandom")) {
						final double d2 = Main.getPlugin().getConfig().getDouble("tasks." + task + ".random");
						if (!randomCheck(d2)) {
							continue;
						}
						else {
							p2.performCommand(cmd);
						}
					}
					else {
						p2.performCommand(cmd);
					}
				}
				else if (pl.getConfig().getBoolean("tasks." + task + ".useRandom")) {
					final double d2 = pl.getConfig().getDouble("tasks." + task + ".random");
					if (!randomCheck(d2)) {
						continue;
					}
					else {
						p2.performCommand(cmd);
					}
				}
				else {
					p2.performCommand(cmd);
				}
			}
		}
	}

	public static boolean randomCheck(double random) {
		final Random r = new Random();
		final float chance = r.nextFloat();
		return chance <= random;
	}
}
