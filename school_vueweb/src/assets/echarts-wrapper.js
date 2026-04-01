// ECharts wrapper for ES module import
// This file wraps the UMD echarts.js to provide ES module compatibility

// Load the echarts library
import './echarts.js'

// Get echarts from global object
const echarts = (typeof window !== 'undefined' && window.echarts) || 
                (typeof global !== 'undefined' && global.echarts) ||
                (typeof globalThis !== 'undefined' && globalThis.echarts)

export default echarts
export { echarts }
