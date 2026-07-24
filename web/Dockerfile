# Vite bakes VITE_* env vars into the bundle at build time (they're not
# runtime-configurable), so the production API URLs have to be passed in as
# build args here rather than as container environment variables.
FROM node:20-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .

ARG VITE_LESSOR_API_URL
ARG VITE_RENTER_API_URL
ARG VITE_NOTIFICATION_API_URL
ENV VITE_LESSOR_API_URL=$VITE_LESSOR_API_URL \
    VITE_RENTER_API_URL=$VITE_RENTER_API_URL \
    VITE_NOTIFICATION_API_URL=$VITE_NOTIFICATION_API_URL
RUN npm run build

# Caddy both serves the built static files and (per deploy/Caddyfile, mounted
# in at runtime by docker-compose.prod.yml) reverse-proxies /api/** to the 3
# backend services - one edge container instead of a separate static server
# plus a separate proxy.
FROM caddy:2-alpine
COPY --from=build /app/dist /srv
EXPOSE 80 443
