/*
 * Copyright 2018 Alex Thomson
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

package io.github.lxgaming.direct.velocity.command;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.suggestion.Suggestion;
import com.velocitypowered.api.command.RawCommand;
import io.github.lxgaming.direct.common.Direct;
import io.github.lxgaming.direct.common.entity.Source;
import io.github.lxgaming.direct.common.manager.CommandManager;
import io.github.lxgaming.direct.velocity.entity.VelocitySource;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class DirectCommand implements RawCommand {
    
    @Override
    public void execute(RawCommand.Invocation invocation) {
        String arguments = getArguments(invocation);
        VelocitySource source = new VelocitySource(invocation.source());
        
        CommandManager.execute(source, arguments);
    }
    
    @Override
    public List<String> suggest(Invocation invocation) {
        try {
            return suggestAsync(invocation).get();
        } catch (Exception ex) {
            Direct.getInstance().getLogger().error("Encountered an error while getting suggestions", ex);
            return ImmutableList.of();
        }
    }
    
    @Override
    public CompletableFuture<List<String>> suggestAsync(Invocation invocation) {
        String arguments = getArguments(invocation);
        VelocitySource source = new VelocitySource(invocation.source());
        
        ParseResults<Source> parseResults = CommandManager.DISPATCHER.parse(arguments, source);
        return CommandManager.DISPATCHER.getCompletionSuggestions(parseResults)
                .thenApply(suggestions -> Lists.transform(suggestions.getList(), Suggestion::getText));
    }
    
    private String getArguments(Invocation invocation) {
        if (invocation.alias().equalsIgnoreCase(CommandManager.getPrefix())) {
            return invocation.arguments();
        }
        
        return invocation.alias() + " " + invocation.arguments();
    }
}