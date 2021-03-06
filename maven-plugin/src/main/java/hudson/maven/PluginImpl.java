/*
 * The MIT License
 * 
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., Tom Huybrechts
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package hudson.maven;

import hudson.Plugin;
import hudson.PluginManager.PluginUpdateMonitor;
import hudson.init.InitMilestone;
import hudson.init.Initializer;
import hudson.model.Items;

/**
 * @author huybrechts
 * @author Dominik Bartholdi (imod)
 */
public class PluginImpl extends Plugin {
    @Override
    public void start() throws Exception {
        super.start();

        Items.XSTREAM.alias("maven2", MavenModule.class);
        Items.XSTREAM.alias("dependency", ModuleDependency.class);
        Items.XSTREAM.alias("maven2-module-set", MavenModule.class);  // this was a bug, but now we need to keep it for compatibility
        Items.XSTREAM.alias("maven2-moduleset", MavenModuleSet.class);

    }
    
    /**
     * @since 1.491
     */
    @Initializer(after=InitMilestone.PLUGINS_STARTED)
    public static void init(){
        // inform the admin if there is a version of the config file provider installed which is not compatible 
        PluginUpdateMonitor.getInstance().ifPluginOlderThenReport("config-file-provider", "2.3", Messages.PluginImpl_updateConfiProvider());        
    }

}
