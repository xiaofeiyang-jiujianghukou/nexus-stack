# Stage 1: build react app with Vite
FROM crpi-27zlqugq2208c0pz.cn-hangzhou.personal.cr.aliyuncs.com/xiaofeiyang930112/node:20 AS build

WORKDIR /app
COPY package*.json ./

# 配置 npm 使用国内镜像
RUN npm config set registry https://registry.npmmirror.com

RUN npm install
COPY . .
RUN npm run build   # 输出在 /app/dist

# Stage 2: serve with nginx
FROM crpi-27zlqugq2208c0pz.cn-hangzhou.personal.cr.aliyuncs.com/xiaofeiyang930112/nginx:alpine

# 删除默认 nginx 页面
RUN rm -rf /usr/share/nginx/html/*

# 拷贝 Vite 构建产物
COPY --from=build /app/dist /usr/share/nginx/html

# 可选：自定义 nginx 配置
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]