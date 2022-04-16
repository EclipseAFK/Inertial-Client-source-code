package net.minecraft.command.server;

import com.mojang.authlib.GameProfile;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;

public class CommandWhitelist extends CommandBase {
     public String getCommandName() {
          return "whitelist";
     }

     public int getRequiredPermissionLevel() {
          return 3;
     }

     public String getCommandUsage(ICommandSender sender) {
          return "commands.whitelist.usage";
     }

     public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
          if (args.length < 1) {
               throw new WrongUsageException("commands.whitelist.usage", new Object[0]);
          } else {
               if ("on".equals(args[0])) {
                    server.getPlayerList().setWhiteListEnabled(true);
                    notifyCommandListener(sender, this, "commands.whitelist.enabled", new Object[0]);
               } else if ("off".equals(args[0])) {
                    server.getPlayerList().setWhiteListEnabled(false);
                    notifyCommandListener(sender, this, "commands.whitelist.disabled", new Object[0]);
               } else if ("list".equals(args[0])) {
                    sender.addChatMessage(new TextComponentTranslation("commands.whitelist.list", new Object[]{server.getPlayerList().getWhitelistedPlayerNames().length, server.getPlayerList().getAvailablePlayerDat().length}));
                    String[] astring = server.getPlayerList().getWhitelistedPlayerNames();
                    sender.addChatMessage(new TextComponentString(joinNiceString(astring)));
               } else {
                    GameProfile gameprofile1;
                    if ("add".equals(args[0])) {
                         if (args.length < 2) {
                              throw new WrongUsageException("commands.whitelist.add.usage", new Object[0]);
                         }

                         gameprofile1 = server.getPlayerProfileCache().getGameProfileForUsername(args[1]);
                         if (gameprofile1 == null) {
                              throw new CommandException("commands.whitelist.add.failed", new Object[]{args[1]});
                         }

                         server.getPlayerList().addWhitelistedPlayer(gameprofile1);
                         notifyCommandListener(sender, this, "commands.whitelist.add.success", new Object[]{args[1]});
                    } else if ("remove".equals(args[0])) {
                         if (args.length < 2) {
                              throw new WrongUsageException("commands.whitelist.remove.usage", new Object[0]);
                         }

                         gameprofile1 = server.getPlayerList().getWhitelistedPlayers().getByName(args[1]);
                         if (gameprofile1 == null) {
                              throw new CommandException("commands.whitelist.remove.failed", new Object[]{args[1]});
                         }

                         server.getPlayerList().removePlayerFromWhitelist(gameprofile1);
                         notifyCommandListener(sender, this, "commands.whitelist.remove.success", new Object[]{args[1]});
                    } else if ("reload".equals(args[0])) {
                         server.getPlayerList().reloadWhitelist();
                         notifyCommandListener(sender, this, "commands.whitelist.reloaded", new Object[0]);
                    }
               }

          }
     }

     public List getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos) {
          if (args.length == 1) {
               return getListOfStringsMatchingLastWord(args, new String[]{"on", "off", "list", "add", "remove", "reload"});
          } else {
               if (args.length == 2) {
                    if ("remove".equals(args[0])) {
                         return getListOfStringsMatchingLastWord(args, server.getPlayerList().getWhitelistedPlayerNames());
                    }

                    if ("add".equals(args[0])) {
                         return getListOfStringsMatchingLastWord(args, server.getPlayerProfileCache().getUsernames());
                    }
               }

               return Collections.emptyList();
          }
     }
}
