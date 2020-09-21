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

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.lxgaming.direct.common.Direct;
import io.github.lxgaming.direct.common.entity.Locale;
import io.github.lxgaming.direct.common.entity.Source;
import io.github.lxgaming.direct.common.util.text.adapter.LocaleAdapter;

public class InformationCommand extends Command {
    
    @Override
    public boolean prepare() {
        addAlias("Information");
        addAlias("Info");
        addAlias("Version");
        permission("direct.information.base");
        return true;
    }
    
    @Override
    public void register(LiteralArgumentBuilder<Source> argumentBuilder) {
        argumentBuilder
                .requires(source -> source.hasPermission(getPermission()))
                .executes(context -> {
                    return execute(context.getSource());
                });
    }
    
    private int execute(Source source) {
        LocaleAdapter.sendSystemMessage(source, Locale.GENERAL_INFORMATION,
                Direct.VERSION, Direct.AUTHORS, Direct.SOURCE, Direct.WEBSITE
        );
        
        return 1;
    }
}