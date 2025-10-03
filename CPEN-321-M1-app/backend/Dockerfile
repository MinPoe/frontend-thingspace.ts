ARG PORT

FROM node:2.6-alpine

WORKDIR /app

COPY package*.json ./

RUN npm ci

COPY . .

EXPOSE $PORT

CMD ["npm", "start"]