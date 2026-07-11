import type { RateLimitViolationEvent } from '../types/rateLimitViolation'

type LiveViolationsLogTableProps = {
  violations: RateLimitViolationEvent[]
}

const formatTimestamp = (timestamp: string) => {
  const date = new Date(timestamp)

  if (Number.isNaN(date.getTime())) {
    return timestamp
  }

  return new Intl.DateTimeFormat(undefined, {
    dateStyle: 'medium',
    timeStyle: 'medium',
  }).format(date)
}

export const LiveViolationsLogTable = ({ violations }: LiveViolationsLogTableProps) => (
  <section className="overflow-hidden rounded-2xl border border-slate-800 bg-slate-900/80 shadow-2xl shadow-cyan-950/20 backdrop-blur">
    <div className="flex flex-col gap-3 border-b border-slate-800 px-5 py-5 sm:flex-row sm:items-center sm:justify-between sm:px-7">
      <div>
        <p className="text-xs font-semibold uppercase tracking-[0.24em] text-cyan-400">
          Live stream
        </p>
        <h2 className="mt-1 text-xl font-semibold text-white">Live Violations Log Table</h2>
      </div>
      <div className="flex items-center gap-2 self-start rounded-full border border-slate-700 bg-slate-950/70 px-3 py-1.5 text-xs text-slate-300 sm:self-auto">
        <span className="relative flex h-2 w-2">
          <span className="absolute inline-flex h-full w-full animate-ping rounded-full bg-rose-400 opacity-75" />
          <span className="relative inline-flex h-2 w-2 rounded-full bg-rose-500" />
        </span>
        {violations.length} events captured
      </div>
    </div>

    <div className="overflow-x-auto">
      <table className="min-w-full divide-y divide-slate-800 text-left text-sm">
        <thead className="bg-slate-950/50 text-xs uppercase tracking-wider text-slate-500">
          <tr>
            <th className="px-5 py-4 font-medium sm:px-7">Client ID</th>
            <th className="px-5 py-4 font-medium sm:px-7">Endpoint</th>
            <th className="px-5 py-4 font-medium sm:px-7">Timestamp</th>
          </tr>
        </thead>
        <tbody className="divide-y divide-slate-800/80">
          {violations.length === 0 ? (
            <tr>
              <td className="px-5 py-16 text-center sm:px-7" colSpan={3}>
                <div className="mx-auto max-w-sm">
                  <div className="mx-auto mb-4 flex h-11 w-11 items-center justify-center rounded-xl border border-slate-700 bg-slate-800/70 text-cyan-400">
                    <span className="text-lg">✓</span>
                  </div>
                  <p className="font-medium text-slate-200">No violations received</p>
                  <p className="mt-1 text-sm text-slate-500">
                    Incoming rate-limit events will appear here in real time.
                  </p>
                </div>
              </td>
            </tr>
          ) : (
            violations.map((violation, index) => (
              <tr
                className="bg-slate-900/30 transition-colors hover:bg-slate-800/50"
                key={`${violation.timestamp}-${violation.clientId}-${index}`}
              >
                <td className="whitespace-nowrap px-5 py-4 font-mono text-cyan-300 sm:px-7">
                  {violation.clientId}
                </td>
                <td className="px-5 py-4 font-mono text-slate-300 sm:px-7">
                  {violation.endpoint}
                </td>
                <td className="whitespace-nowrap px-5 py-4 text-slate-400 sm:px-7">
                  {formatTimestamp(violation.timestamp)}
                </td>
              </tr>
            ))
          )}
        </tbody>
      </table>
    </div>
  </section>
)
