/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nz.co.lolnet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.logging.Filter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.Main;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;


/**
 *
 * @author Dajne Win
 */
public class LolnetAuth extends JavaPlugin implements Filter
{
    
    public static final Logger logger = Logger.getLogger("Minecraft");
    public static Main plugin;
    private HashSet<Player> loggedInPlayers;
    private String url, user, password;
    private PhpbbHandler phpbbhandler = new PhpbbHandler();
    
    
    @Override
    public void onEnable()
    {
        getServer().getLogger().setFilter(this);
        loggedInPlayers = new HashSet<>();
        getServer().getPluginManager().registerEvents(new nz.co.lolnet.LolAuthEventListener(this), this);
        this.saveDefaultConfig();
        url = "jdbc:mysql://" + getConfig().getString("DatabaseAddress") + ":" + getConfig().getString("DatabasePort") + "/" + getConfig().getString("DatabaseName");
        user = getConfig().getString("DatabaseUserName");
        password = getConfig().getString("DatabasePassword");
        try {
            Metrics metrics = new Metrics(this);
            metrics.start();
        } catch (IOException e) {
            // Failed to submit the stats :-(
            logger.log(Level.INFO, "[{0}] Metrics: Failed to submit the stats", this.getName());
        }
        PluginDescriptionFile pdfFile = this.getDescription();
        logger.log(Level.INFO, "{0} Version {1} Has Been Enabled!", new Object[]{pdfFile.getName(), pdfFile.getVersion()});
    }
    
    @Override
    public void onDisable()
    {
        PluginDescriptionFile pdfFile = this.getDescription();
        LolnetAuth.logger.log(Level.INFO, "{0} Version {1} Has Been Disabled!", new Object[]{pdfFile.getName(), pdfFile.getVersion()});
    }
    
    private boolean loginMySQL(Player player, String pword)
    {
        boolean success = false;
        String playerName = player.getName().toLowerCase();
        Connection con = null;
        Statement st = null;
        ResultSet rs = null;
        
        try {
            con = DriverManager.getConnection(url, user, password);
            st = con.createStatement();
            rs = st.executeQuery("SELECT * FROM phpbb_users");
            while (rs.next()) {
                String username_clean = rs.getString("username_clean");
                if (playerName.equals(username_clean))
                {
                    String user_password = rs.getString("user_password");
                    success = phpbbhandler.phpbb_check_hash(pword, user_password);
                }
            }

        } catch (SQLException ex) {
            logger.log(Level.SEVERE, ChatColor.RED + "Unable to connect to Database!", ex);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (st != null) {
                    st.close();
                }
                if (con != null) {
                    con.close();
                }

            } catch (SQLException ex) {
                LolnetAuth.logger.log(Level.WARNING, ex.getMessage(), ex);
            }
        }
        return success;
    }
    
    private boolean playerChangePasswordMySQL(Player player, String newPassword)
    {
        boolean success = false;
        String playerName = player.getName().toLowerCase();
        Connection con = null;
        Statement st = null;
        ResultSet rs = null;
        
        try {
            con = DriverManager.getConnection(url, user, password);
            st = con.createStatement();
            rs = st.executeQuery("SELECT * FROM phpbb_users");
            while (rs.next()) {
                String username_clean = rs.getString("username_clean");
                if (playerName.equals(username_clean))
                {
                    String passwordHash = phpbbhandler.phpbb_hash(newPassword);
                    String user_password = rs.getString("user_password");
                    try (PreparedStatement ps = con.prepareStatement("UPDATE phpbb_users SET user_password=\"" + passwordHash + "\" WHERE user_password=\"" + user_password + "\"", 1)) 
                    {
                        ps.executeUpdate();
                        ps.close();
                        success = true;
                        player.sendMessage(ChatColor.GREEN + "Password Changed!");
                        player.sendMessage(ChatColor.GREEN + "The next time you login please use your new Password!");
                    }
                }
            }

        } catch (SQLException ex) {
            LolnetAuth.logger.log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (st != null) {
                    st.close();
                }
                if (con != null) {
                    con.close();
                }

            } catch (SQLException ex) {
                LolnetAuth.logger.log(Level.WARNING, ex.getMessage(), ex);
            }
        }
        return success;
    }
    
    private boolean playerAlreadyRegisteredMySQL(Player player)
    {
        boolean success = false;
        Connection con = null;
        Statement st = null;
        ResultSet rs = null;
        String playerName = player.getName().toLowerCase();
        try {
            con = DriverManager.getConnection(url, user, password);
            st = con.createStatement();
            rs = st.executeQuery("SELECT * FROM phpbb_users");
            while (rs.next() && success == false) {
                String username_clean = rs.getString("username_clean");
                if (playerName.equals(username_clean))
                {
                    success = true;
                }
            }
        } catch (SQLException ex) {
            LolnetAuth.logger.log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (st != null) {
                    st.close();
                }
                if (con != null) {
                    con.close();
                }

            } catch (SQLException ex) {
                LolnetAuth.logger.log(Level.WARNING, ex.getMessage(), ex);
            }
        }
        if (success)
        {
            player.sendMessage("You are already registered!");
        }
        return success;
    }
    
    private int getPlayerUserID(Player player)
    {
        boolean success = false;
        int userid = 0;
        Connection con = null;
        Statement st = null;
        ResultSet rs = null;
        String playerName = player.getName().toLowerCase();
        try {
            con = DriverManager.getConnection(url, user, password);
            st = con.createStatement();
            rs = st.executeQuery("SELECT * FROM phpbb_users");
            while (rs.next() && success == false) {
                String username_clean = rs.getString("username_clean");
                if (playerName.equals(username_clean))
                {
                    userid = rs.getInt("user_id");
                    success = true;
                }
            }
        } catch (SQLException ex) {
            LolnetAuth.logger.log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (st != null) {
                    st.close();
                }
                if (con != null) {
                    con.close();
                }

            } catch (SQLException ex) {
                LolnetAuth.logger.log(Level.WARNING, ex.getMessage(), ex);
            }
        }
        return userid;
    }
    
    private boolean registerMySQL(Player player, String pword, String email) throws SQLException
    {
        boolean success = false;
        if (!playerAlreadyRegisteredMySQL(player))
        {
            String passwordHash = phpbbhandler.phpbb_hash(pword);
            long timestamp = System.currentTimeMillis()/1000;

            String playerName = player.getName();
            String playerIP = player.getAddress().toString();
            Connection con = DriverManager.getConnection(url, user, password);
            Statement st = null;
            ResultSet rs = null;
            
            
            try (PreparedStatement ps = con.prepareStatement("INSERT INTO `" + "phpbb_" + "users" + "` (`username`, `username_clean`, `user_password`, `user_email`, `group_id`, `user_timezone`, `user_dst`, `user_lang`, `user_type`, `user_regdate`, `user_new`, `user_lastvisit`, `user_permissions`, `user_sig`, `user_occ`, `user_interests`, `user_ip`)  VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", 1)) {
                ps.setString(1, playerName);
                ps.setString(2, playerName.toLowerCase());
                ps.setString(3, passwordHash);
                ps.setString(4, email);
                ps.setString(5, "2"); // group
                ps.setString(6, "0.00"); // timezone
                ps.setString(7, "0"); // dst
                ps.setString(8, "en"); // lang
                ps.setString(9, "0"); // user_type
                ps.setLong(10, timestamp); // user_regdate
                ps.setString(11, "1"); // usernew
                ps.setLong(12, timestamp); // user_lastvisit
                ps.setString(13, ""); // user_permissions
                ps.setString(14, ""); // user_sig
                ps.setString(15, ""); // user_occ
                ps.setString(16, ""); // user_interests
                //
                ps.setString(17, playerIP); // user_ip
                ps.executeUpdate();
                ps.close();
            }
            
            int userid = getPlayerUserID(player);
            
            if (userid != 0)
            {
                try (PreparedStatement ps = con.prepareStatement("INSERT INTO `" + "phpbb_" + "user_group" + "` (`group_id`, `user_id`, `group_leader`, `user_pending`) VALUES (?, ?, ?, ?)", 1)) {
                    ps.setInt(1, 2);
                    ps.setInt(2, userid);
                    ps.setInt(3, 0);
                    ps.setInt(4, 0);
                    ps.executeUpdate();
                    ps.close();
                }

                try (PreparedStatement ps = con.prepareStatement("INSERT INTO `" + "phpbb_" + "user_group" + "` (`group_id`, `user_id`, `group_leader`, `user_pending`) VALUES (?, ?, ?, ?)", 1)) {
                    ps.setInt(1, 7);
                    ps.setInt(2, userid);
                    ps.setInt(3, 0);
                    ps.setInt(4, 0);
                    ps.executeUpdate();
                    ps.close();
                }

                try (PreparedStatement ps = con.prepareStatement("UPDATE `" + "phpbb_" + "config" + "` SET `config_value` = '" + userid + "' WHERE `config_name` = 'newest_user_id'")) {
                    ps.executeUpdate();
                    ps.close();
                }

                try (PreparedStatement ps = con.prepareStatement("UPDATE `" + "phpbb_" + "config" + "` SET `config_value` = '" + playerName + "' WHERE `config_name` = 'newest_username'")) {
                    ps.executeUpdate();
                    ps.close();
                }

                try (PreparedStatement ps = con.prepareStatement("UPDATE `" + "phpbb_" + "config" + "` SET `config_value` = config_value + 1 WHERE `config_name` = 'num_users'")) 
                {
                    ps.executeUpdate();
                    ps.close();
                }
            }
            success = true;
        }
        return success;
    }
    
    public boolean isPlayerLoggedIn(Player player)
    {
        return loggedInPlayers.contains(player);
    }
    
    public boolean setPlayerLoggedIn(Player player)
    {
        boolean success = loggedInPlayers.add(player);
        return success;
    }
    
    public boolean setPlayerLoggedOut(Player player)
    {
        boolean success = loggedInPlayers.remove(player);
        return success;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        boolean success = false;
        if (checkIfCommandSenderIsPlayer(sender)) 
        {
            Player player = (Player) sender;
            if(cmd.getName().equalsIgnoreCase("login"))
            {
                success = commandLogin(player, cmd, label, args);
                if(success)
                {
                    logger.log(Level.INFO, "{0} has logged in.", player.getName());
                }
            }
            else if(cmd.getName().equalsIgnoreCase("register"))
            {
                try {
                    success = commandRegister(player, cmd, label, args);
                    if(success)
                    {
                        logger.log(Level.INFO, "{0} has registered.", player.getName());
                    }
                } catch (SQLException ex) {
                    Logger.getLogger(LolnetAuth.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            if(cmd.getName().equalsIgnoreCase("recover"))
            {
                player.sendMessage(ChatColor.GREEN + "Please go to the link below to reset your password:");
                player.sendMessage(ChatColor.AQUA + "" + ChatColor.UNDERLINE + "http://www.lolnet.co.nz/ucp.php?mode=sendpassword");
                success = true;
            }
            else if(cmd.getName().equalsIgnoreCase("changepassword"))
            {
                if (isPlayerLoggedIn(player))
                {
                    success = commandChangePassword(player, cmd, label, args);
                    if(success)
                    {
                        logger.log(Level.INFO, "{0} has changed their password.", player.getName());
                    }
                }
            }
        }
    	return success; 
    }
    
    private boolean commandChangePassword(Player player, Command cmd, String label, String[] args)
    {
        boolean success = false;
        if (args.length == 2)
        {
            if (args[0].equals(args [1]))
            {
                if (args[0].toString().length() < 6)
                {
                    player.sendMessage(ChatColor.RED + "Password must be 6 characters or more!");
                }
                else
                {
                    player.sendMessage(ChatColor.YELLOW + "Changing password!");
                    if(playerChangePasswordMySQL(player,args[0]))
                    {
                        success = true;
                    }
                }
            }
            else
            {
                player.sendMessage(ChatColor.RED + "Password does not match!");
            }
        }
        else
        {
            player.sendMessage(ChatColor.RED + "/changepassword passwordonce passwordtwice");
        }
        return success;
    }
    
    private boolean checkIfCommandSenderIsPlayer(CommandSender sender)
    {
        boolean success = false;
        if (sender instanceof Player)
        {
            success = true;
        }
        return success;
    }
    
    private boolean commandLogin(Player player, Command cmd, String label, String[] args)
    {
        boolean success = false;
        int argsLength = args.length;
        if (!isPlayerLoggedIn(player))
        {
            if (argsLength == 1)
            {
                if (loginMySQL(player, args[0].toString()))
                {
                    success = setPlayerLoggedIn(player);
                    player.sendMessage(ChatColor.GREEN + "Logged In!");
                }
                else
                {
                    if (player.isOnline())
                    {
                        logger.log(Level.WARNING, "{0} {1} failed to log in!", new Object[]{player.getName(), player.getAddress().toString()});
                        player.kickPlayer("Incorrect Password!");
                    }
                }
            }
            else
            {
                player.sendMessage(ChatColor.RED + "/login yourpasswordhere");
            }
        }
        else
        {
            player.sendMessage(ChatColor.GOLD + "You are already logged in!");
        }
        return success;
    }
    
    private boolean commandRegister(Player player, Command cmd, String label, String[] args) throws SQLException
    {
        boolean success = false;
        int argsLength = args.length;
        if (!isPlayerLoggedIn(player))
        {
            if (argsLength == 2)
            {
                if (args[0].toString().length() < 6)
                {
                    player.sendMessage(ChatColor.RED + "Password must be 6 characters or more!");
                }
                else if (!args[1].toString().contains("@"))
                {
                    player.sendMessage(ChatColor.RED + "Invalid email address!");
                }
                else
                {
                    if (registerMySQL(player, args[0].toString(), args[1].toString()))
                    {
                        success = true;
                        player.sendMessage(ChatColor.GREEN + "You are now Registered!");
                        player.sendMessage(ChatColor.GREEN + "Please login by typing /login password");
                    }
                }
            }
            else
            {
                player.sendMessage(ChatColor.RED + "/register password email");
            }
        }
        return success;
    }

    @Override
    public boolean isLoggable(LogRecord record) 
    {
        String filterConsole = record.getMessage().toLowerCase();
        if (filterConsole.contains("issued server command: /login"))
        {
            return false;
        }
        else if (filterConsole.contains("issued server command: /register"))
        {
            return false;
        }
        else if (filterConsole.contains("issued server command: /changepassword"))
        {
            return false;
        }
        else
        {
            return true;
        }
    }
}
