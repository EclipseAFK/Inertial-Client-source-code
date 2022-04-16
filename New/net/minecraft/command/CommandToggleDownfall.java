package net.minecraft.command;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.storage.WorldInfo;

public class CommandToggleDownfall extends CommandBase {
     public String getCommandName() {
          return "toggledownfall";
     }

     public int getRequiredPermissionLevel() {
          return 2;
     }

     public String getCommandUsage(ICommandSender sender) {
          return "commands.downfall.usage";
     }

     public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
          this.toggleRainfall(server);
          notifyCommandListener(sender, this, "commands.downfall.success", new Object[0]);
     }

     protected void toggleRainfall(MinecraftServer server) {
          WorldInfo worldinfo = server.worldServers[0].getWorldInfo();
          worldinfo.setRaining(!worldinfo.isRaining());
     }
}
