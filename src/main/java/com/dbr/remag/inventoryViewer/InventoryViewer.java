package com.dbr.remag.inventoryViewer;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public class InventoryViewer extends JavaPlugin implements Listener, CommandExecutor {

    private final Map<Player, Player> viewingPlayers = new HashMap<>(); // Map para gerenciamento de quem está visualizando quem.

    @Override
    public void onEnable() {
        getLogger().info(ChatColor.GREEN + "[InventoryViewer] O plugin foi ativado!");
        getServer().getPluginManager().registerEvents(this, this);
        this.getCommand("openinventory").setExecutor(this); // Registra o comando.
    }

    @Override
    public void onDisable() {
        getLogger().info(ChatColor.RED + "[InventoryViewer] O plugin foi desativado!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Apenas jogadores podem usar este comando.");
            return true;
        }

        Player player = (Player) sender;

        // Verifica permissões
        if (!player.hasPermission("inventoryviewer.use")) {
            player.sendMessage(ChatColor.RED + "Você não tem permissão para usar este comando.");
            return true;
        }

        openSelectionMenu(player); // Abre o menu de seleção
        return true;
    }

    /**
     * Exibe o menu de seleção com cabeças personalizadas que representam os jogadores online.
     */
    private void openSelectionMenu(Player viewer) {
        int onlinePlayers = Bukkit.getOnlinePlayers().size();
        int size = ((onlinePlayers - 1) / 9 + 1) * 9; // Calcula o tamanho dinâmico para o inventário (mínimo 9).

        Inventory menu = Bukkit.createInventory(null, size, ChatColor.GOLD + "Selecionar Jogador");

        for (Player target : Bukkit.getOnlinePlayers()) {
            ItemStack skull = new ItemStack(Material.PLAYER_HEAD); // Cria uma cabeça de jogador
            SkullMeta meta = (SkullMeta) skull.getItemMeta();
            if (meta != null) {
                meta.setOwningPlayer(target); // Define o dono da cabeça pela skin do jogador
                meta.setDisplayName(ChatColor.GREEN + target.getName()); // Nome do jogador
                skull.setItemMeta(meta);
            }
            menu.addItem(skull); // Adiciona a cabeça ao menu
        }

        viewer.openInventory(menu); // Abre o menu para quem usou o comando
    }


    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String menuTitle = ChatColor.GOLD + "Selecionar Jogador";

        if (event.getView().getTitle().equals(menuTitle)) {
            event.setCancelled(true); // Impede que os jogadores retirem ou movam itens no menu

            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || clickedItem.getType() != Material.PLAYER_HEAD) return;

            Player viewer = (Player) event.getWhoClicked();

            // Obtém o jogador alvo com base no nome na cabeça
            SkullMeta meta = (SkullMeta) clickedItem.getItemMeta();
            if (meta == null || meta.getOwningPlayer() == null) return;

            Player target = Bukkit.getPlayer(meta.getOwningPlayer().getUniqueId());
            if (target == null) {
                viewer.sendMessage(ChatColor.RED + "O jogador selecionado não está mais online.");
                return;
            }

            viewer.openInventory(target.getInventory()); // Abre o inventário do jogador selecionado
            viewingPlayers.put(viewer, target);
            viewer.sendMessage(ChatColor.GREEN + "Agora você está visualizando o inventário de " + ChatColor.YELLOW + target.getName() + ChatColor.GREEN + ".");
        }
    }
}
