package nz.co.lolnet;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import threads.onPlayerJoin;

/**
 *
 * @author Dajne Win
 */
class LolAuthEventListener implements Listener {
    
    private LolnetAuth lolnetauthmain;
    
    public LolAuthEventListener(LolnetAuth passedlolnetauthmain)
    {
        this.lolnetauthmain = passedlolnetauthmain;
    }

    @EventHandler (priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        onPlayerJoin oPJ = new onPlayerJoin(lolnetauthmain,player);
        Thread oPJThread = new Thread(oPJ);
        oPJThread.start();
    }
    
    @EventHandler
    public void onPlayerAttack(EntityDamageByEntityEvent event)
    {
        if (event.getDamager() instanceof Player)
        {
            Player player = (Player)event.getDamager();
            if (!lolnetauthmain.isPlayerLoggedIn(player))
            {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "You must Login!");
            }
        }
    }
    
    @EventHandler //(priority = EventPriority.LOWEST)
    public void onCommandProcess(PlayerCommandPreprocessEvent event)
    {
        Player player = event.getPlayer();
        if (!lolnetauthmain.isPlayerLoggedIn(player))
        {
            event.setMessage(event.getMessage().toLowerCase());
            if (event.getMessage().contains("login"))
            {
                player.sendMessage(ChatColor.GREEN + "Attempting to Login!");
            }
            else if (event.getMessage().contains("register"))
            {
                player.sendMessage(ChatColor.GREEN + "Attempting to Register!");
            }
            else if (event.getMessage().contains("recover"))
            {
                player.sendMessage(ChatColor.GREEN + "Attempting to Recover Password!");
            }
            else
            {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "You must Login!");
            }
        }
    }
    
    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event)
    {
        Player player = event.getPlayer();
        if (!lolnetauthmain.isPlayerLoggedIn(player))
        {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You must Login!");
        }
    }
    
    @EventHandler
    public void onPlayerBlockDamage(BlockDamageEvent event)
    {
        Player player = event.getPlayer();
        if (!lolnetauthmain.isPlayerLoggedIn(player))
        {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You must Login!");
        }
    }
    
    @EventHandler
    public void onPlayerPlaceBlock(BlockPlaceEvent event)
    {
        Player player = event.getPlayer();
        if (!lolnetauthmain.isPlayerLoggedIn(player))
        {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You must Login!");
        }
    }
    
    @EventHandler
    public void onPlayerEmptyBucket(PlayerBucketEmptyEvent event)
    {
        Player player = event.getPlayer();
        if (!lolnetauthmain.isPlayerLoggedIn(player))
        {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You must Login!");
        }
    }
    
    @EventHandler
    public void onPlayerPickupItem(PlayerPickupItemEvent event)
    {
        Player player = event.getPlayer();
        if (!lolnetauthmain.isPlayerLoggedIn(player))
        {
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onPlayerFillBucket(PlayerBucketFillEvent event)
    {
        Player player = event.getPlayer();
        if (!lolnetauthmain.isPlayerLoggedIn(player))
        {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You must Login!");
        }
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event)
    {
        Player player = event.getPlayer();
        if (!lolnetauthmain.isPlayerLoggedIn(player))
        {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You must Login!");
        }
    }
    
    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event)
    {
        Player player = event.getPlayer();
        if (!lolnetauthmain.isPlayerLoggedIn(player))
        {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You must Login!");
        }
    }
    
    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event)
    {
        if (event.getEntity() instanceof Player)
        {
            Player player = (Player)event.getEntity();
            if (!lolnetauthmain.isPlayerLoggedIn(player))
            {
                if(event.getCause().equals(EntityDamageEvent.DamageCause.STARVATION))
                {
                    player.setFoodLevel(20);
                }
                event.setCancelled(true);
            }
        }
    }
    
    @EventHandler
    public void onPlayerChatEvent(AsyncPlayerChatEvent event)
    {
        Player player = event.getPlayer();
        if(!lolnetauthmain.isPlayerLoggedIn(player))
        {
            event.setMessage(ChatColor.RED + "(OFFLINE) " + ChatColor.DARK_GRAY + event.getMessage());
            player.sendMessage(ChatColor.RED + "Having trouble registering? Click the link below for help:");
            player.sendMessage(ChatColor.AQUA + "" + ChatColor.UNDERLINE + "http://youtu.be/CObzZM1mZxw");
        }
    }
    
}
