export interface RateLimitRule {
  id?: number
  clientId: string
  endpoint: string
  allowedLimit: number
  windowSeconds: number
  active: boolean
}
