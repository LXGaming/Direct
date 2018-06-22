/*
 * Copyright 2018 lolnet.co.nz
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

package nz.co.lolnet.direct.configuration;

import com.google.common.io.ByteStreams;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import nz.co.lolnet.direct.Direct;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Config {
    
    private Configuration configuration;
    
    public void loadConfig() {
        if (!Direct.getInstance().getDataFolder().exists()) {
            Direct.getInstance().getDataFolder().mkdir();
        }
        
        configuration = loadFile("config.yml");
    }
    
    public Configuration loadFile(String name) {
        try {
            File file = new File(Direct.getInstance().getDataFolder(), name);
            
            if (!file.exists()) {
                file.createNewFile();
                InputStream inputStream = getClass().getResourceAsStream("/" + name);
                OutputStream outputStream = new FileOutputStream(file);
                ByteStreams.copy(inputStream, outputStream);
                Direct.getInstance().getLogger().info("Successfully created " + name);
            }
            
            return ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
        } catch (IOException | NullPointerException | SecurityException ex) {
            Direct.getInstance().getLogger().severe("Encountered an error processing " + getClass().getSimpleName() + "::loadFile");
            ex.printStackTrace();
            return null;
        }
    }
    
    protected void saveFile(String name, Configuration config) {
        try {
            File file = new File(Direct.getInstance().getDataFolder(), name);
            ConfigurationProvider.getProvider(YamlConfiguration.class).save(config, file);
        } catch (IOException | NullPointerException | SecurityException ex) {
            Direct.getInstance().getLogger().severe("Encountered an error processing " + getClass().getSimpleName() + "::saveFile");
            ex.printStackTrace();
        }
    }
    
    public Configuration getConfiguration() {
        return configuration;
    }
}