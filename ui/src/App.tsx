import { LiveViolationsLogTable } from './components/LiveViolationsLogTable'
import { RuleManagement } from './components/RuleManagement'
import { useWebSocket, type WebSocketStatus } from './hooks/useWebSocket'

const statusStyles: Record<WebSocketStatus, string> = {
  connected: 'border-emerald-400/30 bg-emerald-400/10 text-emerald-300',
  connecting: 'border-amber-400/30 bg-amber-400/10 text-amber-300',
  disconnected: 'border-rose-400/30 bg-rose-400/10 text-rose-300',
}

const statusLabels: Record<WebSocketStatus, string> = {
  connected: 'Live connection',
  connecting: 'Connecting',
  disconnected: 'Reconnecting',
}

function App() {
  const { violations, status, error } = useWebSocket()

  return (
    <main className="min-h-screen px-4 py-8 sm:px-6 lg:px-8 lg:py-12">
      <div className="mx-auto max-w-[1600px]">
        <header className="mb-8 flex flex-col gap-6 sm:mb-10 sm:flex-row sm:items-end sm:justify-between">
          <div>
            <div className="mb-4 inline-flex items-center gap-2 rounded-full border border-cyan-400/20 bg-cyan-400/10 px-3 py-1 text-xs font-semibold uppercase tracking-[0.2em] text-cyan-300">
              <span className="h-1.5 w-1.5 rounded-full bg-cyan-400" />
              Distributed traffic control
            </div>
            <h1 className="max-w-4xl text-3xl font-bold tracking-tight text-white sm:text-4xl lg:text-5xl">
              Rate Sentinel - Real-Time Dashboard
            </h1>
            <p className="mt-3 max-w-2xl text-sm leading-6 text-slate-400 sm:text-base">
              Observe live rate-limit violations across clients and endpoints from one operational view.
            </p>
          </div>

          <div
            className={
              'inline-flex w-fit items-center gap-2 rounded-full border px-3.5 py-2 text-sm font-medium ' +
              statusStyles[status]
            }
          >
            <span className="h-2 w-2 rounded-full bg-current" />
            {statusLabels[status]}
          </div>
        </header>

        {error && (
          <div className="mb-6 rounded-xl border border-rose-400/20 bg-rose-400/10 px-4 py-3 text-sm text-rose-200">
            {error}
          </div>
        )}

        <div className="mb-6 grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          <div className="rounded-2xl border border-slate-800 bg-slate-900/70 p-5">
            <p className="text-xs font-medium uppercase tracking-wider text-slate-500">
              Total violations
            </p>
            <p className="mt-2 text-3xl font-semibold text-white">{violations.length}</p>
          </div>
          <div className="rounded-2xl border border-slate-800 bg-slate-900/70 p-5">
            <p className="text-xs font-medium uppercase tracking-wider text-slate-500">Transport</p>
            <p className="mt-2 text-lg font-semibold text-slate-100">STOMP + SockJS</p>
          </div>
          <div className="rounded-2xl border border-slate-800 bg-slate-900/70 p-5 sm:col-span-2 lg:col-span-1">
            <p className="text-xs font-medium uppercase tracking-wider text-slate-500">Channel</p>
            <p className="mt-2 font-mono text-sm font-medium text-cyan-300">/topic/violations</p>
          </div>
        </div>

        <div className="grid items-start gap-6 xl:grid-cols-[minmax(0,1fr)_minmax(0,1.15fr)]">
          <RuleManagement />
          <LiveViolationsLogTable violations={violations} />
        </div>
      </div>
    </main>
  )
}

export default App
