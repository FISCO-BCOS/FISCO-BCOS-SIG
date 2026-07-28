<template>
  <div id="app">
    <el-container>
      <el-header class="header"><Header /></el-header>
      <el-container>
        <el-aside width="220px"><navigator></navigator></el-aside>
        <el-main class="trace-main" style="padding:20px!important">
          <div class="page-title-bar">
            <h2><i class="el-icon-search"></i>溯源查询</h2>
            <p class="title-desc">输入产品编号或扫描二维码，查看全链路溯源信息</p>
          </div>

          <!-- 查询区域 -->
          <el-card shadow="never" class="search-section fade-in-up" style="max-width:700px; margin-bottom:20px;">
            <el-row :gutter="12" type="flex" align="middle">
              <el-col :span="18">
                <el-input v-model="productNo" placeholder="请输入产品编号或扫描二维码内容" clearable size="medium" @keyup.enter.native="handleQuery"></el-input>
              </el-col>
              <el-col :span="6">
                <el-button type="primary" @click="handleQuery" :loading="loading" style="width:100%;">查询验证</el-button>
              </el-col>
            </el-row>
          </el-card>

          <!-- 空状态 -->
          <el-empty v-if="!searched && !loading" description="输入产品编号开始溯源查询">
            <p style="color:#999;font-size:13px;">在产品包装或标签上找到溯源二维码/编号，输入即可查看全链路信息</p>
          </el-empty>

          <!-- 加载中 -->
          <div v-if="loading" style="text-align:center; padding:40px 0;">
            <i class="el-icon-loading" style="font-size:36px;color:#409EFF;"></i>
            <p style="color:#999;margin-top:8px;">正在查询链上数据...</p>
          </div>

          <!-- 查询结果 -->
          <div v-if="searched && !loading">

            <!-- 验证结果 -->
            <el-card v-if="result" shadow="never" class="fade-in-up" style="max-width:700px; margin-bottom:20px;">
              <el-alert v-if="result.isValid" title="链上验证通过" type="success" :description="'产品编号: ' + result.productNo + ' | 查询时间: ' + result.queryTime" show-icon :closable="false"/>
              <el-alert v-else title="链上验证暂不可用" type="warning" description="区块链节点连接中，以下展示数据库存档的全链路溯源信息（数据真实有效）" show-icon :closable="false"/>
            </el-card>

            <!-- 产品基本信息 -->
            <el-card v-if="result" shadow="never" class="fade-in-up" style="max-width:700px; margin-bottom:20px;">
              <div slot="header"><span>{{ result.productName }}</span><el-tag style="float:right">{{ result.category }}</el-tag></div>
              <el-row :gutter="20">
                <el-col :span="9">
                  <el-image v-if="result.images && result.images.length > 0" :src="result.images[0]" style="width:100%;height:150px;border-radius:4px;" fit="cover"></el-image>
                  <el-empty v-else description="暂无图片" :image-size="80"></el-empty>
                </el-col>
                <el-col :span="15">
                  <el-descriptions :column="1" border size="small">
                    <el-descriptions-item label="产品编号">{{ result.productNo }}</el-descriptions-item>
                    <el-descriptions-item label="价格">¥{{ result.price }}</el-descriptions-item>
                    <el-descriptions-item label="单位">{{ result.unit }}</el-descriptions-item>
                    <el-descriptions-item label="产地">{{ result.originAddress }}</el-descriptions-item>
                  </el-descriptions>
                </el-col>
              </el-row>
            </el-card>

            <!-- 全链路溯源时间线 -->
            <el-card v-if="result" shadow="never" class="fade-in-up" style="max-width:700px; margin-bottom:20px;" body-style="padding:15px 30px;">
              <div slot="header"><span>全链路溯源</span></div>
              <el-timeline>
                <el-timeline-item v-for="(item, index) in timelineData" :key="index" :icon="item.icon" :color="item.color" :timestamp="item.time" placement="top">
                  <h4>{{ item.title }}</h4>
                  <div v-if="item.content">
                    <template v-if="item.type === 'planting'">
                      <el-descriptions :column="1" border size="mini">
                        <el-descriptions-item label="种植基地">{{ item.content.baseName }}</el-descriptions-item>
                        <el-descriptions-item label="品种">{{ item.content.variety }}</el-descriptions-item>
                        <el-descriptions-item label="播种日期">{{ item.content.sowingDate }}</el-descriptions-item>
                        <el-descriptions-item label="面积(亩)">{{ item.content.area }}</el-descriptions-item>
                      </el-descriptions>
                      <div v-if="item.content.farmingOps && item.content.farmingOps.length > 0" style="margin-top:8px;">
                        <el-collapse accordion>
                          <el-collapse-item v-for="(op, idx) in item.content.farmingOps" :key="idx" :title="(op.action || op.type || '操作') + ' - ' + op.date" :name="'' + idx">
                            <div>{{ op.detail || op.description || '' }}</div>
                          </el-collapse-item>
                        </el-collapse>
                      </div>
                    </template>
                    <template v-else-if="item.type === 'processing'">
                      <el-descriptions :column="1" border size="mini">
                        <el-descriptions-item label="加工日期">{{ item.content.processDate }}</el-descriptions-item>
                      </el-descriptions>
                      <div v-if="item.content.steps && item.content.steps.length > 0" style="margin-top:8px;">
                        <el-table :data="item.content.steps" border stripe size="mini" style="width:100%">
                          <el-table-column prop="stepName" label="步骤名称" width="120"></el-table-column>
                          <el-table-column prop="description" label="描述"></el-table-column>
                          <el-table-column prop="operator" label="操作人" width="80"></el-table-column>
                        </el-table>
                      </div>
                      <div v-else style="margin-top:8px;color:#909399;">{{ item.content.description }}</div>
                    </template>
                    <template v-else-if="item.type === 'testing'">
                      <el-descriptions :column="1" border size="mini">
                        <el-descriptions-item label="报告名称">{{ item.content.reportName }}</el-descriptions-item>
                        <el-descriptions-item label="检测机构">{{ item.content.testingOrg }}</el-descriptions-item>
                        <el-descriptions-item label="检测日期">{{ item.content.testingDate }}</el-descriptions-item>
                        <el-descriptions-item label="检测结果">
                          <el-tag :type="item.content.result === '合格' ? 'success' : 'danger'" size="small">{{ item.content.result }}</el-tag>
                        </el-descriptions-item>
                      </el-descriptions>
                      <div v-if="item.content.items && item.content.items.length > 0" style="margin-top:8px;">
                        <el-table :data="item.content.items" border stripe size="mini" style="width:100%">
                          <el-table-column prop="itemName" label="检测项" min-width="100"></el-table-column>
                          <el-table-column prop="standardValue" label="标准值" width="100"></el-table-column>
                          <el-table-column prop="actualValue" label="检测值" width="100"></el-table-column>
                          <el-table-column prop="result" label="结果" width="60" align="center">
                            <template slot-scope="scope">
                              <el-tag :type="scope.row.result === '合格' ? 'success' : 'danger'" size="mini">{{ scope.row.result === '合格' ? '✓' : '✗' }}</el-tag>
                            </template>
                          </el-table-column>
                        </el-table>
                      </div>
                    </template>
                    <template v-else-if="item.type === 'logistics'">
                      <el-descriptions :column="1" border size="mini">
                        <el-descriptions-item label="操作类型">{{ item.content.type }}</el-descriptions-item>
                        <el-descriptions-item label="地点">{{ item.content.location }}</el-descriptions-item>
                        <el-descriptions-item label="描述">{{ item.content.description }}</el-descriptions-item>
                        <el-descriptions-item label="操作人">{{ item.content.operator }}</el-descriptions-item>
                        <el-descriptions-item label="操作时间">{{ item.content.operateTime }}</el-descriptions-item>
                      </el-descriptions>
                    </template>
                  </div>
                  <p v-else style="color:#999;font-size:13px;">暂无记录</p>
                </el-timeline-item>
              </el-timeline>
            </el-card>

            <!-- 统计卡片 -->
            <el-card v-if="result" shadow="never" class="fade-in-up" style="max-width:700px; margin-bottom:20px;" body-style="padding:15px;">
              <el-row :gutter="16" type="flex" justify="space-around">
                <el-col :span="7" style="text-align:center">
                  <div style="font-size:22px;font-weight:bold;color:#409EFF;">{{ result.scanCount || 0 }}</div>
                  <div style="font-size:12px;color:#999;margin-top:4px;">累计查询</div>
                </el-col>
                <el-col :span="7" style="text-align:center">
                  <div style="font-size:13px;font-weight:bold;color:#67C23A;">{{ result.queryTime }}</div>
                  <div style="font-size:12px;color:#999;margin-top:4px;">本次查询</div>
                </el-col>
                <el-col :span="7" style="text-align:center">
                  <div style="font-size:22px;font-weight:bold;" :style="{color: result.chainVerified ? '#67C23A' : '#E6A23C'}">{{ result.chainVerified ? '✅' : '⏳' }}</div>
                  <div style="font-size:12px;color:#999;margin-top:4px;">链上验证</div>
                </el-col>
              </el-row>
            </el-card>

            <!-- 未查到结果 -->
            <el-card v-if="searched && !loading && !result" shadow="never" class="fade-in-up" style="max-width:700px;">
              <el-alert title="未查到该产品信息" type="warning" description="请检查输入的产品编号是否正确" show-icon :closable="false"/>
            </el-card>

          </div>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script>
import Header from '@/components/Header';
import Navigator from '@/components/Navigator';

export default {
  name: 'TraceQuery',
  components: { Header, Navigator },
  data() {
    return {
      productNo: '',
      loading: false,
      searched: false,
      result: null,
      timelineData: []
    }
  },
  methods: {
    mapPlanting(raw) {
      if (!raw) return null;
      var obj = {
        baseName: raw.baseName || '',
        variety: raw.cropVariety || '',
        sowingDate: raw.sowDate || '',
        area: raw.areaSize || '',
        farmingOps: []
      };
      try {
        if (raw.farmOps && typeof raw.farmOps === 'string') {
          obj.farmingOps = JSON.parse(raw.farmOps);
        } else if (Array.isArray(raw.farmOps)) {
          obj.farmingOps = raw.farmOps;
        }
      } catch(e) { obj.farmingOps = []; }
      return obj;
    },
    mapProcessing(raw) {
      if (!raw) return null;
      var steps = [];
      try {
        if (raw.processSteps && typeof raw.processSteps === 'string') {
          steps = JSON.parse(raw.processSteps);
        } else if (Array.isArray(raw.processSteps)) {
          steps = raw.processSteps;
        }
      } catch(e) { steps = []; }
      var desc = '';
      if (steps.length > 0) {
        desc = steps.map(function(s) { return s.stepName || ''; }).filter(Boolean).join(' → ');
      }
      return {
        stepName: '加工',
        description: desc || raw.processSteps || '',
        steps: steps,
        operator: '',
        processDate: raw.createTime ? raw.createTime.substring(0, 10) : ''
      };
    },
    mapTesting(raw) {
      if (!raw) return null;
      var items = [];
      try {
        if (raw.testItems && typeof raw.testItems === 'string') {
          items = JSON.parse(raw.testItems);
        } else if (Array.isArray(raw.testItems)) {
          items = raw.testItems;
        }
      } catch(e) { items = []; }
      items = items.map(function(item) {
        return {
          itemName: item.itemName || '',
          standardValue: item.standardValue || item.standard || '',
          actualValue: item.actualValue || item.resultValue || '',
          result: item.result || item.judgment || ''
        };
      });
      return {
        reportName: '检测报告-' + (raw.testingNo || ''),
        testingOrg: '检测机构',
        testingDate: raw.testDate || '',
        result: raw.testResult === 1 ? '合格' : '不合格',
        items: items
      };
    },
    mapLogistics(raw) {
      if (!raw) return null;
      var nodes = [];
      try {
        if (raw.logisticsNodes && typeof raw.logisticsNodes === 'string') {
          nodes = JSON.parse(raw.logisticsNodes);
        } else if (Array.isArray(raw.logisticsNodes)) {
          nodes = raw.logisticsNodes;
        }
      } catch(e) { nodes = []; }
      var firstNode = nodes.length > 0 ? nodes[0] : {};
      return {
        type: raw.transportMode || '运输',
        location: firstNode.location || '',
        description: [raw.vehicleInfo, firstNode.status].filter(Boolean).join(' / '),
        operator: firstNode.operator || '',
        operateTime: firstNode.arriveTime || (raw.createTime ? raw.createTime.substring(0, 16).replace('T', ' ') : '')
      };
    },
    handleQuery() {
      if (!this.productNo.trim()) {
        this.$message.warning('请输入产品编号');
        return;
      }
      this.loading = true;
      this.searched = false;
      this.axios.get('/api/trace/query/' + this.productNo.trim(), {
        headers: { Authorization: 'Bearer ' + this.$cookies.get('token') }
      })
        .then(res => {
          if (res.data.code == 200 && res.data.data) {
            var raw = res.data.data;
            var p = raw.product || {};
            // 展平 product 嵌套结构，映射字段名
            this.result = {
              isValid: raw.chainVerified === true,
              productNo: p.productNo || '',
              productName: p.productName || '',
              category: p.category || '',
              price: p.price || 0,
              unit: p.unit || '',
              originAddress: p.originAddress || '',
              images: p.images || [],
              scanCount: raw.scanCount || 0,
              queryTime: new Date().toLocaleString(),
              chainVerified: raw.chainVerified,
              planting: this.mapPlanting(raw.planting),
              processing: this.mapProcessing(raw.processing),
              testing: this.mapTesting(raw.testing),
              logistics: this.mapLogistics(raw.logistics)
            };
            this.buildTimeline();
          } else {
            this.result = null;
          }
        })
        .catch(() => {
          this.result = null;
          this.$message.error('查询失败，请稍后重试');
        })
        .finally(() => {
          this.loading = false;
          this.searched = true;
        });
    },
    buildTimeline() {
      this.timelineData = [];
      if (this.result.planting) {
        this.timelineData.push({
          type: 'planting', icon: 'el-icon-s-promotion', color: '#67C23A',
          title: '种植环节', time: this.result.planting.sowingDate || '',
          content: this.result.planting
        });
      }
      if (this.result.processing) {
        this.timelineData.push({
          type: 'processing', icon: 'el-icon-s-operation', color: '#409EFF',
          title: '加工环节', time: this.result.processing.processDate || '',
          content: this.result.processing
        });
      }
      if (this.result.testing) {
        this.timelineData.push({
          type: 'testing', icon: 'el-icon-success', color: '#E6A23C',
          title: '检测环节', time: this.result.testing.testingDate || '',
          content: this.result.testing
        });
      }
      if (this.result.logistics) {
        this.timelineData.push({
          type: 'logistics', icon: 'el-icon-truck', color: '#909399',
          title: '物流环节', time: this.result.logistics.operateTime || '',
          content: this.result.logistics
        });
      }
    }
  }
}
</script>

<style scoped>
.trace-main { padding: 20px !important; }
.page-title-bar { margin-bottom: 16px; }
.page-title-bar h2 { font-size: 20px; font-weight: 600; color: var(--text-primary); margin: 0 0 4px; display: flex; align-items: center; gap: 8px; }
.page-title-bar h2 i { color: var(--primary); }
.title-desc { font-size: 13px; color: var(--text-secondary); }
.search-section { border-radius: var(--radius-md); box-shadow: var(--shadow-sm); }
.fade-in-up { animation: fadeInUp 0.5s ease both; }
@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(16px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
