/*
 * Copyright 2020 Alex Thomson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.lxgaming.direct.common.manager;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import io.github.lxgaming.direct.common.Direct;
import io.github.lxgaming.direct.common.command.Command;
import io.github.lxgaming.direct.common.command.DebugCommand;
import io.github.lxgaming.direct.common.command.HelpCommand;
import io.github.lxgaming.direct.common.command.InformationCommand;
import io.github.lxgaming.direct.common.command.LobbyCommand;
import io.github.lxgaming.direct.common.command.ReloadCommand;
import io.github.lxgaming.direct.common.entity.Locale;
import io.github.lxgaming.direct.common.entity.Source;
import io.github.lxgaming.direct.common.util.StringUtils;
import io.github.lxgaming.direct.common.util.Toolbox;
import io.github.lxgaming.direct.common.util.brigadier.adapter.CommandAdapter;
import io.github.lxgaming.direct.common.util.text.adapter.LocaleAdapter;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public final class CommandManager {
    
    public static final CommandDispatcher<Source> DISPATCHER = new CommandDispatcher<>();
    private static final Set<Command> COMMANDS = Sets.newLinkedHashSet();
    private static final Set<Class<? extends Command>> COMMAND_CLASSES = Sets.newHashSet();
    
    public static void prepare() {
        registerCommand(DebugCommand.class);
        registerCommand(HelpCommand.class);
        registerCommand(InformationCommand.class);
        registerCommand(LobbyCommand.class);
        registerCommand(ReloadCommand.class);
        
        for (Command command : CommandManager.COMMANDS) {
            register(command).forEach(DISPATCHER.getRoot()::addChild);
        }
    }
    
    public static boolean execute(Source source, String message) {
        if (StringUtils.isBlank(message)) {
            LocaleAdapter.sendSystemMessage(source, Locale.COMMAND_BASE, getPrefix());
            return false;
        }
        
        Direct.getInstance().getLogger().debug("Processing {} for {} ({})", message, source.getName(), source.getUniqueId());
        
        try {
            CommandManager.DISPATCHER.execute(message, source);
            return true;
        } catch (CommandSyntaxException ex) {
            LocaleAdapter.sendSystemMessage(source, Locale.COMMAND_ERROR, ex.getMessage());
            return false;
        } catch (Exception ex) {
            Direct.getInstance().getLogger().error("Encountered an error while executing {}", message, ex);
            LocaleAdapter.sendSystemMessage(source, Locale.COMMAND_EXCEPTION);
            return false;
        }
    }
    
    public static boolean registerAlias(Command command, String alias) {
        if (StringUtils.containsIgnoreCase(command.getAliases(), alias)) {
            Direct.getInstance().getLogger().warn("{} is already registered for {}", alias, Toolbox.getClassSimpleName(command.getClass()));
            return false;
        }
        
        command.getAliases().add(alias);
        Direct.getInstance().getLogger().debug("{} registered for {}", alias, Toolbox.getClassSimpleName(command.getClass()));
        return true;
    }
    
    public static boolean registerCommand(Class<? extends Command> commandClass) {
        Command command = registerCommand(COMMANDS, commandClass);
        if (command != null) {
            Direct.getInstance().getLogger().debug("{} registered", Toolbox.getClassSimpleName(commandClass));
            return true;
        }
        
        return false;
    }
    
    public static boolean registerCommand(Command parentCommand, Class<? extends Command> commandClass) {
        if (parentCommand.getClass() == commandClass) {
            Direct.getInstance().getLogger().warn("{} attempted to register itself", Toolbox.getClassSimpleName(parentCommand.getClass()));
            return false;
        }
        
        Command command = registerCommand(parentCommand.getChildren(), commandClass);
        if (command != null) {
            Direct.getInstance().getLogger().debug("{} registered for {}", Toolbox.getClassSimpleName(commandClass), Toolbox.getClassSimpleName(parentCommand.getClass()));
            return true;
        }
        
        return false;
    }
    
    private static Command registerCommand(Collection<Command> commands, Class<? extends Command> commandClass) {
        if (COMMAND_CLASSES.contains(commandClass)) {
            Direct.getInstance().getLogger().warn("{} is already registered", Toolbox.getClassSimpleName(commandClass));
            return null;
        }
        
        COMMAND_CLASSES.add(commandClass);
        Command command = Toolbox.newInstance(commandClass);
        if (command == null) {
            Direct.getInstance().getLogger().error("{} failed to initialize", Toolbox.getClassSimpleName(commandClass));
            return null;
        }
        
        try {
            if (!command.prepare()) {
                Direct.getInstance().getLogger().warn("{} failed to prepare", Toolbox.getClassSimpleName(commandClass));
                return null;
            }
        } catch (Exception ex) {
            Direct.getInstance().getLogger().error("Encountered an error while preparing {}", Toolbox.getClassSimpleName(commandClass), ex);
            return null;
        }
        
        if (commands.add(command)) {
            return command;
        }
        
        return null;
    }
    
    public static List<String> getPlatformCommands() {
        List<String> aliases = Lists.newArrayList();
        for (Command command : CommandManager.COMMANDS) {
            if (command.getPlatform() == Boolean.TRUE) {
                String alias = Iterables.getFirst(command.getAliases(), null);
                if (StringUtils.isNotBlank(alias)) {
                    aliases.add(alias);
                }
            }
        }
        
        return aliases;
    }
    
    public static String getPrefix() {
        return Direct.ID;
    }
    
    private static Collection<CommandNode<Source>> register(Command command) {
        List<CommandNode<Source>> commandNodes = Lists.newArrayList();
        for (String alias : command.getAliases()) {
            LiteralArgumentBuilder<Source> argumentBuilder = Command.literal(alias.toLowerCase());
            command.register(argumentBuilder);
            if (argumentBuilder.getCommand() != null) {
                argumentBuilder.executes(new CommandAdapter<>(command, argumentBuilder.getCommand()));
            }
            
            commandNodes.add(argumentBuilder.build());
        }
        
        for (Command childCommand : command.getChildren()) {
            Collection<CommandNode<Source>> childCommandNodes = register(childCommand);
            addChildren(commandNodes, childCommandNodes);
        }
        
        return commandNodes;
    }
    
    private static <T> void addChildren(Collection<CommandNode<T>> parentCommandNodes, Collection<CommandNode<T>> childCommandNodes) {
        if (parentCommandNodes.isEmpty() || childCommandNodes.isEmpty()) {
            return;
        }
        
        for (CommandNode<T> parentCommandNode : parentCommandNodes) {
            for (CommandNode<T> childCommandNode : childCommandNodes) {
                parentCommandNode.addChild(childCommandNode);
            }
        }
    }
}