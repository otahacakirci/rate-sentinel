# Rate Sentinel UI

React, TypeScript, Vite, Tailwind CSS, and STOMP/SockJS dashboard for live rate-limit violations.

## Configuration

Copy .env.example to .env.local when the backend is hosted on a different origin and set:

    VITE_WEBSOCKET_URL=https://your-backend.example/ws-provider

When the variable is empty, the client derives /ws-provider from window.location.origin.

## Commands

    npm install
    npm run build
