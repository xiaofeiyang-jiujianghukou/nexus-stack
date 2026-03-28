import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
// export default defineConfig({
//   plugins: [react()],
//   server: {
//     proxy: {
//       '/api': {
//         target: 'http://localhost:8080',
//         changeOrigin: true,
//         rewrite: (path) => path.replace(/^\/api/, '')
//       }
//     }
//   }
// })

export default defineConfig({
  plugins: [react()],
  server: {
    host: '0.0.0.0', // 必须开启，否则容器外无法访问前端页面
    proxy: {
      '/api': {
        // 关键点：使用 host.docker.internal 指向宿主机
        target: 'http://172.17.0.1:8080',
        changeOrigin: true,
        configure: (proxy, options) => {
          // 监听代理请求
          proxy.on('proxyReq', (proxyReq, req, res) => {
            console.log('Sending Request to the Target:', req.method, req.url);
          });
          // 监听代理响应
          proxy.on('proxyRes', (proxyRes, req, res) => {
            console.log('Received Response from the Target:', proxyRes.statusCode, req.url);
          });
          // 监听错误
          proxy.on('error', (err, req, res) => {
            console.error('Proxy Error:', err);
          });
        },
        rewrite: (path) => path.replace(/^\/api/, '')
      }
    }
  }
})
