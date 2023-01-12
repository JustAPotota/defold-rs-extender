package com.defold.extender;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RustConfig {
    // Map of Defold platform name -> Rust platform name
    private static final HashMap<String, String> platformMap = new HashMap<>();
    static {
        platformMap.put("x86_64-linux", "x86_64-unknown-linux-gnu");
        platformMap.put("x86_64-win32", "x86_64-pc-windows-msvc");
        platformMap.put("x86-win32", "i686-pc-windows-msvc");
    }

    private static final HashMap<String, RustConfig> platforms = new HashMap<>();
    static {
        platforms.put("x86_64-linux", new RustConfig(
                "x86_64-unknown-linux-gnu"
        ));

        platforms.put("js-web", new RustConfig(
                "asmjs-unknown-emscripten"
        ));

        platforms.put("wasm-web", new RustConfig(
                "wasm32-unknown-emscripten",
                new String[]{
                        "/opt/platformsdk/emsdk-2.0.11/upstream/emscripten/system/include/compat",
                        "/usr/include"
                }
        ));

        // TODO grab include paths from build.yml
        platforms.put("x86_64-win32", new RustConfig(
                "x86_64-pc-windows-msvc",
                new String[]{
                        "/opt/platformsdk/Win32/MicrosoftVisualStudio2019/VC/Tools/MSVC/14.25.28610/include",
                        "/opt/platformsdk/Win32/MicrosoftVisualStudio2019/VC/Tools/MSVC/14.25.28610/atlmfc/include",
                        "/opt/platformsdk/Win32/WindowsKits/10/Include/10.0.18362.0/ucrt",
                        "/opt/platformsdk/Win32/WindowsKits/10/Include/10.0.18362.0/winrt",
                        "/opt/platformsdk/Win32/WindowsKits/10/Include/10.0.18362.0/um",
                        "/opt/platformsdk/Win32/WindowsKits/10/Include/10.0.18362.0/shared"
                },
                new String[]{"WIN32", "WINVER=0x0600"},
                new String[]{"bcrypt", "userenv"}
        ));

        platforms.put("x86-win32", new RustConfig(
                "i686-pc-windows-msvc",
                forPlatform("x86_64-win32").sysSearchPaths,
                forPlatform("x86_64-win32").defines,
                forPlatform("x86_64-win32").libs
        ));
    }

    public static RustConfig forPlatform(String defoldName) {
        return platforms.get(defoldName);
    }

    public static String rustName(String defoldName) {
        return platformMap.get(defoldName);
    }

    public void setClangArgs(ProcessExecutor executor) {
        StringBuilder arg = new StringBuilder();
        for (String path : this.sysSearchPaths) {
            arg.append("-isystem").append(path).append(" ");
        }
        for (String define : this.defines) {
            arg.append("-D").append(define).append(" ");
        }
        if (System.getenv("RUST_DEBUG") != null) {
            System.out.println("BINDGEN_EXTRA_CLANG_ARGS=" + arg);
        }
        executor.putEnv("BINDGEN_EXTRA_CLANG_ARGS", arg.toString());
    }

    public void putLibs(Map<String, Object> context) {
        List<String> libs = (List<String>) context.get("libs");
        libs.addAll(Arrays.asList(this.libs));
        context.put("libs", libs);
    }

    public final String rustName;
    public final String[] sysSearchPaths;
    public final String[] defines;
    public final String[] libs;

    private RustConfig(String rustName, String[] sysSearchPaths, String[] defines, String[] libs) {
        this.rustName = rustName;
        this.sysSearchPaths = sysSearchPaths;
        this.defines = defines;
        this.libs = libs;
    }

    private RustConfig(String rustName) {
        this(rustName, new String[]{}, new String[]{}, new String[]{});
    }
    private RustConfig(String rustName, String[] sysSearchPaths) {
        this(rustName, sysSearchPaths, new String[]{}, new String[]{});
    }
}
