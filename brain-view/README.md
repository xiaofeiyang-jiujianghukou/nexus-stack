# GMV 数据大屏

## 项目简介
GMV数据大屏是一个基于React的数据可视化大屏应用，用于实时展示和监控GMV（成交总额）数据。通过直观的图表和统计数据，帮助用户快速了解业务数据的变化趋势。

## 技术栈

### 前端框架
- **React 18** - 用于构建用户界面
- **Vite** - 现代化的前端构建工具，提供快速的开发体验

### UI组件库
- **Ant Design** - 企业级UI设计语言和React组件库
- **@ant-design/charts** - 基于G2Plot的React图表库

### 工具库
- **dayjs** - 轻量级JavaScript日期处理库

### 开发工具
- **npm** - JavaScript包管理器
- **ESLint** - JavaScript代码检查工具

## 实现的功能

### 1. 多时间范围数据展示
支持9个时间范围的数据展示：
- **分钟** - 显示最近一分钟的GMV数据
- **半小时** - 显示最近30分钟的GMV数据
- **一小时** - 显示最近一小时的GMV数据
- **半天** - 显示最近12小时的GMV数据
- **一天** - 显示最近24小时的GMV数据
- **半月** - 显示最近15天的GMV数据
- **一月** - 显示最近一个月的GMV数据
- **半年** - 显示最近6个月的GMV数据
- **一年** - 显示最近一年的GMV数据

### 2. 实时数据更新
- 每3秒自动刷新数据，确保展示最新的GMV信息
- 时间范围切换时立即获取对应数据
- 支持加载状态显示，提升用户体验

### 3. 数据可视化展示
#### 统计卡片
- **当前GMV** - 显示当前时间范围的GMV总额
- **累计GMV** - 显示当前时间范围的累计GMV
- **数据点数** - 显示当前时间范围的数据点数量

#### 图表展示
- **GMV趋势图** - 使用折线图展示GMV随时间的变化趋势
- **GMV分布图** - 使用面积图展示GMV数据的分布情况

#### 数据表格
- **详细数据表格** - 展示每个时间点的GMV数值和占比
- 支持滚动查看，适应不同数据量

### 4. 响应式设计
- 适配不同屏幕尺寸（手机、平板、桌面）
- 灵活的栅格布局，自动调整组件位置

### 5. 错误处理
- 友好的错误提示信息
- 网络请求失败时的降级处理
- 加载状态显示

## 使用方法

### 环境要求
- **Node.js** - 版本 >= 16.0.0
- **npm** - 版本 >= 8.0.0
- **现代浏览器** - Chrome、Firefox、Safari、Edge等

### 安装依赖
```bash
npm install
```

### 启动开发服务器
```bash
npm run dev
```

开发服务器将在 http://localhost:5173 启动

### 构建生产版本
```bash
npm run build
```

构建产物将生成在 `dist` 目录

### 预览生产版本
```bash
npm run preview
```

### 后端接口要求

#### 接口地址
```
GET http://localhost:8080/dashboard/gmvAll
```

#### 返回数据格式
```json
{
  "seconds": 1226.68,
  "minute": 3601.99,
  "halfHour": 264975.72,
  "hour": 477165.23,
  "halfDay": 3335697.45,
  "day": 3335697.45,
  "week": 3335697.45,
  "halfMonth": 3335697.45,
  "month": 3335697.45,
  "halfYear": 3335697.45,
  "year": 3335697.45,
  "overview": {
    "gmv_last_1m": [],
    "gmv_last_30m": [],
    "gmv_last_1h": [],
    "gmv_last_12h": [],
    "gmv_last_1d": [],
    "gmv_last_15d": [],
    "gmv_last_1mouth": [],
    "gmv_last_6mouth": [],
    "gmv_last_1y": []
  }
}
```

### 时间范围与数据字段映射

| 时间范围 | 汇总字段 | 详细数据字段 |
|---------|----------|------------|
| 分钟 | minute | gmv_last_1m |
| 半小时 | halfHour | gmv_last_30m |
| 一小时 | hour | gmv_last_1h |
| 半天 | halfDay | gmv_last_12h |
| 一天 | day | gmv_last_1d |
| 半月 | halfMonth | gmv_last_15d |
| 一月 | month | gmv_last_1mouth |
| 半年 | halfYear | gmv_last_6mouth |
| 一年 | year | gmv_last_1y |

### Vite代理配置

项目已配置Vite代理，自动处理跨域问题：

```javascript
// vite.config.js
server: {
  proxy: {
    '/api': {
      target: 'http://localhost:8080',
      changeOrigin: true,
      rewrite: (path) => path.replace(/^\/api/, '')
    }
  }
}
```

前端请求 `/api/dashboard/gmvAll` 会被代理到 `http://localhost:8080/dashboard/gmvAll`

### 项目结构
```
brain-view/
├── src/
│   ├── components/
│   │   └── GMVDashboard.jsx    # GMV大屏主组件
│   ├── App.jsx                     # 应用入口组件
│   ├── main.jsx                     # React应用入口
│   └── index.css                    # 全局样式
├── public/                           # 静态资源
├── index.html                        # HTML模板
├── vite.config.js                   # Vite配置文件
├── package.json                      # 项目依赖配置
└── README.md                        # 项目说明文档
```

### 主要组件说明

#### GMVDashboard组件
核心组件，负责：
- 数据获取和状态管理
- 时间范围切换逻辑
- 数据可视化渲染
- 实时更新机制

主要状态：
- `timeRange` - 当前选择的时间范围
- `gmvData` - GMV详细数据
- `currentGmv` - 当前GMV值
- `totalGmv` - 累计GMV值
- `loading` - 加载状态
- `error` - 错误信息

### 自定义配置

#### 修改实时更新间隔
在 `GMVDashboard.jsx` 中修改：
```javascript
const interval = setInterval(() => {
  fetchGMVData();
}, 3000); // 修改这里的数值（毫秒）
```

#### 修改后端接口地址
在 `GMVDashboard.jsx` 中修改：
```javascript
const response = await fetch('/api/dashboard/gmvAll');
```

在 `vite.config.js` 中修改：
```javascript
'/api': {
  target: 'http://your-backend-url:port',
  changeOrigin: true,
  rewrite: (path) => path.replace(/^\/api/, '')
}
```

### 常见问题

#### 1. 后端接口无法访问
**问题**：页面显示"后端服务可能未运行"

**解决方案**：
- 确保后端服务已启动
- 检查后端服务端口是否为8080
- 检查防火墙设置

#### 2. 跨域问题
**问题**：浏览器控制台显示跨域错误

**解决方案**：
- Vite代理已配置，确保使用 `/api` 前缀访问接口
- 不要直接使用 `http://localhost:8080` 访问接口

#### 3. 数据不更新
**问题**：切换时间范围后数据不刷新

**解决方案**：
- 确保后端返回对应时间范围的数据
- 检查浏览器控制台是否有错误信息

### 性能优化建议

1. **减少API请求频率**：根据实际需求调整实时更新间隔
2. **数据缓存**：对相同时间范围的数据进行缓存
3. **图表优化**：大数据量时考虑数据抽样
4. **懒加载**：对图表组件进行懒加载

### 扩展功能建议

1. **数据导出**：支持导出Excel、CSV等格式
2. **自定义时间范围**：允许用户选择任意时间段
3. **多维度分析**：增加地区、产品类别等维度
4. **异常告警**：GMV异常波动时发送告警
5. **数据对比**：支持同比、环比分析
6. **主题切换**：支持亮色/暗色主题

## 技术支持

如有问题或建议，请联系开发团队。

## 许可证

本项目仅供学习和参考使用。