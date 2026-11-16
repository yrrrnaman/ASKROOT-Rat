import { useEffect, useRef, useState } from 'react';
import { Terminal as XTerm } from 'xterm';
import { FitAddon } from 'xterm-addon-fit';
import 'xterm/css/xterm.css';
import { api } from '../services/api';
import { useDeviceStore } from '../hooks/useDeviceStore';

const VERBS = ['help','devices','device-info','system-info','cpu-status','memory-status','disk-status',
  'network-status','process-list','service-status','uptime','file-list','download-file','upload-file',
  'disconnect','clear'];

export default function TerminalPage() {
  const host = useRef<HTMLDivElement>(null);
  const term = useRef<XTerm>();
  const deviceId = useDeviceStore((s) => s.selected);
  const [busy, setBusy] = useState(false);

  useEffect(() => {
    const t = new XTerm({
      fontFamily: "'JetBrains Mono', monospace",
      fontSize: 13,
      theme: { background: '#050506', foreground: '#86efac', cursor: '#EF233C',
               selectionBackground: '#2a1519' },
      cursorBlink: true,
      convertEol: true,
    });
    const fit = new FitAddon();
    t.loadAddon(fit);
    t.open(host.current!);
    fit.fit();
    term.current = t;

    const ro = new ResizeObserver(() => fit.fit());
    ro.observe(host.current!);

    let line = '';
    t.writeln('\x1b[31mAskRoot remote administration console\x1b[0m');
    t.writeln('\x1b[90mtype "help" for the command surface\x1b[0m\r\n');
    prompt(t);

    t.onData(async (data) => {
      if (busy) return;
      if (data === '\r') {
        t.write('\r\n');
        if (line.trim()) await run(t, line.trim());
        line = '';
        prompt(t);
      } else if (data === '\t') {
        const m = VERBS.find((v) => v.startsWith(line.trim()) && line.trim());
        if (m) { t.write('\b \b'.repeat(line.length) + m); line = m; }
      } else if (data === '\u007f') {
        if (line) { line = line.slice(0, -1); t.write('\b \b'); }
      } else if (data >= ' ') { line += data; t.write(data); }
    });

    return () => { ro.disconnect(); t.dispose(); };
  }, [deviceId]);

  async function run(t: XTerm, command: string) {
    if (!deviceId) { t.writeln('\x1b[33mno device selected\x1b[0m'); return; }
    setBusy(true);
    t.writeln('\x1b[90m› dispatched, awaiting agent\x1b[0m');
    try {
      const r = await api.execute(deviceId, command);
      t.writeln(`\x1b[${r.status === 'OK' ? '32' : '31'}m✓ ${r.status} (${r.durationMs}ms)\x1b[0m`);
      t.writeln(r.output.replace(/\n/g, '\r\n'));
    } catch (e: any) {
      t.writeln(`\x1b[31mdispatch: ${e.message}\x1b[0m`);
    } finally { setBusy(false); }
  }

  return (
    <div className="flex h-full flex-col gap-3">
      <header className="flex items-center gap-3">
        <h1 className="text-lg font-semibold">AskRoot terminal</h1>
        <span className="ml-auto font-mono text-xs text-muted">
          {busy ? <span className="text-accent">executing…</span> : 'idle'}
        </span>
      </header>
      <div ref={host} className="min-h-[420px] flex-1 rounded-lg border border-accent/25 bg-[#050506] p-3" />
    </div>
  );
}
