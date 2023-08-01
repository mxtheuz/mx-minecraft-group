package br.com.mxtheuz.mxgroup;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class MxGroup extends JavaPlugin implements CommandExecutor {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final File dataFile = new File("./groups.json");
    private static final Map<String, Group> groups = new HashMap<>();

    @Override
    public void onEnable() {
        getLogger().info("[mx-group] Sistema de groups inicializado com sucesso!");
        if (!dataFile.exists()) {
            try {
                dataFile.getParentFile().mkdirs();
                dataFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            loadGroups();
        }

        getCommand("setgroup").setExecutor(this);
        getCommand("removegroup").setExecutor(this);
        getCommand("checkgroup").setExecutor(this);
    }

    @Override
    public void onDisable() {
        saveGroups();
    }

    private void loadGroups() {
        try (BufferedReader reader = new BufferedReader(new FileReader(dataFile))) {
            groups.clear();
            Group[] loadedGroups = gson.fromJson(reader, Group[].class);
            if (loadedGroups != null) {
                for (Group group : loadedGroups) {
                    groups.put(group.getName(), group);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveGroups() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(dataFile))) {
            gson.toJson(groups.values().toArray(new Group[0]), writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (command.getName().equalsIgnoreCase("setgroup")) {
                if (args.length == 2) {
                    String username = args[0];
                    String group = args[1];
                    setGroup(username, group);
                    player.sendMessage("O jogador " + username + " foi adicionado ao grupo " + group + ".");
                } else {
                    player.sendMessage("Uso correto: /setgroup <nome do jogador> <grupo>");
                }
            } else if (command.getName().equalsIgnoreCase("removegroup")) {
                if (args.length == 2) {
                    String username = args[0];
                    String group = args[1];
                    removeGroup(username, group);
                    player.sendMessage("O jogador " + username + " foi removido do grupo " + group + ".");
                } else {
                    player.sendMessage("Uso correto: /removegroup <nome do jogador> <grupo>");
                }
            } else if (command.getName().equalsIgnoreCase("checkgroup")) {
                if (args.length == 2) {
                    String username = args[0];
                    String group = args[1];
                    boolean isInGroup = checkUserInGroup(username, group);
                    if (isInGroup) {
                        player.sendMessage("O jogador " + username + " está no grupo " + group + ".");
                    } else {
                        player.sendMessage("O jogador " + username + " não está no grupo " + group + ".");
                    }
                } else {
                    player.sendMessage("Uso correto: /checkgroup <nome do jogador> <grupo>");
                }
            }
        }
        return true;
    }

    private void setGroup(String username, String groupName) {
        Group group = groups.computeIfAbsent(groupName, Group::new);
        group.addMember(username);
        saveGroups();
    }

    private void removeGroup(String username, String groupName) {
        Group group = groups.get(groupName);
        if (group != null) {
            group.removeMember(username);
            if (group.isEmpty()) {
                groups.remove(groupName);
            }
            saveGroups();
        }
    }

    private boolean checkUserInGroup(String username, String groupName) {
        Group group = groups.get(groupName);
        return group != null && group.hasMember(username);
    }

    private static class Group {
        private final String name;
        private final Map<String, Boolean> members;

        public Group(String name) {
            this.name = name;
            this.members = new HashMap<>();
        }

        public String getName() {
            return name;
        }

        public void addMember(String username) {
            members.put(username, true);
        }

        public void removeMember(String username) {
            members.remove(username);
        }

        public boolean hasMember(String username) {
            return members.containsKey(username);
        }

        public boolean isEmpty() {
            return members.isEmpty();
        }
    }
}
