/**
 * Copyright (c) 2012-2014 André Bargull
 * Alle Rechte vorbehalten / All Rights Reserved.  Use is subject to license terms.
 *
 * <https://github.com/anba/es6draft>
 */
package com.github.anba.es6draft.mozilla;

import static com.github.anba.es6draft.runtime.internal.Properties.createProperties;

import java.nio.file.Path;

import com.github.anba.es6draft.repl.console.ShellConsole;
import com.github.anba.es6draft.repl.global.MozShellGlobalObject;
import com.github.anba.es6draft.runtime.ExecutionContext;
import com.github.anba.es6draft.runtime.Realm;
import com.github.anba.es6draft.runtime.internal.ObjectAllocator;
import com.github.anba.es6draft.runtime.internal.ScriptCache;

/**
 *
 */
public class MozTestGlobalObject extends MozShellGlobalObject {
    protected MozTestGlobalObject(Realm realm, ShellConsole console, Path baseDir, Path script,
            ScriptCache scriptCache) {
        super(realm, console, baseDir, script, scriptCache);
    }

    @Override
    protected void initializeExtensions(ExecutionContext cx) {
        super.initializeExtensions(cx);
        createProperties(cx, cx.getGlobalObject(), new TestingFunctions(), TestingFunctions.class);
    }

    /**
     * Returns an object to allocate new instances of this class.
     * 
     * @param console
     *            the console object
     * @param baseDir
     *            the base directory
     * @param script
     *            the main script file
     * @param scriptCache
     *            the script cache
     * @return the object allocator to construct new global object instances
     */
    public static ObjectAllocator<MozTestGlobalObject> newTestGlobalObjectAllocator(
            final ShellConsole console, final Path baseDir, final Path script,
            final ScriptCache scriptCache) {
        return new ObjectAllocator<MozTestGlobalObject>() {
            @Override
            public MozTestGlobalObject newInstance(Realm realm) {
                return new MozTestGlobalObject(realm, console, baseDir, script, scriptCache);
            }
        };
    }
}
