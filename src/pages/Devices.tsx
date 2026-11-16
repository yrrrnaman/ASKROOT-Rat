export default function DevicesPage() {
  const { data, refetch } = useQuery(['devices'], () => api.devices());
  const [code, setCode] = useState<string | null>(null);

  const enroll = async () => setCode((await api.enroll()).code);

  return (
    <section className="space-y-4">
      <header className="flex items-center gap-3">
        <h1 className="text-lg font-semibold">Devices</h1>
        <button onClick={enroll} className="ml-auto btn-primary">Enroll device</button>
      </header>
      <table className="w-full text-sm">
        <thead className="text-muted text-xs uppercase tracking-wider">
          <tr><th>Host</th><th>Status</th><th>Address</th><th>Arch</th><th>Last seen</th><th /></tr>
        </thead>
        <tbody>
          {data?.content.map((d) => (
            <tr key={d.id} className="border-t border-line hover:bg-white/[0.02]">
              <td className="py-2 font-mono">{d.hostname}<div className="text-xs text-muted">{d.os}</div></td>
              <td><StatusTag status={d.status} /></td>
              <td className="font-mono text-xs text-muted">{d.ipAddress}</td>
              <td className="font-mono text-xs">{d.arch}</td>
              <td className="text-xs text-muted">{relative(d.lastSeenAt)}</td>
              <td className="text-right">
                {d.status === 'PENDING_APPROVAL' && <button onClick={async () => { await api.approve(d.id); refetch(); }} className="btn-ok">Approve</button>}
                {d.status !== 'REVOKED' && <button onClick={async () => { await api.revoke(d.id); refetch(); }} className="btn-danger">Revoke</button>}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
      {code && <EnrollmentModal code={code} onClose={() => setCode(null)} />}
    </section>
  );
}
