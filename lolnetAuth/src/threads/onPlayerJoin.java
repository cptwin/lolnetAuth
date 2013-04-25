/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package threads;

import java.util.regex.Pattern;
import nz.co.lolnet.LolnetAuth;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

/**
 *
 * @author Dajne Win
 */
public class onPlayerJoin implements Runnable {
    
    private LolnetAuth lolnetauthmain;
    private Player player;
    
    public onPlayerJoin(LolnetAuth passedMain, Player passedPlayer)
    {
        this.lolnetauthmain = passedMain;
        this.player = passedPlayer;
    }

    @Override
    public void run() {
        lolnetauthmain.setPlayerLoggedOut(player);
        if (player.getGameMode().equals(GameMode.CREATIVE))
        {
            player.getInventory().clear();
            player.setGameMode(GameMode.SURVIVAL);
        }
        if (player.getName().length() < 3)
        {
            if (player.isOnline())
            {
                player.kickPlayer("Username must be longer than 3 characters!");
            }
        }
        else if (invalidPlayerName(player.getName()))
        {
            if (player.isOnline())
            {
                player.kickPlayer("Invalid Username!");
            }
        }
        else
        {
            player.sendMessage(ChatColor.GOLD + "---------========= " + ChatColor.GREEN + "lolnetAuth" + ChatColor.GOLD + " ===========-----------");
            player.sendMessage(ChatColor.GOLD + "---------========= " + ChatColor.AQUA + "" + ChatColor.UNDERLINE + "www.lolnet.co.nz" + ChatColor.RESET + "" + ChatColor.GOLD + " =========---------");
            player.sendMessage(ChatColor.RED + "Please Login or Register");
            player.sendMessage(ChatColor.RED + "By Typing /login password");
            player.sendMessage(ChatColor.RED + "Or /register password email");
            player.sendMessage(ChatColor.RED + "Or /recover to reset your password!");
            player.sendMessage(ChatColor.GOLD + "------------------=================------------------");
        }
    }
    
    private boolean invalidPlayerName(String playerName)
    {      
        boolean success = false;
        Pattern p = Pattern.compile("[^a-zA-Z0-9]");
        Pattern u = Pattern.compile("_");
        if (p.matcher(playerName).find() && !u.matcher(playerName).find())
        {
            success = true;
        }
        
        return success;
    }
    
}
