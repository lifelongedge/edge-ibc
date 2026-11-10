# edge-ibc changes from upstream IBC

## 3.24.2-le.1 (2026-11-10)

From upstream 3.24.2 (IbcAlpha/IBC `fc6d917`, the archived final state).

- **`IbPasswordCommand`** (`resources/config.ini`, `DefaultLoginManager.getTWSPasswordFromSettings`,
  new `PasswordCommand`): when `IbPassword` is empty and `IbPasswordCommand` names an executable, IBC
  runs it at the first login of the process (a `.cmd`/`.bat` through `cmd.exe /d /c` on Windows; no
  arguments; stdin from the null device; stderr discarded), waits up to 60 s, and takes the first line
  of its standard output as the password when it exits 0. The value is kept in memory for the life of
  the process, so a relogin after a 2FA timeout inside the same JVM does not run it again; every new
  JVM that `StartIBC.bat` / `ibcstart.sh` starts (auto-restart, cold restart, 2FA-timeout restart)
  runs it once. The output is never logged; a failure logs the exit code or the exception class only.
  Edge S2 points it at a helper that decrypts the password with Windows DPAPI.
- **`SwingUtils.getTexts` skips `JPasswordField`**: the window text it collects is written to the IBC
  log, so a password field in an unrecognised window could otherwise reach the log.
- Version `3.24.2-le.1` (`resources/version`, `IbcVersionInfo`, `build.xml`); README fork notice;
  `edge/build.sh`.
