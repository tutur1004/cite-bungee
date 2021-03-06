package fr.milekat.cite_bungee.chat.commands;

import fr.milekat.cite_bungee.MainBungee;
import fr.milekat.cite_bungee.chat.utils.ChatSend;
import fr.milekat.cite_bungee.core.jedis.JedisPub;
import fr.milekat.cite_bungee.utils_tools.DateMilekat;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static fr.milekat.cite_bungee.MainBungee.log;

public class Broadcast extends Command {
    public Broadcast() {
        super("broadcast", "modo.chat.command.broadcast", "bc", "annonce", "info");
    }

    /**
     *      Commande pour envoyer une annonce dans le chat
     */
    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length<1) {
            sendHelp(sender);
        } else {
            StringBuilder sb = new StringBuilder();
            for (String loop : args) {
                sb.append(loop);
                sb.append(" ");
            }
            createAnnonce(sb.toString());
        }
    }

    public static void createAnnonce(String annonce) {
        Connection connection = MainBungee.getInstance().getSql().getConnection();
        try {
            PreparedStatement q = connection.prepareStatement("INSERT INTO `" + MainBungee.SQLPREFIX +
                    "chat`(`player_id`, `msg`, `msg_type`, `date_msg`) VALUES (10,?,4,?) RETURNING `msg_id`;");

            q.setString(1,annonce);
            q.setString(2,DateMilekat.setDateNow());
            q.execute();
            q.getResultSet().last();
            new ChatSend().sendChatFor("all",q.getResultSet().getInt("msg_id"),false);
            log("[Annonce] " + ChatColor.stripColor(annonce.replaceAll("%nl%", " ")));
            // Redis
            JedisPub.sendRedis("new_msg#:#" + q.getResultSet().getInt("msg_id"));
            q.close();
        } catch (SQLException throwables) {
            MainBungee.warning("Impossible de faire une nouvelle annonce.");
            if (MainBungee.logDebug) log(annonce);
            if (MainBungee.logDebug) throwables.printStackTrace();
        }
    }

    /**
     *      Envoie la liste d'help de la commande
     * @param sender joueur qui exécute la commande
     */
    private void sendHelp(CommandSender sender){
        sender.sendMessage(new TextComponent(MainBungee.prefixCmd));
        sender.sendMessage(new TextComponent("§6/bc <message>:§r envoyer une annonce dans le chat."));
    }
}
