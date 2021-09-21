package com.imjustdoom.justpermissions.commands;

import com.imjustdoom.justpermissions.JustPermissions;
import com.imjustdoom.justpermissions.commands.subcommands.GroupSubcommand;
import com.imjustdoom.justpermissions.commands.subcommands.PlayerSubcommand;
import com.imjustdoom.justpermissions.config.Config;
import com.imjustdoom.justpermissions.util.MessageUtil;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;

import static net.minestom.server.command.builder.arguments.ArgumentType.Literal;
import static net.minestom.server.command.builder.arguments.ArgumentType.Word;

public class JustPermissionsCommand extends Command {

    public JustPermissionsCommand() {
        super("justpermissions", "jp");

        addSubcommand(new PlayerSubcommand());
        addSubcommand(new GroupSubcommand());

        addSyntax(this::executeCreateGroup, Literal("creategroup"), Word("group"));
        addSyntax(this::executeRemoveGroup, Literal("removegroup"), Word("group"));

        //TODO: Sync command
    }

    /**
     * Creates a group and adds it to the db
     * @param sender - command sender
     * @param context - command context
     */
    private void executeCreateGroup(@NotNull CommandSender sender, @NotNull CommandContext context){
        final String group = context.get("group");

        final String perm = "justpermissions.perms";

        if (sender.isPlayer() && !sender.hasPermission(perm)) {
            sender.sendMessage(MessageUtil.translate(Config.Messages.MISSING_PERMISSION.replaceAll("%perm%", perm)));
            return;
        }

        try {
            JustPermissions.getInstance().getDbCon().insertRecord("groups", "'" + group + "'");

            JustPermissions.getInstance().getGroups().add(group);

            sender.sendMessage(MessageUtil.translate(Config.Messages.GROUP_CREATION.replaceAll("%group%", group)));
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    /**
     * Removes a group
     * @param sender - command sender
     * @param context - command context
     */
    private void executeRemoveGroup(@NotNull CommandSender sender, @NotNull CommandContext context){
        final String group = context.get("group");

        final String perm = "justpermissions.perms";

        if (sender.isPlayer() && !sender.hasPermission(perm)) {
            sender.sendMessage(MessageUtil.translate(Config.Messages.MISSING_PERMISSION.replaceAll("%perm%", perm)));
            return;
        }

        try {
            JustPermissions.getInstance().getDbCon().runSql("DELETE FROM groups WHERE name = '" + group + "'");

            JustPermissions.getInstance().getGroups().remove(group);

            sender.sendMessage(MessageUtil.translate(Config.Messages.GROUP_DELETION.replaceAll("%group%", group)));
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }
}