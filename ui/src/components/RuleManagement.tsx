import { useCallback, useEffect, useState, type FormEvent } from 'react'
import type { RateLimitRule } from '../types/rateLimitRule'

type RuleDraft = Omit<RateLimitRule, 'id'>

const initialRule: RuleDraft = {
  clientId: '',
  endpoint: '',
  allowedLimit: 100,
  windowSeconds: 60,
  active: true,
}

const rulesEndpoint = () => {
  const baseUrl = import.meta.env.VITE_API_BASE_URL?.trim().replace(/\/+$/, '')
  return baseUrl ? `${baseUrl}/api/rules` : '/api/rules'
}

const isRateLimitRule = (value: unknown): value is RateLimitRule => {
  if (!value || typeof value !== 'object') {
    return false
  }

  const rule = value as Record<string, unknown>
  return (
    (rule.id === undefined || typeof rule.id === 'number') &&
    typeof rule.clientId === 'string' &&
    typeof rule.endpoint === 'string' &&
    typeof rule.allowedLimit === 'number' &&
    typeof rule.windowSeconds === 'number' &&
    typeof rule.active === 'boolean'
  )
}

export function RuleManagement() {
  const [rules, setRules] = useState<RateLimitRule[]>([])
  const [draft, setDraft] = useState<RuleDraft>(initialRule)
  const [isLoading, setIsLoading] = useState(true)
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const fetchRules = useCallback(async (signal?: AbortSignal) => {
    setIsLoading(true)
    setError(null)

    try {
      const response = await fetch(rulesEndpoint(), { signal })
      if (!response.ok) {
        throw new Error(`Rules could not be loaded (HTTP ${response.status}).`)
      }

      const payload: unknown = await response.json()
      if (!Array.isArray(payload) || !payload.every(isRateLimitRule)) {
        throw new Error('The rules API returned an unexpected response.')
      }

      setRules(payload)
    } catch (cause) {
      if (cause instanceof DOMException && cause.name === 'AbortError') {
        return
      }

      setError(cause instanceof Error ? cause.message : 'Rules could not be loaded.')
    } finally {
      if (!signal?.aborted) {
        setIsLoading(false)
      }
    }
  }, [])

  useEffect(() => {
    const controller = new AbortController()
    void fetchRules(controller.signal)
    return () => controller.abort()
  }, [fetchRules])

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setIsSubmitting(true)
    setError(null)

    try {
      const response = await fetch(rulesEndpoint(), {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(draft),
      })

      if (!response.ok) {
        throw new Error(`Rule could not be created (HTTP ${response.status}).`)
      }

      const createdRule: unknown = await response.json()
      if (!isRateLimitRule(createdRule)) {
        throw new Error('The rules API returned an unexpected response.')
      }

      setRules((currentRules) => [createdRule, ...currentRules])
      setDraft(initialRule)
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : 'Rule could not be created.')
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <section className="overflow-hidden rounded-3xl border border-slate-800 bg-slate-900/80 shadow-2xl shadow-cyan-950/20 backdrop-blur">
      <div className="border-b border-slate-800 px-6 py-5">
        <p className="text-xs font-semibold uppercase tracking-[0.24em] text-cyan-400">
          Control plane
        </p>
        <h2 className="mt-2 text-xl font-semibold text-white">Rate limit rules</h2>
        <p className="mt-1 text-sm text-slate-400">
          Create and review the policies enforced by Rate Sentinel.
        </p>
      </div>

      <form className="grid gap-4 border-b border-slate-800 p-6 sm:grid-cols-2" onSubmit={handleSubmit}>
        <label className="grid gap-2 text-sm font-medium text-slate-300">
          Client ID
          <input
            className="rounded-xl border border-slate-700 bg-slate-950/70 px-3 py-2.5 text-white outline-none transition placeholder:text-slate-600 focus:border-cyan-500 focus:ring-2 focus:ring-cyan-500/20"
            value={draft.clientId}
            onChange={(event) => setDraft((rule) => ({ ...rule, clientId: event.target.value }))}
            placeholder="partner-mobile"
            required
          />
        </label>

        <label className="grid gap-2 text-sm font-medium text-slate-300">
          Endpoint
          <input
            className="rounded-xl border border-slate-700 bg-slate-950/70 px-3 py-2.5 text-white outline-none transition placeholder:text-slate-600 focus:border-cyan-500 focus:ring-2 focus:ring-cyan-500/20"
            value={draft.endpoint}
            onChange={(event) => setDraft((rule) => ({ ...rule, endpoint: event.target.value }))}
            placeholder="/api/resource"
            required
          />
        </label>

        <label className="grid gap-2 text-sm font-medium text-slate-300">
          Allowed requests
          <input
            className="rounded-xl border border-slate-700 bg-slate-950/70 px-3 py-2.5 text-white outline-none transition focus:border-cyan-500 focus:ring-2 focus:ring-cyan-500/20"
            type="number"
            min="1"
            value={draft.allowedLimit}
            onChange={(event) =>
              setDraft((rule) => ({ ...rule, allowedLimit: event.target.valueAsNumber }))
            }
            required
          />
        </label>

        <label className="grid gap-2 text-sm font-medium text-slate-300">
          Window (seconds)
          <input
            className="rounded-xl border border-slate-700 bg-slate-950/70 px-3 py-2.5 text-white outline-none transition focus:border-cyan-500 focus:ring-2 focus:ring-cyan-500/20"
            type="number"
            min="1"
            value={draft.windowSeconds}
            onChange={(event) =>
              setDraft((rule) => ({ ...rule, windowSeconds: event.target.valueAsNumber }))
            }
            required
          />
        </label>

        <label className="flex items-center gap-3 text-sm font-medium text-slate-300 sm:col-span-2">
          <input
            className="size-4 rounded border-slate-600 bg-slate-950 text-cyan-500 focus:ring-cyan-500/40"
            type="checkbox"
            checked={draft.active}
            onChange={(event) => setDraft((rule) => ({ ...rule, active: event.target.checked }))}
          />
          Activate rule immediately
        </label>

        {error ? (
          <p className="rounded-xl border border-rose-500/20 bg-rose-500/10 px-3 py-2 text-sm text-rose-300 sm:col-span-2">
            {error}
          </p>
        ) : null}

        <button
          className="rounded-xl bg-cyan-400 px-4 py-2.5 text-sm font-semibold text-slate-950 transition hover:bg-cyan-300 disabled:cursor-not-allowed disabled:opacity-50 sm:col-span-2"
          type="submit"
          disabled={isSubmitting}
        >
          {isSubmitting ? 'Creating rule...' : 'Add rule'}
        </button>
      </form>

      <div className="overflow-x-auto">
        <table className="min-w-full divide-y divide-slate-800 text-left text-sm">
          <thead className="bg-slate-950/40 text-xs uppercase tracking-wider text-slate-500">
            <tr>
              <th className="px-5 py-3 font-medium">Client / endpoint</th>
              <th className="px-5 py-3 font-medium">Limit</th>
              <th className="px-5 py-3 font-medium">Status</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-800/80">
            {isLoading ? (
              <tr>
                <td className="px-5 py-8 text-center text-slate-500" colSpan={3}>
                  Loading rules...
                </td>
              </tr>
            ) : rules.length === 0 ? (
              <tr>
                <td className="px-5 py-8 text-center text-slate-500" colSpan={3}>
                  No rules configured yet.
                </td>
              </tr>
            ) : (
              rules.map((rule, index) => (
                <tr key={rule.id ?? `${rule.clientId}-${rule.endpoint}-${index}`} className="text-slate-300">
                  <td className="px-5 py-4">
                    <p className="font-medium text-white">{rule.clientId}</p>
                    <p className="mt-1 font-mono text-xs text-slate-500">{rule.endpoint}</p>
                  </td>
                  <td className="whitespace-nowrap px-5 py-4">
                    {rule.allowedLimit} / {rule.windowSeconds}s
                  </td>
                  <td className="px-5 py-4">
                    <span
                      className={`inline-flex rounded-full px-2.5 py-1 text-xs font-semibold ${
                        rule.active
                          ? 'bg-emerald-500/10 text-emerald-300'
                          : 'bg-slate-700/50 text-slate-400'
                      }`}
                    >
                      {rule.active ? 'Active' : 'Inactive'}
                    </span>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </section>
  )
}
