import { useState, useEffect } from 'react';
import { Card, Select, Statistic, Row, Col, Typography } from 'antd';
import { Line, Area } from '@ant-design/charts';
import dayjs from 'dayjs';

const { Title } = Typography;

const TIME_RANGES = [
  { label: '分钟', value: '1m' },
  { label: '半小时', value: '30m' },
  { label: '一小时', value: '1h' },
  { label: '半天', value: '12h' },
  { label: '一天', value: '1d' },
  { label: '半月', value: '15d' },
  { label: '一月', value: '1month' },
  { label: '半年', value: '6m' },
  { label: '一年', value: '1y' }
];

const GMVDashboard = () => {
  const [timeRange, setTimeRange] = useState('1d');
  const [gmvData, setGmvData] = useState([]);
  const [currentGmv, setCurrentGmv] = useState(0);
  const [totalGmv, setTotalGmv] = useState(0);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const fetchGMVData = async () => {
    setLoading(true);
    setError(null);
    try {
      console.log('=== Starting GMV data fetch ===');
      console.log('Fetching GMV data from:', '/api/dashboard/gmvAll');
      
      const response = await fetch('/api/dashboard/gmvAll');
      
      console.log('Response status:', response.status);
      console.log('Response headers:', Object.fromEntries(response.headers));
      
      if (!response.ok) {
        const errorText = await response.text();
        console.error('Error response text:', errorText);
        throw new Error(`Failed to fetch GMV data: ${response.status} ${response.statusText}`);
      }
      
      const data = await response.json();
      console.log('Received data:', data);
      console.log('Data keys:', Object.keys(data));
      console.log('Overview keys:', data.overview ? Object.keys(data.overview) : 'No overview');
      console.log('=== GMV data fetch completed ===');
      
      // 根据时间范围获取对应的数据
      let selectedData = [];
      let currentValue = 0;
      let totalValue = 0;

      switch (timeRange) {
        case '1m':
          currentValue = data.minute || 0;
          totalValue = data.minute || 0;
          if (data.overview && data.overview.gmv_last_1m) {
            selectedData = data.overview.gmv_last_1m.map(item => ({
              time: dayjs(item.statTime).format('HH:mm:ss'),
              gmv: item.gmv,
              date: dayjs(item.statTime)
            }));
          }
          break;
        case '30m':
          currentValue = data.halfHour || 0;
          totalValue = data.halfHour || 0;
          if (data.overview && data.overview.gmv_last_30m) {
            selectedData = data.overview.gmv_last_30m.map(item => ({
              time: dayjs(item.statTime).format('HH:mm'),
              gmv: item.gmv,
              date: dayjs(item.statTime)
            }));
          }
          break;
        case '1h':
          currentValue = data.hour || 0;
          totalValue = data.hour || 0;
          if (data.overview && data.overview.gmv_last_1h) {
            selectedData = data.overview.gmv_last_1h.map(item => ({
              time: dayjs(item.statTime).format('HH:mm'),
              gmv: item.gmv,
              date: dayjs(item.statTime)
            }));
          }
          break;
        case '12h':
          currentValue = data.halfDay || 0;
          totalValue = data.halfDay || 0;
          if (data.overview && data.overview.gmv_last_12h) {
            selectedData = data.overview.gmv_last_12h.map(item => ({
              time: dayjs(item.statTime).format('HH:mm'),
              gmv: item.gmv,
              date: dayjs(item.statTime)
            }));
          }
          break;
        case '1d':
          currentValue = data.day || 0;
          totalValue = data.day || 0;
          if (data.overview && data.overview.gmv_last_1d) {
            selectedData = data.overview.gmv_last_1d.map(item => ({
              time: dayjs(item.statTime).format('HH:mm'),
              gmv: item.gmv,
              date: dayjs(item.statTime)
            }));
          }
          break;
        case '15d':
          currentValue = data.halfMonth || 0;
          totalValue = data.halfMonth || 0;
          if (data.overview && data.overview.gmv_last_15d) {
            selectedData = data.overview.gmv_last_15d.map(item => ({
              time: dayjs(item.statTime).format('MM-DD'),
              gmv: item.gmv,
              date: dayjs(item.statTime)
            }));
          }
          break;
        case '1month':
          currentValue = data.month || 0;
          totalValue = data.month || 0;
          if (data.overview && data.overview.gmv_last_1mouth) {
            selectedData = data.overview.gmv_last_1mouth.map(item => ({
              time: dayjs(item.statTime).format('MM-DD'),
              gmv: item.gmv,
              date: dayjs(item.statTime)
            }));
          }
          break;
        case '6m':
          currentValue = data.halfYear || 0;
          totalValue = data.halfYear || 0;
          if (data.overview && data.overview.gmv_last_6mouth) {
            selectedData = data.overview.gmv_last_6mouth.map(item => ({
              time: dayjs(item.statTime).format('MM-DD'),
              gmv: item.gmv,
              date: dayjs(item.statTime)
            }));
          }
          break;
        case '1y':
          currentValue = data.year || 0;
          totalValue = data.year || 0;
          if (data.overview && data.overview.gmv_last_1y) {
            selectedData = data.overview.gmv_last_1y.map(item => ({
              time: dayjs(item.statTime).format('MM-DD'),
              gmv: item.gmv,
              date: dayjs(item.statTime)
            }));
          }
          break;
        default:
          currentValue = data.day || 0;
          totalValue = data.day || 0;
          if (data.overview && data.overview.gmv_last_1d) {
            selectedData = data.overview.gmv_last_1d.map(item => ({
              time: dayjs(item.statTime).format('HH:mm'),
              gmv: item.gmv,
              date: dayjs(item.statTime)
            }));
          }
      }

      // 如果没有对应的数据，使用后端提供的汇总数据创建简单的展示数据
      if (selectedData.length === 0) {
        console.log('No detailed data for time range:', timeRange, 'using summary data:', currentValue);
        // 使用后端提供的汇总数据创建单个数据点
        selectedData = [{
          time: dayjs().format('YYYY-MM-DD HH:mm'),
          gmv: currentValue,
          date: dayjs()
        }];
      }

      setGmvData(selectedData);
      setCurrentGmv(currentValue);
      setTotalGmv(totalValue);
    } catch (err) {
      setError(err.message);
      console.error('Error fetching GMV data:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchGMVData();
  }, [timeRange]);

  useEffect(() => {
    const interval = setInterval(() => {
      fetchGMVData();
    }, 3000);

    return () => clearInterval(interval);
  }, [timeRange]);

  const lineConfig = {
    data: gmvData,
    xField: 'time',
    yField: 'gmv',
    smooth: true,
    area: {
      style: {
        fill: 'l(270) 0:#ffffff 0.5:#7ec2f3 1:#1890ff',
      },
    },
    line: {
      color: '#1890ff',
      size: 3,
    },
    point: {
      size: 4,
      shape: 'circle',
      style: {
        fill: '#fff',
        stroke: '#1890ff',
        lineWidth: 2,
      },
    },
    xAxis: {
      label: {
        style: {
          fill: '#666',
        },
      },
    },
    yAxis: {
      label: {
        formatter: (v) => `${(v / 10000).toFixed(0)}万`,
        style: {
          fill: '#666',
        },
      },
    },
    tooltip: {
      title: (datum) => `时间: ${datum.time}`,
      items: [(datum) => ({
        name: 'GMV',
        value: `¥${datum.gmv.toLocaleString()}`,
      })],
    },
    animation: {
      appear: {
        animation: 'path-in',
        duration: 1000,
      },
    },
  };

  const areaConfig = {
    data: gmvData,
    xField: 'time',
    yField: 'gmv',
    smooth: true,
    areaStyle: {
      fill: 'l(270) 0:#ffffff 0.5:#7ec2f3 1:#1890ff',
    },
    line: {
      color: '#1890ff',
      size: 2,
    },
    xAxis: {
      label: {
        style: {
          fill: '#666',
        },
      },
    },
    yAxis: {
      label: {
        formatter: (v) => `${(v / 10000).toFixed(0)}万`,
        style: {
          fill: '#666',
        },
      },
    },
    tooltip: {
      title: (datum) => `时间: ${datum.time}`,
      items: [(datum) => ({
        name: 'GMV',
        value: `¥${datum.gmv.toLocaleString()}`,
      })],
    },
  };

  return (
    <div style={{ 
      padding: '24px',
      background: '#f0f2f5',
      minHeight: '100vh'
    }}>
      <Card 
        style={{ 
          marginBottom: '24px',
          background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
          border: 'none'
        }}
      >
        <div style={{ color: '#fff' }}>
          <Title level={2} style={{ color: '#fff', marginBottom: '16px' }}>
            GMV 数据大屏
          </Title>
          <Select
            value={timeRange}
            onChange={setTimeRange}
            options={TIME_RANGES}
            style={{ 
              width: 200,
              background: 'rgba(255, 255, 255, 0.2)',
              borderRadius: '4px'
            }}
            size="large"
          />
        </div>
      </Card>

      {error && (
        <Card style={{ marginBottom: '24px' }}>
          <div style={{ color: '#ff4d4f' }}>
            <p>错误: {error}</p>
            <p>后端服务可能未运行，请启动后端服务后刷新页面</p>
          </div>
        </Card>
      )}

      <Row gutter={[24, 24]}>
        <Col xs={24} sm={12} md={8}>
          <Card>
            <Statistic
              title="当前 GMV"
              value={currentGmv}
              precision={0}
              styles={{ content: { color: '#1890ff', fontSize: '28px' } }}
              prefix="¥"
              suffix=""
              loading={loading}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} md={8}>
          <Card>
            <Statistic
              title="累计 GMV"
              value={totalGmv}
              precision={0}
              styles={{ content: { color: '#52c41a', fontSize: '28px' } }}
              prefix="¥"
              suffix=""
              loading={loading}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} md={8}>
          <Card>
            <Statistic
              title="数据点数"
              value={gmvData.length}
              styles={{ content: { color: '#faad14', fontSize: '28px' } }}
              suffix="个"
              loading={loading}
            />
          </Card>
        </Col>
      </Row>

      <Row gutter={[24, 24]} style={{ marginTop: '24px' }}>
        <Col xs={24} lg={16}>
          <Card title="GMV 趋势图" variant="borderless">
            {loading ? (
              <div style={{ height: '350px', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                加载中...
              </div>
            ) : (
              <Line {...lineConfig} height={350} />
            )}
          </Card>
        </Col>
        <Col xs={24} lg={8}>
          <Card title="GMV 分布图" variant="borderless">
            {loading ? (
              <div style={{ height: '350px', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                加载中...
              </div>
            ) : (
              <Area {...areaConfig} height={350} />
            )}
          </Card>
        </Col>
      </Row>

      <Row gutter={[24, 24]} style={{ marginTop: '24px' }}>
        <Col xs={24}>
          <Card title="GMV 详细数据" variant="borderless">
            {loading ? (
              <div style={{ height: '300px', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                加载中...
              </div>
            ) : (
              <div style={{ maxHeight: '300px', overflowY: 'auto' }}>
                <table style={{ width: '100%', borderCollapse: 'collapse' }}>
                  <thead>
                    <tr style={{ background: '#fafafa', position: 'sticky', top: 0 }}>
                      <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #f0f0f0' }}>时间</th>
                      <th style={{ padding: '12px', textAlign: 'right', borderBottom: '1px solid #f0f0f0' }}>GMV</th>
                      <th style={{ padding: '12px', textAlign: 'right', borderBottom: '1px solid #f0f0f0' }}>占比</th>
                    </tr>
                  </thead>
                  <tbody>
                    {gmvData.map((item, index) => (
                      <tr key={index} style={{ borderBottom: '1px solid #f0f0f0' }}>
                        <td style={{ padding: '12px' }}>{item.time}</td>
                        <td style={{ padding: '12px', textAlign: 'right' }}>
                          ¥{item.gmv.toLocaleString()}
                        </td>
                        <td style={{ padding: '12px', textAlign: 'right' }}>
                          {totalGmv > 0 ? ((item.gmv / totalGmv) * 100).toFixed(2) : '0.00'}%
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </Card>
        </Col>
      </Row>
    </div>
  );
};

export default GMVDashboard;