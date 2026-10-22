# COMPILA EL FRONT
FROM node:22-alpine AS build
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci
COPY . .
RUN npx ng build --configuration production

# LO SIRVE CON NGINX
FROM nginx:1.27-alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist/residencial-portal/browser /usr/share/nginx/html
EXPOSE 80
