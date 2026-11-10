// This file is part of IBC.
// Copyright (C) 2026 lifelongedge (edge-ibc fork)
// For conditions of distribution and use, see copyright notice in COPYING.txt

// IBC is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.

// IBC is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.

// You should have received a copy of the GNU General Public License
// along with IBC.  If not, see <http://www.gnu.org/licenses/>.

package ibcalpha.ibc;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Runs the IbPasswordCommand setting: an executable (or a .cmd / .bat on Windows) that prints the
 * IBKR password on its first line of standard output, so the password need not be stored in this
 * file. The command takes no arguments; its output is never logged.
 */
class PasswordCommand {

    private static final int TIMEOUT_SECONDS = 60;

    static String run(String command) {
        List<String> argv = new ArrayList<>();
        String lower = command.toLowerCase();
        if (System.getProperty("os.name").startsWith("Windows") && (lower.endsWith(".cmd") || lower.endsWith(".bat"))) {
            argv.add("cmd.exe");
            argv.add("/d");
            argv.add("/c");
        }
        argv.add(command);
        try {
            ProcessBuilder pb = new ProcessBuilder(argv);
            pb.redirectInput(ProcessBuilder.Redirect.from(new File(System.getProperty("os.name").startsWith("Windows") ? "NUL" : "/dev/null")));
            pb.redirectError(ProcessBuilder.Redirect.DISCARD);
            Process p = pb.start();
            // the output is one short line, well inside the pipe's buffer, so wait first: a helper that hangs cannot hang the login
            if (!p.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                p.destroyForcibly();
                Utils.logError("IbPasswordCommand did not finish within " + TIMEOUT_SECONDS + " seconds");
                return "";
            }
            String line;
            try (BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
                line = r.readLine();
            }
            if (p.exitValue() != 0 || line == null || line.isEmpty()) {
                Utils.logError("IbPasswordCommand gave no password (exit code " + p.exitValue() + ")");
                return "";
            }
            Utils.logToConsole("IbPasswordCommand supplied the password");
            return line;
        } catch (Exception e) {
            Utils.logError("IbPasswordCommand could not be run: " + e.getClass().getSimpleName());
            return "";
        }
    }
}
