import { useEffect, useState } from 'react'
import { Client, type IMessage } from '@stomp/stompjs'
import SockJS from 'sockjs-client'

import type { RateLimitViolationEvent } from '../types/rateLimitViolation'

export type WebSocketStatus = 'connecting' | 'connected' | 'disconnected'

const VIOLATIONS_DESTINATION = '/topic/violations'

const resolveWebSocketUrl = () => {
  const configuredUrl = import.meta.env.VITE_WEBSOCKET_URL?.trim()

  if (configuredUrl) {
    return configuredUrl
  }

  return new URL('/ws-provider', window.location.origin).toString()
}

const isViolationEvent = (value: unknown): value is RateLimitViolationEvent => {
  if (typeof value !== 'object' || value === null) {
    return false
  }

  const event = value as Record<string, unknown>
  return (
    typeof event.clientId === 'string' &&
    typeof event.endpoint === 'string' &&
    typeof event.timestamp === 'string'
  )
}

const parseViolation = (message: IMessage) => {
  const payload: unknown = JSON.parse(message.body)

  if (!isViolationEvent(payload)) {
    throw new Error('Received an invalid rate-limit violation event')
  }

  return payload
}

export const useWebSocket = () => {
  const [violations, setViolations] = useState<RateLimitViolationEvent[]>([])
  const [status, setStatus] = useState<WebSocketStatus>('connecting')
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    const client = new Client({
      reconnectDelay: 5_000,
      heartbeatIncoming: 10_000,
      heartbeatOutgoing: 10_000,
      webSocketFactory: () => new SockJS(resolveWebSocketUrl()),
      onConnect: () => {
        setStatus('connected')
        setError(null)

        client.subscribe(VIOLATIONS_DESTINATION, (message) => {
          try {
            const violation = parseViolation(message)
            setViolations((current) => [violation, ...current])
          } catch (cause) {
            setError(cause instanceof Error ? cause.message : 'Unable to read violation event')
          }
        })
      },
      onDisconnect: () => setStatus('disconnected'),
      onWebSocketClose: () => setStatus('disconnected'),
      onWebSocketError: () => setError('WebSocket connection failed'),
      onStompError: (frame) => {
        setStatus('disconnected')
        setError(frame.headers.message ?? 'STOMP broker error')
      },
    })

    client.activate()

    return () => {
      void client.deactivate()
    }
  }, [])

  return { violations, status, error }
}
