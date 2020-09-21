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

package io.github.lxgaming.direct.common.command;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.lxgaming.direct.common.Direct;
import io.github.lxgaming.direct.common.configuration.Config;
import io.github.lxgaming.direct.common.configuration.category.GeneralCategory;
import io.github.lxgaming.direct.common.entity.Locale;
import io.github.lxgaming.direct.common.entity.Source;
import io.github.lxgaming.direct.common.util.text.adapter.LocaleAdapter;

public class DebugCommand extends Command {
    
    private static final String STATE_ARGUMENT = "state";
    
    @Override
    public boolean prepare() {
        addAlias("Debug");
        description("For development purposes");
        permission("direct.debug.base");
        return true;
    }
    
    @Override
    public void register(LiteralArgumentBuilder<Source> argumentBuilder) {
        argumentBuilder
                .requires(source -> source.hasPermission(getPermission()))
                .executes(context -> {
                    return execute(context.getSource());
                })
                .then(argument(STATE_ARGUMENT, BoolArgumentType.bool())
                        .executes(context -> {
                            return execute(context.getSource(), BoolArgumentType.getBool(context, STATE_ARGUMENT));
                        })
                );
    }
    
    private int execute(Source source) {
        return execute(source, null);
    }
    
    private int execute(Source source, Boolean state) {
        GeneralCategory generalCategory = Direct.getInstance().getConfig().map(Config::getGeneralCategory).orElse(null);
        if (generalCategory == null) {
            LocaleAdapter.sendSystemMessage(source, Locale.CONFIGURATION_ERROR);
            return 0;
        }
        
        if (state != null) {
            generalCategory.setDebug(state);
        } else {
            generalCategory.setDebug(!generalCategory.isDebug());
        }
        
        if (generalCategory.isDebug()) {
            LocaleAdapter.sendSystemMessage(source, Locale.COMMAND_DEBUG_ENABLE);
        } else {
            LocaleAdapter.sendSystemMessage(source, Locale.COMMAND_DEBUG_DISABLE);
        }
        
        return 1;
    }
}