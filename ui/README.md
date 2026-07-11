# Rate Sentinel UI

React, TypeScript, Vite, Tailwind CSS, and STOMP/SockJS dashboard for rule management and live rate-limit violations.

## Configuration

Copy `.env.example` to `.env.local` when the backend is hosted on a different origin and set one or both variables:

    VITE_WEBSOCKET_URL=https://your-backend.example/ws-provider
    VITE_API_BASE_URL=https://your-backend.example

When the variables are empty, the client derives `/ws-provider` from `window.location.origin` and calls the control-plane API through the relative `/api/rules` path.

## Commands

    npm install
    npm run build
